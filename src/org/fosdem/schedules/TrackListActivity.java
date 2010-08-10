/**
 * 
 */
package org.fosdem.schedules;

import java.util.ArrayList;

import net.spamt.froscon10.R;

import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Room;
import org.fosdem.pojo.Track;
import org.fosdem.util.RoomAdapter;
import org.fosdem.util.TrackAdapter;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class TrackListActivity extends ListActivity  {

	

	
	public static final String LOG_TAG=TrackListActivity.class.getName();

	public static final String DAY_INDEX = "dayIndex";
	
	private static final int ALL_EVENTS = Menu.FIRST;
	private static final int SETTINGS = Menu.FIRST + 1;
	
	private ArrayList<Track> tracks = null;
	private int dayIndex = 0;

	private ArrayList<Room> rooms;
	private enum SortBy {
		Track, Room
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// what day should we show? fetch from the parameters or saved instance
		dayIndex = savedInstanceState != null ? savedInstanceState.getInt(DAY_INDEX) : 0;
	
		if (dayIndex == 0) { 
			Bundle extras = getIntent().getExtras();
			if (extras != null)
				dayIndex = extras.getInt(DAY_INDEX);
			if (dayIndex == 0 ) {
				Log.e(LOG_TAG, "You are loading this class with no valid day parameter");
				return;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		String sortby = prefs.getString(Preferences.PREF_SORT, "Track");
		switch (SortBy.valueOf(sortby)) {
		case Track:
			handleTrack();
			break;
		case Room:
			handleRoom();
		default:
			break;
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ALL_EVENTS, 1, R.string.all_events).setIcon(android.R.drawable.ic_menu_view);
		menu.add(0, SETTINGS, 2, R.string.menu_settings).setIcon(
				android.R.drawable.ic_menu_preferences);

		return true;
	}
	protected void handleRoom() {
		setTitle("Rooms for Day " + dayIndex);
		rooms = getRooms();
		setListAdapter(new RoomAdapter(this, R.layout.track_list, getRooms()));
	}

	protected void handleTrack()	{
		tracks = getTracks();
		setTitle("Tracks for Day " + dayIndex);
		setListAdapter(new TrackAdapter(this, R.layout.track_list, getTracks()));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Object o = getListView().getItemAtPosition(position);
		Intent i = new Intent(this, EventListActivity.class);
		if (o.getClass().isInstance(new Track(""))) {
			Track track = (Track) o;
			Log.d(LOG_TAG, "Track selected: " + track.getName());
			i.putExtra(EventListActivity.TRACK_NAME, track.getName());
		} else if(o.getClass().isInstance(new Room(""))) {
			Room room = (Room) o;
			Log.d(LOG_TAG, "Room selected: " + room.getName());
			i.putExtra(EventListActivity.ROOM, room.getName());
		}
		i.putExtra(EventListActivity.DAY_INDEX, dayIndex);
		startActivity(i);
	}

	private ArrayList<Track> getTracks() {
		
		// Load event with specified id from the db
		final DBAdapter db = new DBAdapter(this);
		try {
			db.open();
			String[] trackNames = db.getTracksByDayIndex(dayIndex);
			ArrayList<Track> tracks = new ArrayList<Track>();
			for (String trackName : trackNames) {
				tracks.add(new Track(trackName));
			}
			return tracks;
		} finally {
			db.close();
		}
	}

	private ArrayList<Room> getRooms() {
		// Load event with specified id from the db
		final DBAdapter db = new DBAdapter(this);
		try {
			db.open();
			String[] roomNames = db.getRoomsByDayIndex(dayIndex);
			ArrayList<Room> rooms = new ArrayList<Room>();
			for (String roomName : roomNames) {
				rooms.add(new Room(roomName));
			}
			return rooms;
		} finally {
			db.close();
		}
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
		case ALL_EVENTS:
			i = new Intent(this, EventListActivity.class);
			i.putExtra(EventListActivity.DAY_INDEX, dayIndex);
			startActivity(i);
			return true;
		case SETTINGS:
			i = new Intent(this, SortPreferences.class);
			startActivity(i);
			return true;
		default:
			return false;
		}


	}
	
}
