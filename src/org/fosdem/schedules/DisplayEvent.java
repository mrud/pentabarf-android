package org.fosdem.schedules;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;

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

		// final Event event = new Event();
		// event.setTitle("Free. Open. Future?");
		// event.setRoom("Janson");
		// event.setTrack("Keynotes");
		// event.setDescription("Freedom, openness and participation have become a pervasive part of digital life. 250 million people use Firefox. Wikipedia reaches people in 260 languages. Whole countries have Linux in their schools. Flickr hosts millions of openly licenses photos. Apache underpins the Internet. We have moved mountains.\n\n"
		// +
		// "At the same time, the terrain has shifted. Our digital world has moved into the cloud. And, our window into this world is just as often unhackable phones in our pocket as it is flexible computers on our desktop. Hundreds of millions of people take being digital for granted, and rarely stop to think what it means. The world where free and open source software were born is not the same as the world they have helped to build.\n\n"
		// +
		// "It's time to ask: what do freedom, openness and participation look like 10 years from now? How do we promote these values into the future? Building the open web and hackability into the world of mobile is part of the answer. Promoting privacy, portability and user control in the cloud are also critical. But what else? Mark Surman will reflect on these questions and chat with the FOSDEM crowd.");
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
		if (value == null)
			value = "Null!!";
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
		setTextViewText(R.id.event_speaker, event.getPersons().toString());
		setTextViewText(R.id.event_description, event.getDescription());
	}

}
