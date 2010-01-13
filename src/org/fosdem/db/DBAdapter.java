package org.fosdem.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fosdem.pojo.Day;
import org.fosdem.pojo.Event;
import org.fosdem.pojo.Room;
import org.fosdem.pojo.Schedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	protected static final String DB_NAME = "fosdem_schedule";
	protected static final String TABLE_EVENTS = "events";
	protected static final int DB_VERSION = 1;

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

	// TODO replace fields and table
	protected static final String DB_CREATE_EVENTS = "create table events (id integer primary key,start long,duration int,room text,tag text,title text,subtitle text,track text,eventtype text,language text,abstract text,description text)";

	protected DatabaseHelper dbHelper;
	protected Context context;
	protected SQLiteDatabase db;

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
			db.execSQL("drop table if exists events");
			db.execSQL(DB_CREATE_EVENTS);
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
		for (Day day : s.getDays()) {
			for (Room room : day.getRooms()) {
				for (Event event : room.getEvents()) {
					addEvent(event);
				}
			}
		}
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
		return db.insert(TABLE_EVENTS, null, initialValues);
	}

	public boolean deleteFromEvents(int id) {
		return db.delete(TABLE_EVENTS, ID + "='" + id + "'", null) > 0;
	}

	protected Cursor getRawEvents() {
		return db.query(TABLE_EVENTS, new String[] { ID, START, DURATION, ROOM, TAG, TITLE, SUBTITLE, TRACK, EVENTTYPE, LANGUAGE, ABSTRACT, DESCRIPTION }, null, null, null, null, null, null);
	}

	public List<Event> getEvents() {
		Cursor eventsCursor = getRawEvents();
		return getEventsFromCursor(eventsCursor);
	}

	public List<Event> getEventsFiltered(Date beginDate, Date endDate, String[] tracks, String[] types, String[] tags, String[] rooms, String[] languages) {
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
			sb.append("and (start>=" + beginDate.getTime() + " and end<=" + endDate.getTime() + ")");
		}
		String where = sb.toString();
		if (where.startsWith(" or ")) {
			where = where.substring(4);
		}
		if (where.startsWith(" and ")) {
			where = where.substring(5);
		}
		Log.v(getClass().getName(), where);
		Cursor c = db.query(TABLE_EVENTS, new String[] { ID, START, DURATION, ROOM, TAG, TITLE, SUBTITLE, TRACK, EVENTTYPE, LANGUAGE, ABSTRACT, DESCRIPTION }, where, null, null, null, null, null);
		return getEventsFromCursor(c);
	}

	/**
	 * Converts a cursor over the events table to a list of {@link Event}s. If
	 * the cursor is empty, will return an empty list. 
	 * 
	 * @param eventsCursor The cursor. 
	 * @return A list of events.
	 */
	protected List<Event> getEventsFromCursor(Cursor eventsCursor) {

		final List<Event> events = new ArrayList<Event>();
		while (eventsCursor.moveToNext()) {
			final Event event = new Event();
			event.setId(eventsCursor.getInt(eventsCursor.getColumnIndex(ID)));
			event.setStart(new Date(eventsCursor.getLong(eventsCursor.getColumnIndex(START))));
			event.setDuration(eventsCursor.getInt(eventsCursor.getColumnIndex(DURATION)));
			event.setRoom(eventsCursor.getString(eventsCursor.getColumnIndex(ROOM)));
			event.setTag(eventsCursor.getString(eventsCursor.getColumnIndex(TAG)));
			event.setTitle(eventsCursor.getString(eventsCursor.getColumnIndex(TITLE)));
			event.setSubtitle(eventsCursor.getString(eventsCursor.getColumnIndex(SUBTITLE)));
			event.setTrack(eventsCursor.getString(eventsCursor.getColumnIndex(TRACK)));
			event.setType(eventsCursor.getString(eventsCursor.getColumnIndex(EVENTTYPE)));
			event.setLanguage(eventsCursor.getString(eventsCursor.getColumnIndex(LANGUAGE)));
			event.setAbstract_description(eventsCursor.getString(eventsCursor.getColumnIndex(ABSTRACT)));
			event.setDescription(eventsCursor.getString(eventsCursor.getColumnIndex(DESCRIPTION)));
			events.add(event);
		}
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
		final Cursor eventsById = db.query(TABLE_EVENTS, new String[] { ID, START, DURATION, ROOM, TAG, TITLE, SUBTITLE, TRACK, EVENTTYPE, LANGUAGE, ABSTRACT, DESCRIPTION }, "id = :1",
				new String[] { Integer.toString(id) }, null, null, null, null);

		if (eventsById.getCount() < 1)
			return null;

		return getEventsFromCursor(eventsById).get(0);
	}

	public void clearEvents() {
		db.execSQL("delete from " + TABLE_EVENTS);
	}

}
