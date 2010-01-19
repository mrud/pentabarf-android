package org.fosdem.schedules;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;
import org.fosdem.util.StringUtil;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayEvent extends Activity {

	/** Display event action string */
	public final static String ACTION_DISPLAY_EVENT = "org.fosdem.schedules.DISPLAY_EVENT";

	/** Id extras parameter name */
	public final static String ID = "org.fosdem.Id";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displayevent);

		// Get the event from the intent
		final Event event = getEvent();

		// No event? stop this activity
		if (event == null) {
			finish();
			return;
		}

		// populate the UI_event
		showEvent(event);
	}

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

	/**
	 * Loads the contents of the event with into the gui.
	 * 
	 * @param event
	 *            The event to show
	 */
	private void showEvent(Event event) {
		setTextViewText(R.id.event_title, event.getTitle());
		setTextViewText(R.id.event_track, event.getTrack());
		setTextViewText(R.id.event_room, event.getRoom());
		setTextViewText(R.id.event_time, StringUtil.datesToString(event.getStart(), event.getDuration()));
		setTextViewText(R.id.event_speaker, StringUtil.personsToString(event.getPersons()));
		setTextViewText(R.id.event_abstract, StringUtil.niceify(event.getAbstract_description()));
		setTextViewText(R.id.event_description, StringUtil.niceify(event.getDescription()));
	}

}
