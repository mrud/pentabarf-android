package org.fosdem.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fosdem.exceptions.ParserException;
import org.fosdem.listeners.ParserEventListener;
import org.fosdem.pojo.Conference;
import org.fosdem.pojo.Day;
import org.fosdem.pojo.Event;
import org.fosdem.pojo.Room;
import org.fosdem.pojo.Schedule;
import org.fosdem.util.DateUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

//TODO: persons and links need to be added to the parser.
public class ScheduleParser extends BaseParser {

	public static final String LOGTAG=ScheduleParser.class.getName();
	
	//Conference
	public static final String SCHEDULE="schedule";
	public static final String CONFERENCE="conference";
	public static final String SUBTITLE="subtitle";
	public static final String VENUE="venue";
	public static final String CITY="city";
	public static final String START="start";
	public static final String END="end";
	public static final String DAYS="days";
	public static final String DAY_CHANGE="day_change";
	public static final String TIMESLOT_DURATION="timeslot_duration";

	//Day
	public static final String DAY="day";
	public static final String INDEX="index";
	public static final String DATE="date";
	
	//Room
	public static final String ROOM="room";
	public static final String NAME="name";
	
	//Event
	public static final String EVENT="event";
	public static final String ID="id";
	public static final String DURATION="duration";
	public static final String TAG="tag";
	public static final String TITLE="title";
	public static final String TRACK="track";
	public static final String TYPE="type";
	public static final String LANGUAGE="language";
	public static final String ABSTRACT="abstract";
	public static final String DESCRIPTION="description";
	public static final String PERSONS="persons";
	public static final String LINKS="links";
	
	//Person
	public static final String PERSON="person";
	
	//Links
	public static final String LINK="link";
	public static final String HREF="href";
	
	public ScheduleParser(InputStream s) {
		super(s);
	}

	public ScheduleParser(String url) throws IOException {
		super(url);
	}

