package org.fosdem.schedules;

import java.util.ArrayList;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Day;
import org.fosdem.pojo.Event;
import org.fosdem.pojo.Room;
import org.fosdem.util.EventAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
	public class EventListActivity extends ListActivity{

	public static final String LOG_TAG=EventListActivity.class.getName();
	
	public static final String ROOM_NAME = "roomName";
	public static final String DAY_INDEX = "dayIndex";
	
	private ArrayList<Event> events = null;
	private Room room = null;
	private String roomName = null;
    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		roomName = savedInstanceState != null ? savedInstanceState.getString(ROOM_NAME) : null;
		// TODO dayindex
		
		events = getEventList();
		
		setTitle(roomName);
		// FIXME also pass on the day or dates of the event
		
		// TODO chri - adapt layout to show a right arrow 
        setListAdapter(new EventAdapter(this, R.layout.event_list, events));
       
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Event event = (Event) getListView().getItemAtPosition(position);
        
        Log.d(LOG_TAG, "Event selected: " + event.getTitle());
        
        Intent i = new Intent(this, EventListActivity.class);
		i.putExtra(DisplayEvent.ID, event.getId());
		startActivity(i);
    }
    
	
	/**
	 * Gets the {@link Event} that was specified through the intent or null if
	 * no or wrongly specified event.
	 * 
	 * @return The Event or null.
	 */
	private ArrayList<Event> getEventList() {
		
		// TODO dayIndex 
		// what room should we show? fetch from the parameters
		if (roomName == null) { 
			Bundle extras = getIntent().getExtras();
			if (extras != null)
				roomName = extras.getString(ROOM_NAME);
			if (roomName == null ) {
				Log.e(LOG_TAG, "You are loading this class with no valid room parameter");
				return null;
			}
		}
		
		// Load event with specified id from the db
		final DBAdapter db = new DBAdapter(this);
		try {
			db.open();
			return (ArrayList<Event>) db.getEventsByRoomName(roomName);
		} finally {
			db.close();
		}
	}

    
	
}
