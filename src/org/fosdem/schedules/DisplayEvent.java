package org.fosdem.schedules;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;
import org.fosdem.util.FileUtil;
import org.fosdem.util.StringUtil;
import org.fosdem.views.FavoriteButton;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayEvent extends Activity {

	/** Display event action string */
	public final static String ACTION_DISPLAY_EVENT = "org.fosdem.schedules.DISPLAY_EVENT";

	/** Id extras parameter name */
	public final static String ID = "org.fosdem.Id";
	public final static int SHARE_ID = 1;

	private Drawable roomImageDrawable;

	protected static final int MAPREADY = 1120;

	private Event event;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displayevent);

		// Get the event from the intent
		event = getEvent();

		// No event? stop this activity
		if (event == null) {
			finish();
			return;
		}

		// populate the UI_event
		showEvent(event);
		FavoriteButton fb = (FavoriteButton) findViewById(R.id.favoriteButton);
		fb.setEvent(event);
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg == null)
				return;
			if (msg.arg1 == MAPREADY) {
				ImageView iv = (ImageView) findViewById(R.id.room_image);
				iv.setImageDrawable(roomImageDrawable);
				// tv.setText("Fetched "+counter+" events.");
			}
		}
	};

	/**
	 * Gets the {@link Event} that was specified through the intent or null if
	 * no or wrongly specified event.
	 * 
	 * @return The Event or null.
	 */
	private Event getEvent() {

		// Get the extras
		final Bundle extras = getIntent().getExtras();
		if (extras == null)
			return null;

		// Get id from extras
		if (!(extras.get(ID) instanceof Integer))
			return null;
		final int id = (Integer) extras.get(ID);

		// Load event with specified id from the db
		final DBAdapter db = new DBAdapter(this);
		try {
			db.open();
			return db.getEventById(id);
		} finally {
			db.close();
		}
	}

	/**
	 * Helper method to set the text of the {@link TextView} identified by
	 * specified id.
	 * 
	 * @param id
	 *            Id of the view (must be a TextView)
	 * @param value
	 *            Text to set.
	 */
	private void setTextViewText(int id, String value) {
		final TextView tv = (TextView) findViewById(id);

		if (value == null) {
			tv.setText("");
			return;
		}

		tv.setText(value);
	}

	public void prefetchImageViewImageAndShowIt(final String filename) {
		Thread t = new Thread() {
			public void run() {
				try {
					roomImageDrawable = FileUtil.fetchCachedDrawable(filename);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Message msg = Message.obtain();
				msg.arg1 = MAPREADY;
				handler.sendMessage(msg);
			}
		};
		t.start();

	}

	private void setImageViewImage(int id, String filename) {
		if (filename == null) {
			throw new IllegalArgumentException();
		}

		try {
			ImageView iv = (ImageView) findViewById(id);
			iv.setImageDrawable(FileUtil.fetchCachedDrawable(filename));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Loads the contents of the event with into the gui.
	 * 
	 * @param event
	 *            The event to show
	 */
	private void showEvent(Event event) {
		String eventAbstract = StringUtil.niceify(event
				.getAbstract_description());
		if (eventAbstract.length() == 0)
			eventAbstract = "No abstract available.";
		String eventDescription = StringUtil.niceify(event.getDescription());
		if (eventDescription.length() == 0)
			eventDescription = "No lecture description avablable.";

		setTextViewText(R.id.event_title, event.getTitle());
		setTextViewText(R.id.event_track, event.getTrack());
		setTextViewText(R.id.event_room, event.getRoom());
		setTextViewText(R.id.event_time, StringUtil.datesToString(event
				.getStart(), event.getDuration()));
		setTextViewText(R.id.event_speaker, StringUtil.personsToString(event
				.getPersons()));
		setTextViewText(R.id.event_abstract, eventAbstract);
		setTextViewText(R.id.event_description, eventDescription);

		// setImageViewImage(R.id.room_image,
		// StringUtil.roomNameToURL(event.getRoom()));
		prefetchImageViewImageAndShowIt(StringUtil.roomNameToURL(event
				.getRoom()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SHARE_ID, 2, R.string.share).setIcon(
				android.R.drawable.ic_menu_share);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case SHARE_ID:
			share();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void share() {
		final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		String extra = "I'm attending '" + event.getTitle() + "' (Day "
				+ (event.getDayindex() + 1) + " at "
				+ event.getStart().getHours() + ":"
				+ event.getStart().getMinutes() + " @ " + event.getRoom()
				+ ") #fosdem";
		long currentTime = Calendar.getInstance().getTimeInMillis();
		if (currentTime >= event.getStart().getTime()
				&& currentTime <= (event.getStart().getTime() + ((event
						.getDuration() + 10) * 60 * 1000)))
			extra = "I'm currently attending '" + event.getTitle() + "' ("
					+ event.getRoom() + ") #fosdem";
		intent.putExtra(Intent.EXTRA_TEXT, extra);
		startActivity(Intent.createChooser(intent, getString(R.string.share)));
	}
}