	public Schedule parse() throws ParserException{
		try{
			List<Day> days=new ArrayList<Day>();
			Event event=null;
			List<Event> events=null;
			List<Room> rooms=null;
			Conference conference=null;
			
			Day day=null;
			Room room=null;
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser xpp = factory.newPullParser();
	
	        xpp.setInput(stream, "UTF-8");
	        
	        @SuppressWarnings("unused")
			String content=null;
	        
	        int eventType = xpp.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
		         if(eventType == XmlPullParser.START_TAG) {
		             launchEvent(xpp.getName(), ParserEventListener.TAG_OPEN);
		        	 if(xpp.getName().equals(CONFERENCE)){
		            	 xpp.next();
		        		 conference=parseConference(xpp);
		             }
		             else if(xpp.getName().equals(DAY)){
		            	 Date date=null;
		            	 int idx=0;
		            	 for(int i = 0 ; i < xpp.getAttributeCount();i++){
		            		if(xpp.getAttributeName(i).equals(DATE)){
		            			date=DateUtil.getDate(xpp.getAttributeValue(i));
		            		}
		            		else if(xpp.getAttributeName(i).equals(INDEX)){
		            			idx=Integer.parseInt(xpp.getAttributeValue(i));
		            		}
		            	 }
		            	 day=new Day(date,idx);
		            	 rooms=new ArrayList<Room>();
		            	 
		             }
		             else if(xpp.getName().equals(ROOM)){
		            	 String name=null;
		            	 for(int i=0;i<xpp.getAttributeCount();i++){
		            		 if(xpp.getAttributeName(i).equals(NAME)){
		            			 name=xpp.getAttributeValue(i);
		            		 }
		            	 }
		            	 room = new Room(name);
		            	 events = new ArrayList<Event>();
		             }
		             else if(xpp.getName().equals(EVENT)){
		            	 int id=-1;
		            	 for(int i = 0 ; i < xpp.getAttributeCount();i++){
		            		 if(xpp.getAttributeName(i).equals(ID)){
		            			 id=Integer.parseInt(xpp.getAttributeValue(i));
		            		 }
		            	 }
		            	 xpp.next();
		            	 Event e = parseEvent(xpp,day.getDate());
		            	 e.setId(id);
		            	 room.addEvent(e);
		             }
		        	 content=null;
		         } else if(eventType == XmlPullParser.END_TAG) {
		        	 launchEvent(xpp.getName(), ParserEventListener.TAG_CLOSED);
		        	 if(xpp.getName().equals(DAY)){
		            	 day.addRooms(rooms);
		            	 days.add(day);
		            	 rooms=null;
		             }
		             else if(xpp.getName().equals(ROOM)){
		            	 room.addEvents(events);
		            	 rooms.add(room);
		            	 events=null;
		             }
		             else if(xpp.getName().equals(EVENT)){
		            	 events.add(event);
		            	 event=null;
		             }
		         } else if(eventType == XmlPullParser.TEXT) {
		             content=xpp.getText();
		         }
		         eventType = xpp.next();
	        }
	        return new Schedule(conference,days);
        }
		catch(Exception e){
			throw new ParserException(e);
		}
		
	
	}
	
	public Conference parseConference(XmlPullParser xpp) throws XmlPullParserException, IOException, ParseException{
		String content = null;
		int eventType = xpp.getEventType();
		Conference conference = new Conference();
        while (eventType != XmlPullParser.END_DOCUMENT) {
	         if(eventType == XmlPullParser.START_TAG) {
	        	 launchEvent(xpp.getName(), ParserEventListener.TAG_OPEN);
	        	 content=null;
	        	 
	         } else if(eventType == XmlPullParser.END_TAG) {
	        	 launchEvent(xpp.getName(), ParserEventListener.TAG_CLOSED);
	        	 if(xpp.getName().equals(TITLE)){
	            	 conference.setTitle(content);
	             }
	             else if(xpp.getName().equals(SUBTITLE)){
	            	 conference.setSubtitle(content);
	             }
	             else if(xpp.getName().equals(VENUE)){
	            	 conference.setVenue(content);
	             }
	             else if(xpp.getName().equals(CITY)){
	            	 conference.setCity(content);
	             }
	             else if(xpp.getName().equals(START)){
	            	 conference.setStart(DateUtil.getDate(content));
	             }
	             else if(xpp.getName().equals(END)){
	            	 conference.setEnd(DateUtil.getDate(content));
	             }
	             else if(xpp.getName().equals(DAYS)){
	            	 conference.setDays(Integer.parseInt(content));
	             }
	             else if(xpp.getName().equals(DAY_CHANGE)){
	            	 conference.setDay_change(content);
	             }
	             else if(xpp.getName().equals(TIMESLOT_DURATION)){
	            	 conference.setTimeslot_duration(content);
	             }
	             else if(xpp.getName().equals(CONFERENCE)){
	            	 return conference;
	             }
	         } else if(eventType == XmlPullParser.TEXT) {
	             content=xpp.getText();
	         }
	         eventType = xpp.next();
        }
        return null;
	}
	
	public Event parseEvent(XmlPullParser xpp,Date date) throws XmlPullParserException, IOException, ParseException{
		String content = null;
		int eventType = xpp.getEventType();
		Event event = new Event();
        while (eventType != XmlPullParser.END_DOCUMENT) {
	         if(eventType == XmlPullParser.START_TAG) {
	        	 launchEvent(xpp.getName(), ParserEventListener.TAG_OPEN);
	        	 if(xpp.getName().equals(EVENT)){
	            	 int id=0;
	        		 for(int i = 0;i<xpp.getAttributeCount();i++){
	            		 if(xpp.getAttributeName(i).equals(ID)){
	            			 id=Integer.parseInt(xpp.getAttributeValue(i));
	            		 }
	            	 }
	        		 event.setId(id);
	             }
	        	 content=null;
	         } else if(eventType == XmlPullParser.END_TAG) {
	        	 launchEvent(xpp.getName(), ParserEventListener.TAG_CLOSED);
	        	 if(xpp.getName().equals(START)){
	            	 Date d = new Date(
	            			 date.getYear(),
	            			 date.getMonth(),
	            			 date.getDay(),
	            			 Integer.parseInt(content.substring(0, 2)),
	            			 Integer.parseInt(content.substring(3, 5)));
	            	 event.setStart(d);
	             }
	             else if(xpp.getName().equals(DURATION)){
	            	 event.setDuration(DateUtil.convertStringToMinutes(content));
	             }
	             else if(xpp.getName().equals(ROOM)){
	            	 event.setRoom(content);
	             }
	             else if(xpp.getName().equals(TAG)){
	            	 event.setTag(content);
	             }
	             else if(xpp.getName().equals(TITLE)){
	            	 event.setTitle(content);
	             }
	             else if(xpp.getName().equals(SUBTITLE)){
	            	 event.setSubtitle(content);
	             }
	             else if(xpp.getName().equals(TRACK)){
	            	 event.setTrack(content);
	             }
	             else if(xpp.getName().equals(TYPE)){
	            	 event.setType(content);
	             }
	             else if(xpp.getName().equals(LANGUAGE)){
	            	 event.setLanguage(content);
	             }
	             else if(xpp.getName().equals(ABSTRACT)){
	            	 event.setAbstract_description(content);
	             }
	             else if(xpp.getName().equals(DESCRIPTION)){
	            	 event.setDescription(content);
	             }
	             else if(xpp.getName().equals(EVENT)){
	            	 return event;
	             }
	         } else if(eventType == XmlPullParser.TEXT) {
	             content=xpp.getText();
	         }
	         eventType = xpp.next();
        }
        return null;
	}
	
	protected List<ParserEventListener> listeners=new ArrayList<ParserEventListener>();
	
	public void addTagEventListener(ParserEventListener pel){
		listeners.add(pel);
	}
	
	public void removeTagEventListener(ParserEventListener pel){
		listeners.remove(pel);
	}
	
	protected void launchEvent(String tag,int type){
		for(ParserEventListener parser:listeners){
			parser.onTagEvent(tag, type);
		}
	}
}
