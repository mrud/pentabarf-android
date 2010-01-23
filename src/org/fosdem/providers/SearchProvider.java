package org.fosdem.providers;

import java.util.ArrayList;
import java.util.List;

import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;
import org.fosdem.pojo.Person;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class SearchProvider extends ContentProvider {
	// TODO eMich - add person to search suggestions
	public static String AUTHORITY = "fosdemsearch";

	private static final int SEARCH_SUGGEST = 0;
	private static final int SHORTCUT_REFRESH = 1;
	private static final UriMatcher sURIMatcher = buildUriMatcher();

	/**
	 * Sets up a uri matcher for search suggestion and shortcut refresh queries.
	 */
	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
				SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
				SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT,
				SHORTCUT_REFRESH);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT
				+ "/*", SHORTCUT_REFRESH);
		return matcher;
	}

	private static final String[] COLUMNS = {
			"_id", // must include this column
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA, };

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (!TextUtils.isEmpty(selection)) {
			throw new IllegalArgumentException("selection not allowed for "
					+ uri);
		}
		if (selectionArgs != null && selectionArgs.length != 0) {
			throw new IllegalArgumentException("selectionArgs not allowed for "
					+ uri);
		}
		if (!TextUtils.isEmpty(sortOrder)) {
			throw new IllegalArgumentException("sortOrder not allowed for "
					+ uri);
		}
		switch (sURIMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			String query = null;
			if (uri.getPathSegments().size() > 1) {
				query = uri.getLastPathSegment().toLowerCase();
			}
			Log.v(getClass().getName(), "Query: " + query);
			return getSuggestions(query, projection);
		case SHORTCUT_REFRESH:
			String shortcutId = null;
			if (uri.getPathSegments().size() > 1) {
				shortcutId = uri.getLastPathSegment();
			}
			return refreshShortcut(shortcutId, projection);
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	private Cursor getSuggestions(String query, String[] projection) {
		DBAdapter db = new DBAdapter(getContext());
		db.open();
		String[] queryVal = new String[] { query };
		MatrixCursor cursor = null;
		try {
			List<Event> events = db.getEventsFilteredLike(null, null, queryVal,
					queryVal, queryVal, queryVal, queryVal, null, queryVal);
			cursor = new MatrixCursor(COLUMNS);
			for (Event event : events) {
				cursor.addRow(columnValuesOfEvent(event));
			}
		} finally {
			db.close();
		}

		return cursor;
	}

	private Object[] columnValuesOfEvent(Event event) {
		return new String[] {
				Integer.toString(event.getId()), // _id
				event.getTitle(), // text1
				getPersonsAsString(event.getPersons()) + " - "
						+ event.getTrack(), // text2
				Integer.toString(event.getId()), // intent_data (included when
		// clicking on item)
		};
	}

	private String getPersonsAsString(ArrayList<Person> persons) {
		StringBuilder personStr = new StringBuilder();
		for (Person person : persons) {
			if (personStr.length() != 0)
				personStr.append(" , ");
			personStr.append(person.getName());
		}
		return personStr.toString();
	}

	/**
	 * Note: this is unused as is, but if we included
	 * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our
	 * results, we could expect to receive refresh queries on this uri for the
	 * id provided, in which case we would return a cursor with a single item
	 * representing the refreshed suggestion data.
	 */
	private Cursor refreshShortcut(String shortcutId, String[] projection) {
		return null;
	}

	/**
	 * All queries for this provider are for the search suggestion and shortcut
	 * refresh mime type.
	 */
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;
		case SHORTCUT_REFRESH:
			return SearchManager.SHORTCUT_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

}
