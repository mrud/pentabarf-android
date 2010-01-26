package org.fosdem.schedules;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;

import org.fosdem.db.DBAdapter;
import org.fosdem.exceptions.ParserException;
import org.fosdem.listeners.ParserEventListener;
import org.fosdem.parsers.ScheduleParser;
import org.fosdem.pojo.Schedule;
import org.fosdem.util.FileUtil;
import org.fosdem.util.StringUtil;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author sandbender
 * 
 */
public class BackgroundUpdater implements Runnable {
	
	private final static Object LOCK = new Object();

	private final Handler handler;
	private final ParserEventListener parseEventListener;
	private final Context context;
	private final boolean doUpdateXml;
	private final boolean doUpdateRooms;

	/**
	 * Constructor
	 * 
	 * @param handler
	 *            Handler that gets messages about progress
	 * @param parseEventListener
	 *            Listener that gets messages about the progress of parsing the
	 *            xml.
	 * @param context
	 *            The application context.
	 */
	public BackgroundUpdater(Handler handler, ParserEventListener parseEventListener, Context context, boolean updateXml, boolean updateRooms) {
		this.handler = handler;
		this.parseEventListener = parseEventListener;
		this.context = context;
		this.doUpdateRooms = updateRooms;
		this.doUpdateXml = updateXml;
	}

	/**
	 * Sends a message to the handler of the specific type (in arg1)
	 * 
	 * @param type
	 */
	private void sendMessage(int type) {
		final Message msg = Message.obtain();
		msg.what = type;
		handler.sendMessage(msg);
	}

	/**
	 * Downloads the xml and repopulates the database
	 * 
	 * @throws ParserException
	 * @throws IOException
	 * 
	 */
	private void updateXml() throws ParserException, IOException {
		
		sendMessage(Main.STARTFETCHING);

		// Parse
		final ScheduleParser parser = new ScheduleParser(Main.XML_URL);
		parser.addTagEventListener(parseEventListener);
		final Schedule s = parser.parse();

		sendMessage(Main.DONEFETCHING);

		// Persist
		final DBAdapter db = new DBAdapter(context);
		db.open();
		try {
			db.persistSchedule(s, handler);
		} finally {
			db.close();
		}

		sendMessage(Main.DONELOADINGDB);
	}

	/**
	 * Prefetches the rooms images
	 */
	private void updateRooms() {

		sendMessage(Main.ROOMIMGSTART);

		// get the list of the rooms
		final String[] rooms;
		final DBAdapter db = new DBAdapter(context);
		db.open();
		try {
			rooms = db.getRooms();
		} finally {
			db.close();
		}

		// Download the images in the background
		for (final String room : rooms) {
			// Log.d(LOG_TAG, "Downloading room image:" + room);
			try {
				FileUtil.fetchCached(StringUtil.roomNameToURL(room));
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
		}

		sendMessage(Main.ROOMIMGDONE);
	}

	@Override
	public void run() {
		synchronized (LOCK) {
			try {
				if (doUpdateXml)
					updateXml();
				if (doUpdateRooms)
					updateRooms();
			} catch (IOException e) {
			} catch (ParserException e) {
			}
		}
	}
}