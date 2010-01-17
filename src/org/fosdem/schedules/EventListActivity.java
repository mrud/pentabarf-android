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
	
	public static final String DAY_INDEX = "dayIndex";
	public static final String TRACK_NAME = "trackName";
	
	private ArrayList<Event> events = null;
	private String trackName = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		trackName = savedInstanceState != null ? savedInstanceState.getString(TRACK_NAME) : null;
		// TODO dayindex
		
		events = getEventList();
		
		setTitle(trackName);
		// FIXME also pass on the day or dates of the event
		
		setListAdapter(new EventAdapter(this, R.layout.event_list, events));
       
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Event event = (Event) getListView().getItemAtPosition(position);
        
        Log.d(LOG_TAG, "Event selected: " + event.getId() + " - " + event.getTitle());
        
        Intent i = new Intent(this, DisplayEvent.class);
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
		if (trackName == null) { 
			Bundle extras = getIntent().getExtras();
			if (extras != null)
				trackName = extras.getString(TRACK_NAME);
			if (trackName == null ) {
				Log.e(LOG_TAG, "You are loading this class with no valid room parameter");
				return null;
			}
		}
		
		// Load event with specified id from the db
		final DBAdapter db = new DBAdapter(this);
		try {
			db.open();
			return (ArrayList<Event>) db.getEventsByTrackName(trackName);
		} finally {
			db.close();
		}
	}

    
	
}
