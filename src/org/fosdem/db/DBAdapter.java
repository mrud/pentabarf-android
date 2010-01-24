package org.fosdem.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fosdem.pojo.Day;
import org.fosdem.pojo.Event;
import org.fosdem.pojo.Person;
import org.fosdem.pojo.Room;
import org.fosdem.pojo.Schedule;
import org.fosdem.schedules.Main;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/*
 * This class can either be used as a content provider or as a standalone DBAdapter.
 */
public class DBAdapter extends ContentProvider {
	// Provider related
	public static final String PROVIDER_NAME = "org.fosdem.pojo.Event";
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + "/events");

	private static final int EVENTS = 1;
	private static final int EVENT_ID = 2;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "events", EVENTS);
		uriMatcher.addURI(PROVIDER_NAME, "events/#", EVENT_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case EVENTS:
			count = db.delete(TABLE_EVENTS, selection, selectionArgs);
			break;
		case EVENT_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(TABLE_EVENTS, ID
					+ " = "
					+ id
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case EVENTS:
			return "vnd.org.fosdem.events/vnd.fosdem.org";
		case EVENT_ID:
			return "vnd.org.fosdem.event/vnd.fosdem.org";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = db.insert(TABLE_EVENTS, null, values);

		if (rowId > 0) {
			Uri _uri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		return db != null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (uriMatcher.match(uri) == EVENT_ID)
			selection = ID + " = " + uri.getPathSegments().get(1);
		if (sortOrder == null || sortOrder.length() == 0)
			sortOrder = START;

		Cursor c = db.query(TABLE_EVENTS, projection, selection, selectionArgs,
				null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case EVENT_ID:
			String id = uri.getPathSegments().get(1);
			count = db.update(TABLE_EVENTS, values, ID
					+ " = "
					+ id
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		case EVENTS:
			count = db.update(TABLE_EVENTS, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri " + uri);

		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	// DB Related
	protected static final String DB_NAME = "fosdem_schedule";
	protected static final String TABLE_EVENTS = "events";
	protected static final String TABLE_PERSONS = "persons";
	protected static final String TABLE_JOIN_PERSON_EVENT = "person_event";
	protected static final String TABLE_FAVORITES = "favorites";
	protected static final int DB_VERSION = 6;

	public static final String ID = "id";
	public static final String START = "start";
	public static final String DURATION = "duration";
	public static final String ROOM = "room";
	public static final String TAG = "tag";
	public static final String TITLE = "title";
	public static final String SUBTITLE = "subtitle";
	public static final String TRACK = "track";
	public static final String EVENTTYPE = "eventtype";
	public static final String LANGUAGE = "language";
	public static final String ABSTRACT = "abstract";
	public static final String DESCRIPTION = "description";
	public static final String DAYINDEX = "dayindex";
	public static final String NAME = "name";
	public static final String PERSONS = "person";
	public static final String PERSONID = "personid";
	public static final String EVENTID = "eventid";
	public static final String PERSONSEARCH = "personsearch";

	// TODO eMich - replace fields and table
	protected static final String DB_CREATE_EVENTS = "create table events (id integer primary key,start long,duration integer,room text,tag text,title text,subtitle text,track text,eventtype text,language text,abstract text,description text,dayindex integer,personsearch text)";
	protected static final String DB_CREATE_PERSONS = "create table persons (id integer primary key,name text)";
	protected static final String DB_CREATE_PERSON_EVENT = "create table person_event (id integer primary key autoincrement,personid integer,eventid integer)";
	protected static final String DB_CREATE_FAVORITES = "create table favorites(id integer primary key,start long)";

	protected DatabaseHelper dbHelper;
	protected Context context;
	protected SQLiteDatabase db;

	public DBAdapter() {
		super();
	}

	public DBAdapter(Context context) {
		this.context = context;
		dbHelper = new DatabaseHelper(context);
	}

	protected static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATE_EVENTS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.v(getClass().getName(), "Updating db to version " + DB_VERSION);
			db.execSQL("drop table if exists events");
			db.execSQL("drop table if exists persons");
			db.execSQL("drop table if exists person_event");
			db.execSQL("drop table if exists favorites");
			db.execSQL(DB_CREATE_EVENTS);
			db.execSQL(DB_CREATE_PERSONS);
			db.execSQL(DB_CREATE_PERSON_EVENT);
			db.execSQL(DB_CREATE_FAVORITES);
		}
	}

	public DBAdapter open() throws SQLException {
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public void persistSchedule(Schedule s) {
		clearEvents();
		clearPersons();
		clearPersonEventLinks();
		for (Day day : s.getDays()) {
			for (Room room : day.getRooms()) {
				for (Event event : room.getEvents()) {
					addEvent(event);
					persistPersons(event.getPersons());
					persistPersonEventLink(event);
				}
			}
		}
	}

	public void persistPersons(List<Person> persons) {
		for (Person p : persons) {
			addPerson(p);
		}
	}

	public void persistPersonEventLink(Event event) {
		for (Person p : event.getPersons()) {
			linkPersonToEvent(p, event);
		}
	}

	public long addPerson(Person person) {
		deleteFromPersons(person.getId());
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, person.getId());
		initialValues.put(NAME, person.getName());
		return db.insert(TABLE_PERSONS, null, initialValues);
	}

	public long linkPersonToEvent(Person person, Event event) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(PERSONID, person.getId());
		initialValues.put(EVENTID, event.getId());
		return db.insert(TABLE_JOIN_PERSON_EVENT, null, initialValues);
	}

	public long addEvent(Event event) {
		deleteFromEvents(event.getId());
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, event.getId());
		initialValues.put(START, event.getStart().getTime());
		initialValues.put(DURATION, event.getDuration());
		initialValues.put(ROOM, event.getRoom());
		initialValues.put(TAG, event.getTag());
		initialValues.put(TITLE, event.getTitle());
		initialValues.put(SUBTITLE, event.getSubtitle());
		initialValues.put(TRACK, event.getTrack());
		initialValues.put(EVENTTYPE, event.getType());
		initialValues.put(LANGUAGE, event.getLanguage());
		initialValues.put(ABSTRACT, event.getAbstract_description());
		initialValues.put(DESCRIPTION, event.getDescription());
		initialValues.put(DAYINDEX, event.getDayindex());
		initialValues.put(PERSONSEARCH, event.getPersons().toString());
		return db.insert(TABLE_EVENTS, null, initialValues);
	}

	public long addBookmark(Event event) {
		deleteBookmark(event.getId());
		Log.v(getClass().getName(), event.getId() + " - "
				+ event.getStart().getTime());
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, event.getId());
		initialValues.put(START, event.getStart().getTime());
		long rowId = db.insert(TABLE_FAVORITES, null, initialValues);

		Intent intent = new Intent(Main.FAVORITES_UPDATE);
		intent.putExtra(Main.COUNT, getBookmarkCount());
		context.sendBroadcast(intent);
		
		return rowId;
	}
	
	public long getBookmarkCount(){
		Cursor c = db.rawQuery("select count(" + ID + ") from " + TABLE_FAVORITES, null);
		c.moveToFirst();
		long count = c.getLong(0);
		c.close();
		return count;
	}

	public boolean deleteBookmark(int id) {
		Log.v(getClass().getName(), "Deleting " + id);
		boolean success = db.delete(TABLE_FAVORITES, ID + "='" + id + "'", null) > 0;
		
		//Intent intent = new Intent(context,FavoritesBroadcastReceiver.class);
		Intent intent = new Intent(Main.FAVORITES_UPDATE);
		intent.putExtra(Main.COUNT, getBookmarkCount());
		context.sendBroadcast(intent);
		
		return success;
	}

	public boolean deleteFromPersons(int id) {
		return db.delete(TABLE_PERSONS, ID + "='" + id + "'", null) > 0;
	}

	public boolean deleteFromEvents(int id) {
		return db.delete(TABLE_EVENTS, ID + "='" + id + "'", null) > 0;
	}

	protected Cursor getRawEvents() {
		return db.query(TABLE_EVENTS, new String[] { ID, START, DURATION, ROOM,
				TAG, TITLE, SUBTITLE, TRACK, EVENTTYPE, LANGUAGE, ABSTRACT,
				DESCRIPTION, DAYINDEX }, null, null, null, null, START, null);
	}

	public List<Event> getEvents() {
		Cursor eventsCursor = getRawEvents();
		return getEventsFromCursor(eventsCursor);
	}

	protected Cursor getRawRooms() {
		return db.query(true, TABLE_EVENTS, new String[] { ROOM }, null, null,
				null, null, START, null);
	}

	protected ArrayList<Person> getPersonsForEvent(int id) {
		Cursor c = db.query(TABLE_JOIN_PERSON_EVENT, new String[] { PERSONID },
				EVENTID + " = '" + id + "'", null, null, null, null);
		int[] personIds = getIntFromCursor(c, PERSONID);

		ArrayList<Person> persons = new ArrayList<Person>();
		for (int personId : personIds) {
			Person person = getPersonById(personId);
			if (person != null)
				persons.add(person);
		}

		return persons;
	}

	protected Person getPersonById(int id) {
		Cursor c = db.query(TABLE_PERSONS, new String[] { NAME }, ID + " = "
				+ id, null, null, null, null);
		String[] persons = getStringFromCursor(c, NAME);
		if (persons.length == 0)
			return null;
		return new Person(id, persons[0]);
	}

	public String[] getRooms() {
		Cursor trackCursor = getRawRooms();
		return getStringFromCursor(trackCursor, ROOM);
	}

	public ArrayList<Event> getFavoriteEvents(Date fromDate) {
		String dateCriteria = null;
		if (fromDate != null)
			dateCriteria = START + ">" + fromDate.getTime();
		Cursor favoriteIdsCursor = db.query(TABLE_FAVORITES,
				new String[] { ID }, dateCriteria, null, null, null, START);
		int[] favoriteIds = getIntFromCursor(favoriteIdsCursor, ID);
		ArrayList<Event> events = new ArrayList<Event>();
		for (int favoriteId : favoriteIds) {
			Event event = getEventById(favoriteId);
			if (event != null)
				events.add(event);
		}
		return events;
	}

	public boolean isFavorite(Event event) {
		Cursor isFavoritesCursor = db.query(TABLE_FAVORITES,
				new String[] { ID }, ID + "='" + event.getId() + "'", null,
				null, null, null);
		boolean retVal = isFavoritesCursor.getCount() > 0;
		isFavoritesCursor.close();
		return retVal;
	}

	public List<Date> getDays() {
		ArrayList<Date> list = new ArrayList<Date>();
		Cursor c = db.rawQuery("select min(" + START + "),max(" + START
				+ ") from " + TABLE_EVENTS, null);
		c.moveToFirst();
		long min = c.getLong(0);
		long max = c.getLong(1);
		Date minDate = new Date(min);
		Date maxDate = new Date(max);
		Date currDate = minDate;
		currDate.setHours(0);
		currDate.setMinutes(0);
		currDate.setSeconds(0);
		while (currDate.getTime() <= maxDate.getTime()) {
			list.add(currDate);
			currDate = new Date(currDate.getTime() + (60 * 60 * 24 * 1000));
		}
		c.close();
		return list;
	}

	public String[] getRoomsByDayIndex(int dayIndex) {
		Cursor roomCursor = db.query(true, TABLE_EVENTS, new String[] { ROOM },
				DAYINDEX + "=" + dayIndex, null, null, null, START, null);
		return getStringFromCursor(roomCursor, ROOM);
	}

	public String[] getTracksByDayIndex(int dayIndex) {
		Cursor trackCursor = db.query(true, TABLE_EVENTS,
				new String[] { TRACK }, DAYINDEX + "=" + dayIndex, null, null,
				null, null, null);
		return getStringFromCursor(trackCursor, TRACK);
	}

	protected Cursor getRawTracks() {
		return db.query(true, TABLE_EVENTS, new String[] { TRACK }, null, null,
				null, null, START, null);
	}

	public String[] getTracks() {
		Cursor trackCursor = getRawTracks();
		return getStringFromCursor(trackCursor, TRACK);
	}

	protected String[] getStringFromCursor(Cursor cursor, String field) {
		cursor.moveToFirst();
		String[] values = new String[cursor.getCount()];
		for (int i = 0; i < cursor.getCount(); i++) {
			values[i] = cursor.getString(cursor.getColumnIndex(field));
			cursor.moveToNext();
		}
		cursor.close();
		return values;
	}

	protected int[] getIntFromCursor(Cursor cursor, String field) {
		cursor.moveToFirst();
		int[] values = new int[cursor.getCount()];
		for (int i = 0; i < cursor.getCount(); i++) {
			values[i] = cursor.getInt(cursor.getColumnIndex(field));
			cursor.moveToNext();
		}
		cursor.close();
		return values;
	}

	public List<Event> getEventsFiltered(Date beginDate, Date endDate,
			String[] tracks, String[] types, String[] tags, String[] rooms,
			String[] languages, Integer dayIndex) {
		StringBuilder sb = new StringBuilder();
		if (tracks != null)
			for (String track : tracks) {
				sb.append(" or track='" + track + "'");
			}
		if (types != null)
			for (String type : types) {
				sb.append(" or eventtype='" + type + "'");
			}
		if (tags != null)
			for (String tag : tags) {
				sb.append(" or tag='" + tag + "'");
			}
		if (rooms != null)
			for (String room : rooms) {
				sb.append(" or room='" + room + "'");
			}
		if (languages != null)
			for (String language : languages) {
				sb.append(" or language='" + language + "'");
			}
		if (beginDate != null && endDate != null) {
			sb.append("and (start>=" + beginDate.getTime() + " and end<="
					+ endDate.getTime() + ")");
		}
		if (dayIndex != null) {
			sb.append(" or dayindex = :" + dayIndex + "");
		}
		String where = sb.toString();
		if (where.startsWith(" or ")) {
			where = where.substring(4);
		}
		if (where.startsWith(" and ")) {
			where = where.substring(5);
		}
		Log.v(getClass().getName(), where);
		Cursor c = db.query(TABLE_EVENTS, new String[] { ID, START, DURATION,
				ROOM, TAG, TITLE, SUBTITLE, TRACK, EVENTTYPE, LANGUAGE,
				ABSTRACT, DESCRIPTION, DAYINDEX }, where, null, null, null,
				START, null);
		return getEventsFromCursor(c);
	}

	public List<Event> getEventsFilteredLike(Date beginDate, Date endDate,
			String[] titles, String[] tracks, String[] types, String[] tags,
			String[] rooms, String[] languages, String[] persons) {
		StringBuilder sb = new StringBuilder();
		if (titles != null)
			for (String title : titles) {
				sb.append(" or title like '%" + title + "%'");
			}
		if (tracks != null)
			for (String track : tracks) {
				sb.append(" or track like '%" + track + "%'");
			}
		if (types != null)
			for (String type : types) {
				sb.append(" or eventtype like '%" + type + "%'");
			}
		if (tags != null)
			for (String tag : tags) {
				sb.append(" or tag like '%" + tag + "%'");
			}
		if (rooms != null)
			for (String room : rooms) {
				sb.append(" or room like '%" + room + "%'");
			}
		if (languages != null)
			for (String language : languages) {
				sb.append(" or language like '%" + language + "%'");
			}
		if (persons != null)
			for (String person : persons) {
				sb.append(" or personsearch like '%" + person + "%'");
			}
		if (beginDate != null && endDate != null) {
			sb.append("and (start>=" + beginDate.getTime() + " and end<="
					+ endDate.getTime() + ")");
		}
		String where = sb.toString();
		if (where.startsWith(" or ")) {
			where = where.substring(4);
		}
		if (where.startsWith(" and ")) {
			where = where.substring(5);
		}
		Log.v(getClass().getName(), where);
		Cursor c = db.query(TABLE_EVENTS, new String[] { ID, START, DURATION,
				ROOM, TAG, TITLE, SUBTITLE, TRACK, EVENTTYPE, LANGUAGE,
				ABSTRACT, DESCRIPTION, DAYINDEX }, where, null, null, null,
				START, null);
		return getEventsFromCursor(c);
	}

	/**
	 * Converts a cursor over the events table to a list of {@link Event}s. If
	 * the cursor is empty, will return an empty list.
	 * 
	 * @param eventsCursor
	 *            The cursor.
	 * @return A list of events.
	 */
	protected List<Event> getEventsFromCursor(Cursor eventsCursor) {
		eventsCursor.moveToFirst();
		final List<Event> events = new ArrayList<Event>();
		for (int i = 0; i < eventsCursor.getCount(); i++) {
			final Event event = new Event();
			event.setId(eventsCursor.getInt(eventsCursor.getColumnIndex(ID)));
			event.setStart(new Date(eventsCursor.getLong(eventsCursor
					.getColumnIndex(START))));
			event.setDuration(eventsCursor.getInt(eventsCursor
					.getColumnIndex(DURATION)));
			event.setRoom(eventsCursor.getString(eventsCursor
					.getColumnIndex(ROOM)));
			event.setTag(eventsCursor.getString(eventsCursor
					.getColumnIndex(TAG)));
			event.setTitle(eventsCursor.getString(eventsCursor
					.getColumnIndex(TITLE)));
			event.setSubtitle(eventsCursor.getString(eventsCursor
					.getColumnIndex(SUBTITLE)));
			event.setTrack(eventsCursor.getString(eventsCursor
					.getColumnIndex(TRACK)));
			event.setType(eventsCursor.getString(eventsCursor
					.getColumnIndex(EVENTTYPE)));
			event.setLanguage(eventsCursor.getString(eventsCursor
					.getColumnIndex(LANGUAGE)));
			event.setAbstract_description(eventsCursor.getString(eventsCursor
					.getColumnIndex(ABSTRACT)));
			event.setDescription(eventsCursor.getString(eventsCursor
					.getColumnIndex(DESCRIPTION)));
			event.setDayindex(eventsCursor.getInt(eventsCursor
					.getColumnIndex(DAYINDEX)));
			events.add(event);
			event.setPersons(getPersonsForEvent(event.getId()));
			eventsCursor.moveToNext();
		}
		eventsCursor.close();
		return events;
	}

	/**
	 * Retrieves the event for given id, or null if no such event exists.
	 * 
	 * @param id
	 *            The id of the requested event.
	 * @return The event or null.
	 */
	public Event getEventById(int id) {
		final Cursor eventsById = db.query(TABLE_EVENTS, new String[] { ID,
				START, DURATION, ROOM, TAG, TITLE, SUBTITLE, TRACK, EVENTTYPE,
				LANGUAGE, ABSTRACT, DESCRIPTION, DAYINDEX }, "id = :1",
				new String[] { Integer.toString(id) }, null, null, START, null);

		if (eventsById.getCount() < 1)
			return null;

		return getEventsFromCursor(eventsById).get(0);
	}

	public List<Event> getEventsByTrackNameAndDayIndex(String trackName,
			int dayIndex) {
		String tracks[] = { trackName };
		return getEventsFiltered(null, null, tracks, null, null, null, null,
				dayIndex);
	}

	public void clearEvents() {
		db.execSQL("delete from " + TABLE_EVENTS);
	}

	public void clearPersons() {
		db.execSQL("delete from " + TABLE_PERSONS);
	}

	public void clearPersonEventLinks() {
		db.execSQL("delete from " + TABLE_JOIN_PERSON_EVENT);
	}

	public void clearBookmarks() {
		db.execSQL("delete from " + TABLE_FAVORITES);
	}

}
