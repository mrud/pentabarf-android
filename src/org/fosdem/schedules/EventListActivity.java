package org.fosdem.schedules;

import java.util.ArrayList;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;
import org.fosdem.util.EventAdapter;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

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

	private ArrayList<Event> events = null;
	private String trackName = null;
	private int dayIndex = 0;
	private String query = null;
	private Boolean favorites = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		trackName = savedInstanceState != null ? savedInstanceState
				.getString(TRACK_NAME) : null;

		// what room should we show? fetch from the parameters
		Bundle extras = getIntent().getExtras();
		if (trackName == null && query == null && extras != null) {
			trackName = extras.getString(TRACK_NAME);
			dayIndex = extras.getInt(DAY_INDEX);
			favorites = extras.getBoolean(FAVORITES);
			query = extras.getString(QUERY);
		}
		if (trackName != null && dayIndex != 0)
			setTitle("Day " + dayIndex + " - " + trackName);
		if (query != null)
			setTitle("Search for: " + query);
		if (favorites != null && favorites)
			setTitle("Favorites");

		events = getEventList(favorites);

		setListAdapter(new EventAdapter(this, R.layout.event_list, events));

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

		if (query == null && trackName == null
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
				return (ArrayList<Event>) db.getEventsByTrackNameAndDayIndex(
						trackName, dayIndex);
			} else if (query != null) {
				String[] queryArgs = new String[] { query };
				return (ArrayList<Event>) db.getEventsFilteredLike(null, null,
						queryArgs, queryArgs, queryArgs, queryArgs, queryArgs,
						null, queryArgs);
			} else if (favorites != null && favorites) {
				Log.e(LOG_TAG, "Getting favorites...");
				return db.getFavoriteEvents(null);
			}

			return (ArrayList<Event>) db.getEventsByTrackNameAndDayIndex(
					trackName, dayIndex);
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

}
