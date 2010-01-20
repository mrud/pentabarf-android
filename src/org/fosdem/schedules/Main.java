package org.fosdem.schedules;

import java.io.IOException;
import java.net.MalformedURLException;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.exceptions.ParserException;
import org.fosdem.listeners.ParserEventListener;
import org.fosdem.parsers.ScheduleParser;
import org.fosdem.pojo.Schedule;
import org.fosdem.util.FileUtil;
import org.fosdem.util.StringUtil;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class Main extends Activity implements ParserEventListener, OnClickListener {
    public static final String LOG_TAG=Main.class.getName();
    protected static final int DONEFETCHING=0;
    protected static final int TAGEVENT=1;
    protected static final int DONELOADINGDB=2;
    protected static final int ROOMIMGSTART=3;
    protected static final int ROOMIMGDONE=4;
    
    
    private static final int ABOUT_ID = Menu.FIRST;
	private static final int UPDATE_ID = Menu.FIRST+1;
	private static final int SETTINGS_ID = Menu.FIRST +2;
	private static final int TEST_ID = Menu.FIRST +3;
	private static final int TEST_DISPLAY_EVENT_ID = Menu.FIRST + 4;
	private static final int PREFETCH_IMG_ID= Menu.FIRST + 5;

    
	public int counter=0;
	protected TextView tv=null;
	protected Button btn_day_1, btn_day_2, btn_search;
   
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Intent intent = getIntent();
        final String queryAction = intent.getAction();
        if(Intent.ACTION_SEARCH.equals(queryAction)){
        	EventListActivity.doSearchWithIntent(this,intent);
        	finish();
        }
        if (Intent.ACTION_VIEW.equals(queryAction)) {
        	Intent i = new Intent(this, DisplayEvent.class);
    		i.putExtra(DisplayEvent.ID, Integer.parseInt(intent.getDataString()));
    		startActivity(i);
    		finish();
        }
        
        setContentView(R.layout.main);
        
        btn_day_1 = (Button) findViewById(R.id.btn_day_1);
        btn_day_1.setOnClickListener(this);
        btn_day_2 = (Button) findViewById(R.id.btn_day_2);
        btn_day_2.setOnClickListener(this);
        btn_search = (Button) findViewById(R.id.btn_search);
        btn_search.setOnClickListener(this);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SETTINGS_ID, 2, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, UPDATE_ID, 2, R.string.menu_update).setIcon(R.drawable.menu_refresh);
        menu.add(0, PREFETCH_IMG_ID, 2, R.string.menu_prefetch_rooms).setIcon(R.drawable.menu_refresh);
        menu.add(0, ABOUT_ID, 2, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
        menu.add(0, TEST_DISPLAY_EVENT_ID, 2, R.string.menu_test_display_event).setIcon(android.R.drawable.ic_menu_view);
        return true;
    }
    
    public void onClick(View v) {
    	int id = v.getId();
    	switch (id) {
    	case R.id.btn_day_1:
    		showTracksForDay(1);
    		break;
    	case R.id.btn_day_2:
    		showTracksForDay(2);
    		break;
		case R.id.btn_search:
			// FIXME 
			break;
		default:
			Log.e(LOG_TAG, "Received a button click, but I don't know from where.");
			break;
		}
	}
    
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case UPDATE_ID:
			updateXML();
			return true;
		case PREFETCH_IMG_ID:
			prefetchAllRoomImages();
			return true;
    	case TEST_DISPLAY_EVENT_ID:
    		testDisplayEvent();
    		return true;
        		
        }
        return super.onMenuItemSelected(featureId, item);
    }
   
    
    public Handler handler = new Handler(){
    	public void handleMessage(Message msg) {
    		if(msg==null)return;
    		if(msg.arg1==TAGEVENT){
				tv.setText("Fetched "+counter+" events.");
    		}else if(msg.arg1==DONEFETCHING){
    			tv.setText("Done fetching, loading into DB");
    		}else if(msg.arg1==DONELOADINGDB){
    			tv.setText("Done loading into DB");
    		}else if (msg.arg1==ROOMIMGSTART) {
    			tv.setText("Downloading room images...");
    		}else if (msg.arg1==ROOMIMGDONE) {
    			tv.setText("Room Images downloaded");
    		}
    	}
    };
    
    public void onTagEvent(String tag, int type) {
		if(tag.equals("event") && type==ParserEventListener.TAG_OPEN){
			counter++;
			Message msg = new Message();
			msg.arg1=TAGEVENT;
			handler.sendMessage(msg);
		}
	}
	
	public void showTracksForDay(int day) {
		Log.d(LOG_TAG, "showTracksForDay("+day+");");
		Intent i = new Intent(this, TrackListActivity.class);
		i.putExtra(TrackListActivity.DAY_INDEX, day);
		startActivity(i);
	}
	
	/**
	 * Sandb's test method for display event activity 
	 */
	private void testDisplayEvent() {
		final Intent intent = new Intent(DisplayEvent.ACTION_DISPLAY_EVENT);
		intent.putExtra(DisplayEvent.ID, 505);
		startActivity(intent);
	}
	
	/**
	 * Download the new schedule from the server and import the data 
	 * in the local database
	 */
	public void updateXML() {
		/*TODO: this is test code, it has to be replaced by some code to:
	        * - if the db is empty, fill it up from xml (using the fetcher as below)
	        * - if db is not empty, fetch headers of xml, check last mod date and 
	        *   compare to fetch date. if fetch date is older dan last mod date, suggest update to user.
	        * - updating: automatic/manual => setting!
	        */
	        
	        /*TODO: make a view scrollable in 2 ways. Nesting a scrollview and horizontal
	         * scroll view doesn't seem to do the trick: no diagonal scrolling...
	         */
	        tv = (TextView)findViewById(R.id.progress);
	        Thread t = new Thread(){
	        	public void run() {
	        		try {
						ScheduleParser parser=new ScheduleParser("http://fosdem.org/schedule/xml");
	        			parser.addTagEventListener(Main.this);
						Schedule s = parser.parse();
						Message msg = new Message();
						msg.arg1=DONEFETCHING;
						handler.sendMessage(msg);
						DBAdapter db = new DBAdapter(Main.this);
						db.open();
						db.persistSchedule(s);
						db.close();
						Message msg2 = new Message();
						msg2.arg1=DONELOADINGDB;
						handler.sendMessage(msg2);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	};
	        	
	        };
	        
	        t.start();
	}
	
	/**
	 * This function will prefetch all the images of the rooms. 
	 * This enables the user to have a fast and internet-less experience. 
	 */
	public void prefetchAllRoomImages() {
		tv = (TextView)findViewById(R.id.progress);
		Thread t = new Thread() {
			public void run() {

				Message msg1 = new Message();
				msg1.arg1 = ROOMIMGSTART;
				handler.sendMessage(msg1);

				String[] rooms;

				// get the list of the rooms
				DBAdapter db = new DBAdapter(Main.this);
				try {
					db.open();
					rooms = db.getRooms();
				} finally {
					db.close();
				}
				// download the images in the background
				for (String room : rooms) {
					Log.d(LOG_TAG, "Downloading room image:" + room);
					try {
						FileUtil.fetchCached(StringUtil.roomNameToURL(room));
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				Message msg2 = new Message();
				msg2.arg1 = ROOMIMGDONE;
				handler.sendMessage(msg2);

			};

		};
        
        t.start();
	}
}