package org.fosdem.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.fosdem.R;
import org.fosdem.broadcast.FavoritesBroadcast;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;
import org.fosdem.schedules.DisplayEvent;
import org.fosdem.util.StringUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class NotificationService extends Service {

	protected Context context;
	protected NotificationManager notificationManager;
	protected Timer timer = new Timer();
	protected ArrayList<Integer> notifiedIds = new ArrayList<Integer>();

	private BroadcastReceiver favoritesChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(getClass().getName(), "Received event!");
			int actionType = intent.getIntExtra(FavoritesBroadcast.EXTRA_TYPE,
					-1);
			if (actionType == FavoritesBroadcast.EXTRA_TYPE_DELETE) {
				notifDelete(intent
						.getLongExtra(FavoritesBroadcast.EXTRA_ID, -1));
			} else {
				notifInsert(intent
						.getLongExtra(FavoritesBroadcast.EXTRA_ID, -1));
			}

		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.v(getClass().getName(), "NotificationService started.");
		context = getApplicationContext();
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		registerReceiver(favoritesChangedReceiver, new IntentFilter(
				FavoritesBroadcast.ACTION_FAVORITES_UPDATE));

		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				Log.v(getClass().getName(), "run()");
				addNotifications();
			}

		};
		timer.scheduleAtFixedRate(task, 0, 60000);

		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.v(getClass().getName(), "NotificationService stopped.");
		unregisterReceiver(favoritesChangedReceiver);
		timer.cancel();
		super.onDestroy();
	}

	public void addNotifications() {
		DBAdapter db = new DBAdapter(context);
		db.open();
		Date currentDate = new Date();
		ArrayList<Event> events = db.getFavoriteEvents(new Date());
		for (Event event : events) {
			long timeDiff = event.getStart().getTime() - currentDate.getTime();
			if (timeDiff > 0 && timeDiff < (10 * 60 * 1000))
				addNotification(event);
		}
		db.close();
	}

	public void addNotification(Event event) {
		Date currentDate = new Date();
		long timeDiff = event.getStart().getTime() - currentDate.getTime();
		if (timeDiff < 0 || timeDiff > (10 * 60 * 1000)
				|| notifiedIds.contains(event.getId()))
			return;
		//TODO: make notification icon (hero is black bg while magic white -> what to do ?)
		int icon = R.drawable.icon;
		long when = event.getStart().getTime();
		Notification notification = new Notification(icon, event.getTitle(),
				when);
		CharSequence title = event.getTitle();
		CharSequence text = event.getRoom() + " - "
				+ StringUtil.personsToString(event.getPersons());

		Intent notificationIntent = new Intent(this, DisplayEvent.class);
		notificationIntent.putExtra(DisplayEvent.ID, event.getId());
		notificationIntent.setData(Uri.parse(event.toString()));
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, title, text, contentIntent);

		notificationManager.notify(event.getId(), notification);
		notifiedIds.add(event.getId());
	}

	public void notifDelete(long id) {
		notificationManager.cancel((int) id);
	}

	public void notifInsert(long id) {
		DBAdapter db = new DBAdapter(context);
		db.open();
		Event event = db.getEventById((int) id);
		db.close();
		addNotification(event);
	}

}
