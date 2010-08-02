package org.fosdem.schedules;

import java.util.ArrayList;
import java.util.Date;

import org.fosdem.R;
import org.fosdem.broadcast.FavoritesBroadcast;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;
import org.fosdem.util.EventAdapter;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class EventListActivity extends ListActivity {

	public static final String LOG_TAG = EventListActivity.class.getName();

	public static final String DAY_INDEX = "dayIndex";
	public static final String TRACK_NAME = "trackName";
	public static final String QUERY = "query";
	public static final String FAVORITES = "favorites";
	public static final String ROOM = "roomName";
	public static final String TIME = "time";

	private ArrayList<Event> events = null;
	private String trackName = null;
	private int dayIndex = -1;
	private String query = null;
	private Boolean favorites = null;
	private EventAdapter eventAdapter = null;
	private String roomName = null;
	private Long timeSearch  = null;
	// 10 min.
	private static final long CURRENT_TIME_SLICE = 600000;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		trackName = savedInstanceState != null ? savedInstanceState
				.getString(TRACK_NAME) : null;

		// what room should we show? fetch from the parameters
		Bundle extras = getIntent().getExtras();
		if (trackName == null && query == null && extras != null) {
			trackName = extras.getString(TRACK_NAME);
			if (extras.containsKey(DAY_INDEX)) {
				dayIndex = extras.getInt(DAY_INDEX);
			}
			favorites = extras.getBoolean(FAVORITES);
			roomName = extras.getString(ROOM);
			query = extras.getString(QUERY);
			timeSearch = extras.getLong(TIME);
		}
		if (trackName != null && dayIndex != 0)
			setTitle("Day " + dayIndex + " - " + trackName);
		if (trackName == null && roomName != null) {
			setTitle("Day " + dayIndex + " - Room " + roomName);
		}
		if (query != null)
			setTitle("Search for: " + query);
		if (favorites != null && favorites) {
			setTitle("Favorites");

			registerReceiver(favoritesChangedReceiver, new IntentFilter(
					FavoritesBroadcast.ACTION_FAVORITES_UPDATE));

		}

		events = getEventList(favorites);
		if (events.size() <= 0) {
			this.finish();
			final Context context = getApplicationContext();
			final Toast toast = Toast.makeText(context, "Could not find events", Toast.LENGTH_LONG);
			toast.show();
		}
		eventAdapter = new EventAdapter(this, R.layout.event_list, events);
		setListAdapter(eventAdapter);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Event event = (Event) getListView().getItemAtPosition(position);

		Log.d(LOG_TAG, "Event selected: " + event.getId() + " - "
				+ event.getTitle());

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
	private ArrayList<Event> getEventList(Boolean favoritesOnly) {

		if (query == null && trackName == null && roomName == null
				&& dayIndex == -1 && timeSearch == null
				&& (favoritesOnly == null || !favoritesOnly)) {
			Log.e(LOG_TAG,
					"You are loading this class with no valid room parameter");
			return null;
		}

		// Load event with specified id from the db
		final DBAdapter db = new DBAdapter(this);
		try {
			db.open();

			if (trackName != null) {
				setTitle("Events on Track " + trackName + " for Day " + dayIndex);
				return (ArrayList<Event>) db.getEventsByTrackNameAndDayIndex(
						trackName, dayIndex);
			} else if (query != null) {
				String[] queryArgs = new String[] { query };
				return (ArrayList<Event>) db.getEventsFilteredLike(null, null,
						queryArgs, queryArgs, queryArgs, queryArgs, queryArgs,
						null, queryArgs);
			} else if (favorites != null && favorites) {
				Log.e(LOG_TAG, "Getting favorites...");

				SharedPreferences prefs = getSharedPreferences(Main.PREFS, Context.MODE_PRIVATE);
				Date startDate=prefs.getBoolean(Preferences.PREF_UPCOMING, false)?new Date():null;

				return db.getFavoriteEvents(startDate);
			} else if (roomName != null) {
				setTitle("Events in Room " + roomName + " on Day " + dayIndex);
				return (ArrayList<Event>) db.getEventsbyRoomNameAndDayIndex(roomName, dayIndex);
			} else if (dayIndex != -1){
				return (ArrayList<Event>) db.getEventsbyDayIndex(dayIndex);
			} else if (timeSearch != null) {
				setTitle("Upcoming Events");
				Date beginDate = new Date(timeSearch -CURRENT_TIME_SLICE);
				return (ArrayList<Event>) db.getUpcomingEvents(beginDate);
			} else {
				return null;
			}
 		} finally {
			db.close();
		}
	}

	public static void doSearchWithIntent(Context context,
			final Intent queryIntent) {
		queryIntent.getStringExtra(SearchManager.QUERY);
		Intent i = new Intent(context, EventListActivity.class);
		i.putExtra(EventListActivity.QUERY, queryIntent
				.getStringExtra(SearchManager.QUERY));
		context.startActivity(i);
	}

	private BroadcastReceiver favoritesChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			eventAdapter.clear();
			events = getEventList(favorites);
			for (Event event : events) {
				eventAdapter.add(event);
			}
			if (events == null || events.size() == 0)
				EventListActivity.this.finish();
		}
	};

	protected void onDestroy() {
		super.onDestroy();
		if (favorites != null && favorites)
			unregisterReceiver(favoritesChangedReceiver);

	}
}
