package at.linuxtage.schedule;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import at.linuxtage.R;

public class SortPreferences extends PreferenceActivity implements
		OnPreferenceChangeListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.menu_settings);
		addPreferencesFromResource(R.xml.sort_preferences);
		Preference sortPref = (Preference) findPreference(at.linuxtage.schedule.Preferences.PREF_SORT);
		sortPref.setOnPreferenceChangeListener(this);

	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),
				Activity.MODE_PRIVATE);
		Editor edit = sharedPreferences.edit();

		if (preference.getKey().equals(at.linuxtage.schedule.Preferences.PREF_SORT)) {
			edit.putString(preference.getKey(), (String) newValue);
		}
		edit.commit();
		return true;
	}

}