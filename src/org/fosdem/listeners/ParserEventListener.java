package org.fosdem.listeners;

public interface ParserEventListener {
	public static final int TAG_OPEN=0;
	public static final int TAG_CLOSED=1;
	
	public abstract void onTagEvent(String tag,int type);
}
