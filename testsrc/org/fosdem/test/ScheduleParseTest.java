package org.fosdem.test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.fosdem.db.DBAdapter;
import org.fosdem.exceptions.ParserException;
import org.fosdem.parsers.ScheduleParser;
import org.fosdem.pojo.Day;
import org.fosdem.pojo.Event;
import org.fosdem.pojo.Room;
import org.fosdem.pojo.Schedule;

import android.test.AndroidTestCase;
import android.util.Log;

public class ScheduleParseTest extends AndroidTestCase{
	public static Schedule schedule;
	
	public void testScheduleParses(){
		ScheduleParser sp = null;
		try {
			sp = new ScheduleParser("http://fosdem.org/2010/schedule/xml");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to parse from URL");
		}
		assertTrue(sp!=null);
		Schedule s = null;
		
		try {
			s=sp.parse();
		} catch (ParserException e) {
			e.printStackTrace();
			fail("Failed to parse");
		}
		assertTrue(s!=null);
		assertTrue(s.getDays()!=null);
		assertTrue(s.getDays().size()==2);
		assertTrue(s.getConference().getCity().equals("Brussels"));
		assertTrue(((Day)(s.getDays().toArray()[0])).getRooms().size()>0);
		Collection<Room> rooms = ((Day)(s.getDays().toArray()[0])).getRooms();
		assertTrue(((Room)(rooms.toArray()[0])).getEvents().size()>0);
		schedule=s;
	}
	
	public void testSchedulePersistence(){
		DBAdapter db = new DBAdapter(getContext());
		db.open();
		db.clearEvents();
		assertTrue(db.getEvents().size()==0);
		
		db.persistSchedule(schedule);
		int total=0;
		for(Day day:schedule.getDays()){
			for(Room room:day.getRooms()){
				for(@SuppressWarnings("unused") Event event:room.getEvents()){
					total++;
				}
			}
		}
		List<Event> events = db.getEvents();
		assertTrue(total==events.size());
		
		db.close();
	}
	
	public void testScheduleQueryByCriteria(){
		DBAdapter db = new DBAdapter(getContext());
		db.open();
		List<Event> events = db.getEventsFiltered(null, null, new String[]{"Database"}, null, null, null,null);
		Log.v(getClass().getName(),"Number of filtered events: "+events.size());
		assertTrue(events.size()==3);
		events=db.getEventsFiltered(null, null,null, null, null, null,new String[]{"English"});
		assertTrue(events.size()==db.getEvents().size());
	}
}
