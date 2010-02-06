package org.fosdem.broadcast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;
import org.fosdem.schedules.DisplayEvent;
import org.fosdem.schedules.Main;
import org.fosdem.schedules.Preferences;
import org.fosdem.util.StringUtil;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		
		// Check intent type
		//Log.v(getClass().getName(),"Action received: "+intent.getAction());
		String action = intent.getAction();
		if (FavoritesBroadcast.ACTION_FAVORITES_INITIAL_LOAD.equals(action)){
			setEventAlarms();
		}
		else if (FavoritesBroadcast.ACTION_FAVORITES_UPDATE.equals(action)) {
			// Favourite has been added or removed
			handleUpdate(intent);
		} else if (FavoritesBroadcast.ACTION_FAVORITES_ALARM.equals(action)) {
			// Alarm has gone off; show notification
			//Log.v(getClass().getName(),"Alarm has gone off!");
			int eventId = intent.getIntExtra(FavoritesBroadcast.EXTRA_ID, -1);
			//Log.v(getClass().getName(),"Event id: "+eventId);
			createNotification(eventId);
		}
	}

	private void handleUpdate(Intent intent) {
		int actionType = intent.getIntExtra(FavoritesBroadcast.EXTRA_TYPE, -1);
		long eventId = intent.getLongExtra(FavoritesBroadcast.EXTRA_ID, -1);
		if (actionType == FavoritesBroadcast.EXTRA_TYPE_INSERT) {
			// Set alarm for newly-added favourite
			setEventAlarm(eventId);
		} else if (actionType == FavoritesBroadcast.EXTRA_TYPE_DELETE) {
			// Remove alarm for removed favourite
			removeEventAlarm(eventId);
		} else if (actionType == FavoritesBroadcast.EXTRA_TYPE_REMOVE_NOTIFICATION) {
			// User has just clicked on a notification
			cancelNotification(eventId);
		} else if (actionType == FavoritesBroadcast.EXTRA_TYPE_RESCHEDULE) {
			// User has changed delay preference; reschedule alarms
			setEventAlarms();
		}
	}
	
	private void createNotification(int eventId) {
		// Ensure user wants notified
		SharedPreferences prefs = mContext.getSharedPreferences(Main.PREFS, Context.MODE_PRIVATE);
		
		if (!prefs.getBoolean(Preferences.PREF_NOTIFY, true)) {
			Log.d("NotifyRecv", "User doesn't want notified");
			return;
		}

		Event event = getEventById(eventId);
		if (event == null) {
			return;
		}
		
		long startTime = event.getStart().getTime();
		int delayMins = prefs.getInt(Preferences.PREF_DELAY, 10);
		if (System.currentTimeMillis() < (startTime - (delayMins * 60 * 1000))) {
			// Shouldn't happen...
			return;
		}
		
		// TODO: make notification icon (hero is black bg while magic white ->
		// what to do ?)
		int icon = R.drawable.icon;
		Notification notification = new Notification(icon, event.getTitle(), startTime);
		Boolean vibrate = prefs.getBoolean(Preferences.PREF_VIBRATE, true);
		Boolean led = prefs.getBoolean(Preferences.PREF_LED, true);
		if (vibrate) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		if (led) {
			notification.ledARGB = 0xff0000ff;
			notification.ledOnMS = 100;
			notification.ledOffMS = 1000;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}

		CharSequence title = event.getTitle();
		CharSequence text = event.getRoom() + " - "  
			+ StringUtil.personsToString(event.getPersons());

		Intent notificationIntent = new Intent(mContext, DisplayEvent.class);
		notificationIntent.putExtra(DisplayEvent.ID, eventId);
		notificationIntent.setData(Uri.parse(event.toString()));

		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(mContext, title, text, contentIntent);

		NotificationManager notificationManager = 
			(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(event.getId(), notification);
	}
	
	private void cancelNotification(long eventId) {
		NotificationManager notificationManager = 
			(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel((int) eventId);
	}
	
	private void setEventAlarms() {
		SharedPreferences prefs = mContext.getSharedPreferences(Main.PREFS, Context.MODE_PRIVATE);
		int delayMins = prefs.getInt(Preferences.PREF_DELAY, 10);
		
		DBAdapter db = new DBAdapter(mContext);
		db.open();

		ArrayList<Event> events = db.getFavoriteEvents(new Date());
		for (Event event : events) {
			setEventAlarm(event, delayMins);
		}

		db.close();
	}

	private void setEventAlarm(long eventId) {
		SharedPreferences prefs = mContext.getSharedPreferences(Main.PREFS, Context.MODE_PRIVATE);
		int delayMins = prefs.getInt(Preferences.PREF_DELAY, 10);
		setEventAlarm(getEventById(eventId), delayMins);
	}
	
	private void setEventAlarm(Event event, int delayMins) {
		//Log.v(getClass().getName(),event.toString()+" "+delayMins);
		if (event == null) {
			return;
		}
		
		// Schedule alarm based on current delay time
		long alarmTime = event.getStart().getTime() - (delayMins * 60 * 1000);
		if (alarmTime < System.currentTimeMillis()) {
			return;
		}
		
		PendingIntent alarmIntent = getPendingAlarmIntent(event);
		AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
	}
	
	private void removeEventAlarm(long eventId) {
		// Attempt to un-schedule alarm for this event
		Event event = getEventById(eventId);
		if (event == null) {
			return;
		}
		
		PendingIntent alarmIntent = getPendingAlarmIntent(event);
		AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(alarmIntent);
	}
	
	private PendingIntent getPendingAlarmIntent(Event event) {
		Intent notifyIntent = new Intent(FavoritesBroadcast.ACTION_FAVORITES_ALARM);
		notifyIntent.setData(Uri.parse("event:"+ event.getId())); // make Intent unique
		notifyIntent.putExtra(FavoritesBroadcast.EXTRA_ID, event.getId());
		return PendingIntent.getBroadcast(mContext, 0, notifyIntent, PendingIntent.FLAG_ONE_SHOT);
	}
	
	private Event getEventById(long eventId) {
		DBAdapter db = new DBAdapter(mContext);
		db.open();
		Event event = db.getEventById((int) eventId);
		db.close();
		return event;
	}
}
