package org.fosdem.schedules;

import java.util.ArrayList;

import org.fosdem.R;
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
    
	private ArrayList<Event> events = null;
	private Room room = null;
	private String roomName = null;
    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// what room should we show? fetch from the parameters
		roomName = savedInstanceState != null ? savedInstanceState.getString(Room.CLASSNAME) : null;
		
		if (roomName == null) { 
			Bundle extras = getIntent().getExtras();
			if (extras != null)
				roomName = extras.getString(Room.CLASSNAME);
			if (roomName == null ) {
				Log.e(LOG_TAG, "You are loading this class with no valid room parameter");
				return;
			}
		}
		
		setTitle(roomName);
		
//		room = new Room(roomName);

		// FIXME events = database.getEventsByRoomName(roomName);
		
		Event event1 = new Event("Opening talk");
		Event event2 = new Event("Why open source matters");
		Event event3 = new Event("Closing talk");
		
		events = new ArrayList<Event>();
		events.add(event1);
		events.add(event2);
		events.add(event3);
		
		
//		String[] room_a = { "foo", "bar" };
//		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, room_a));
			
		// TODO chri - adapt layout to show a right arrow 
        setListAdapter(new EventAdapter(this, R.layout.simple_list_tab_indicator, events));
       
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Room room = (Room) getListView().getItemAtPosition(position);
        
        Log.d(LOG_TAG, "Room selected: " + room.getName());
        
        // TODO load list of Events in Room
        Intent i = new Intent(this, EventListActivity.class);
		i.putExtra(Room.CLASSNAME, room.getName());
		startActivity(i);
    }
    
    
	
}
