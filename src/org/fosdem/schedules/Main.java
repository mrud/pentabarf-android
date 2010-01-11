package org.fosdem.schedules;

import java.io.IOException;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.exceptions.ParserException;
import org.fosdem.listeners.ParserEventListener;
import org.fosdem.parsers.ScheduleParser;
import org.fosdem.pojo.Schedule;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class Main extends Activity implements ParserEventListener {
    public static final String LOG_TAG=Main.class.getName();
    protected static final int DONEFETCHING=0;
    protected static final int TAGEVENT=1;
    protected static final int DONELOADINGDB=2;
	public int counter=0;
	protected TextView tv=null;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
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
}