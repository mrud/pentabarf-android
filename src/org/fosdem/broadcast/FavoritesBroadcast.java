package org.fosdem.broadcast;

public class FavoritesBroadcast {
	
	public static final String ACTION_FAVORITES_UPDATE = "org.fosdem.action.favorites_update";
	public static final String ACTION_FAVORITES_ALARM = "org.fosdem.action.favorites_alarm";
	public static final String ACTION_FAVORITES_INITIAL_LOAD = "org.fosdem.action.favorites_initial_load";

	public static final String EXTRA_COUNT = "count";
	public static final String EXTRA_ID = "id";
	public static final String EXTRA_TYPE = "type";

	public static final int EXTRA_TYPE_INSERT = 1;
	public static final int EXTRA_TYPE_DELETE = 2;
	public static final int EXTRA_TYPE_REMOVE_NOTIFICATION = 3;
	public static final int EXTRA_TYPE_RESCHEDULE = 4;
}
