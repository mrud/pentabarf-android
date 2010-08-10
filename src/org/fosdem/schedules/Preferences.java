package org.fosdem.schedules;

import org.fosdem.R;
import org.fosdem.broadcast.FavoritesBroadcast;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class Preferences extends PreferenceActivity implements
		OnPreferenceChangeListener {
	public static final String PREF_NOTIFY = "notifyPref";
	public static final String PREF_VIBRATE = "vibratePref";
	public static final String PREF_DELAY = "delayPref";
	public static final String PREF_LED = "ledPref";
	public static final String PREF_UPCOMING = "upcomingPref";
	public static final String PREF_SORT = "sortbyPref";

	private Preference notifyPref;
	private Preference vibratePref;
	private Preference ledPref;
	private Preference delayPref;
	private Preference upcomingPref;
	private Preference sortPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.menu_settings);
		addPreferencesFromResource(R.xml.preferences);
		// Get the custom preference
		/*
		 * Preference customPref = (Preference) findPreference("customPref");
		 * customPref .setOnPreferenceClickListener(new
		 * OnPreferenceClickListener() {
		 * 
		 * public boolean onPreferenceClick(Preference preference) {
		 * Toast.makeText(getBaseContext(),
		 * "The custom preference has been clicked", Toast.LENGTH_LONG).show();
		 * SharedPreferences customSharedPreference = getSharedPreferences(
		 * "myCustomSharedPrefs", Activity.MODE_PRIVATE);
		 * SharedPreferences.Editor editor = customSharedPreference .edit();
		 * editor.putString("myCustomPref", "The preference has been clicked");
		 * editor.commit(); return true; }
		 * 
		 * });
		 */
		notifyPref = (Preference) findPreference(PREF_NOTIFY);
		notifyPref.setOnPreferenceChangeListener(this);

		vibratePref = (Preference) findPreference(PREF_VIBRATE);
		vibratePref.setOnPreferenceChangeListener(this);

		delayPref = (Preference) findPreference(PREF_DELAY);
		delayPref.setOnPreferenceChangeListener(this);
		
		ledPref = (Preference) findPreference(PREF_LED);
		ledPref.setOnPreferenceChangeListener(this);
		
		upcomingPref = (Preference) findPreference(PREF_UPCOMING);
		upcomingPref.setOnPreferenceChangeListener(this);

		sortPref = (Preference) findPreference(PREF_SORT);
		sortPref.setOnPreferenceChangeListener(this);
		
		SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),
				Activity.MODE_PRIVATE);
		vibratePref.setEnabled(sharedPreferences.getBoolean(PREF_NOTIFY, true));
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),
				Activity.MODE_PRIVATE);
		Editor edit = sharedPreferences.edit();
		
		if (preference.getKey().equals(PREF_NOTIFY)) {
			vibratePref.setEnabled((Boolean) newValue);
			delayPref.setEnabled((Boolean) newValue);
		}
		if (preference.getKey().equals(PREF_DELAY)) {
			String value=(String)newValue;
			if(value!=null && value.length()==0)return false;
			edit.putInt(PREF_DELAY, Integer.parseInt(value));
			
			// Update alarms
			Intent intent = new Intent(FavoritesBroadcast.ACTION_FAVORITES_UPDATE);
			intent.putExtra(FavoritesBroadcast.EXTRA_TYPE, FavoritesBroadcast.EXTRA_TYPE_RESCHEDULE);
			sendBroadcast(intent);
		}
		if(preference.getKey().equals(PREF_UPCOMING) || preference.getKey().equals(PREF_NOTIFY) || preference.getKey().equals(PREF_VIBRATE) || preference.getKey().equals(PREF_LED)){
			edit.putBoolean(preference.getKey(), (Boolean)newValue);
		}
		if (preference.getKey().equals(PREF_SORT)) {
			edit.putString(preference.getKey(), (String) newValue);
		}
		edit.commit();
		return true;
	}

}