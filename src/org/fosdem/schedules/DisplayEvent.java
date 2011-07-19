package org.fosdem.schedules;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;

import net.spamt.debconf10.R;

import org.fosdem.broadcast.FavoritesBroadcast;
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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class DisplayEvent extends Activity implements OnGestureListener {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	/** Display event action string */
	public final static String ACTION_DISPLAY_EVENT = "org.fosdem.schedules.DISPLAY_EVENT";

	/** Id extras parameter name */
	public final static String ID = "org.fosdem.Id";
	public final static int SHARE_ID = 1;

	private Drawable roomImageDrawable;

	protected static final int MAPREADY = 1120;
	public static final String POSITON = "pos";
	public static final String EVENTS = "events";

	private Event event;
	private ScrollView scrollView;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private ArrayList<Integer> event_ids;
	private Integer pos=-1;
	ViewFlipper flipper;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gestureDetector = new GestureDetector(this);
		setContentView(R.layout.displayevent);
		scrollView = (ScrollView) findViewById(R.id.eventscrollview);
		// Get the event from the intent
		event = getEvent();
		// No event? stop this activity
		if (event == null) {
			finish();
			return;
		}
		// populate the UI_event
		showEvent(event);

		Intent intent = new Intent(FavoritesBroadcast.ACTION_FAVORITES_UPDATE);
		intent.putExtra(FavoritesBroadcast.EXTRA_TYPE,
				FavoritesBroadcast.EXTRA_TYPE_REMOVE_NOTIFICATION);
		intent.putExtra(FavoritesBroadcast.EXTRA_ID, ((long) (event.getId())));
		sendBroadcast(intent);

	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg == null)
				return;
			if (msg.what == MAPREADY) {
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
		
		pos = extras.getInt(POSITON);
		event_ids = extras.getIntegerArrayList(EVENTS);

		if (pos == null || event_ids == null) {
			final int id = extras.getInt(ID, -1);
			if (id == -1)
				return null;
			pos = 0;
			event_ids = new ArrayList<Integer>(1);
			event_ids.add(id);
		}
			
		
		return getEvent(event_ids.get(pos));


	}

	private Event getEvent(int id) {

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
				msg.what = MAPREADY;
				handler.sendMessage(msg);
			}
		};
		t.start();

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
//		prefetchImageViewImageAndShowIt(StringUtil.roomNameToURL(event
//				.getRoom()));

		FavoriteButton fb = (FavoriteButton) findViewById(R.id.favoriteButton);
		fb.setEvent(event);

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
				+ (event.getDayindex()) + " at " + event.getStart().getHours()
				+ ":" + event.getStart().getMinutes() + " @ " + event.getRoom()
				+ ") #debconf11";
		long currentTime = Calendar.getInstance().getTimeInMillis();
		if (currentTime >= event.getStart().getTime()
				&& currentTime <= (event.getStart().getTime() + ((event
						.getDuration() + 10) * 60 * 1000)))
			extra = "I'm currently attending '" + event.getTitle() + "' ("
					+ event.getRoom() + ") #debconf11";
		intent.putExtra(Intent.EXTRA_TEXT, extra);
		startActivity(Intent.createChooser(intent, getString(R.string.share)));
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		return gestureDetector.onTouchEvent(me);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		return gestureDetector.onTouchEvent(ev);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				next();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				prev();
			}
		} catch (Exception e) {
		}
		return false;

	}
	private void prev() {
		if (pos <= 0 ) {
			Toast.makeText(this, "No more previous events", Toast.LENGTH_SHORT).show();
			return;
		}
		pos -= 1;
		event = getEvent((int) event_ids.get(pos));
		showEvent(event);

		scrollView.startAnimation(inFromRightAnimation());
		scrollView.smoothScrollTo(0,0);
	}

	private void next() {
		
		if (pos >= event_ids.size() -1) {
			Toast.makeText(this, "No more additional events", Toast.LENGTH_SHORT).show();
			return;
		}
		pos += 1;
		event = getEvent((int) event_ids.get(pos));
		showEvent(event);
		
		scrollView.startAnimation(outToLeftAnimation());
		scrollView.smoothScrollTo(0,0);
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	private Animation inFromRightAnimation() {
		return slideAnimation(0.0f, +1.0f);
	}

	private Animation outToLeftAnimation() {
		return slideAnimation(0.0f, -1.0f);
	}

	private Animation slideAnimation(float right, float left) {

		Animation slide = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
				right, Animation.RELATIVE_TO_PARENT, left,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		slide.setDuration(125);
		slide.setFillBefore(true);
		slide.setInterpolator(new AccelerateInterpolator());
		return slide;
	}

}
