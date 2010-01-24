package org.fosdem.broadcast;

public class FavoritesBroadcast {
	public static final String ACTION_FAVORITES_UPDATE = "favoritesupdate";

	public static final String EXTRA_COUNT = "count";
	public static final String EXTRA_ID = "id";
	public static final String EXTRA_TYPE = "type";

	public static final int EXTRA_TYPE_INSERT = 1;
	public static final int EXTRA_TYPE_DELETE = 2;
}
