package org.fosdem.schedules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FavoritesBroadcastReceiver extends BroadcastReceiver {

	public static final String ACTION=FavoritesBroadcastReceiver.class.getName();
	public static final String COUNT="count";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(getClass().getName(),"Received !"+intent.getLongExtra(COUNT, -1));
	}

}
