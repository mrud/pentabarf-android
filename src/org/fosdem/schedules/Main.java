package org.fosdem.schedules;

import java.io.IOException;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.exceptions.ParserException;
import org.fosdem.listeners.ParserEventListener;
import org.fosdem.parsers.ScheduleParser;
import org.fosdem.pojo.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Main extends Activity implements ParserEventListener {
    public static final String LOG_TAG=Main.class.getName();
    protected static final int DONEFETCHING=0;
    protected static final int TAGEVENT=1;
    protected static final int DONELOADINGDB=2;
    
    
    private static final int ABOUT_ID = Menu.FIRST;
	private static final int UPDATE_ID = Menu.FIRST+1;
	private static final int SETTINGS_ID = Menu.FIRST +2;
	private static final int TEST_ID = Menu.FIRST +3;
	private static final int TEST_DISPLAY_EVENT_ID = Menu.FIRST + 4;

    
	public int counter=0;
	protected TextView tv=null;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SETTINGS_ID, 2, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, UPDATE_ID, 2, R.string.menu_update).setIcon(R.drawable.menu_refresh);
        menu.add(0, TEST_ID, 2, R.string.menu_testing).setIcon(R.drawable.menu_refresh);
        menu.add(0, ABOUT_ID, 2, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
        menu.add(0, TEST_DISPLAY_EVENT_ID, 2, R.string.menu_test_display_event).setIcon(android.R.drawable.ic_menu_view);
        return true;
    }
    
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case UPDATE_ID:
			updateXML();
			return true;
    	case TEST_ID:
    		testChri();
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
	
	
	public void testChri() {
		// test function for Christophe
		Intent i = new Intent(this, RoomListActivity.class);
		i.putExtra(RoomListActivity.DAY_INDEX, 1);
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
						//ScheduleParser parser=new ScheduleParser("http://fosdem.org/2010/schedule/xml");
						ScheduleParser parser=new ScheduleParser("http://archive.fosdem.org/2009/schedule/xml");
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
}