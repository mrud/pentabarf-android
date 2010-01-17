/**
 * 
 */
package org.fosdem.schedules;

import java.util.ArrayList;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Day;
import org.fosdem.pojo.Event;
import org.fosdem.pojo.Track;
import org.fosdem.util.TrackAdapter;

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
public class TrackListActivity extends ListActivity  {

	public static final String LOG_TAG=TrackListActivity.class.getName();

	public static final String DAY_INDEX = "dayIndex";
	
    
	private ArrayList<Track> tracks = null;
	private Day day = null;
	private int dayIndex = 0;
    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// what day should we show? fetch from the parameters or saved instance
		dayIndex = savedInstanceState != null ? savedInstanceState.getInt(DAY_INDEX) : 0;
		
		
		tracks = getTracks();
		
		setTitle("Tracks for Day " + dayIndex);
		
//		day = new Day();
//		day.setIndex(dayIndex);

//		Track track1 = new Track("Janson");
//		Track track2 = new Track("Chavanne");
//		Track track3 = new Track("Ferrer");
//		
//		tracks = new ArrayList<Track>();
//		tracks.add(track1);
//		tracks.add(track2);
//		tracks.add(track3);
		
		
//		String[] track_a = { "foo", "bar" };
//		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, track_a));
			
		// TODO chri - adapt layout to show a right arrow 
        setListAdapter(new TrackAdapter(this, R.layout.room_list, tracks));
       
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Track track = (Track) getListView().getItemAtPosition(position);
        
        Log.d(LOG_TAG, "Track selected: " + track.getName());
        
        // TODO load list of Events in Track
        Intent i = new Intent(this, EventListActivity.class);
		i.putExtra(EventListActivity.ROOM_NAME, track.getName());
		i.putExtra(EventListActivity.DAY_INDEX, dayIndex);
		startActivity(i);
    }
	
	private ArrayList<Track> getTracks() {

		if (dayIndex == 0) { 
			Bundle extras = getIntent().getExtras();
			if (extras != null)
				dayIndex = extras.getInt(DAY_INDEX);
			if (dayIndex == 0 ) {
				Log.e(LOG_TAG, "You are loading this class with no valid day parameter");
				return null;
			}
		}
		
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
    
    
	
}
