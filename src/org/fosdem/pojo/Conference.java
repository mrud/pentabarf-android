package org.fosdem.pojo;
/**
 *  This file is part of the FOSDEM Android application.
 *  http://android.fosdem.org
 *  
 *  Thisis open source software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  It is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  @author Christophe Vandeplas <christophe@vandeplas.com>
 */


import java.util.Date;

public class Conference {

	//<title>FOSDEM 2009</title>
	//  <subtitle>Free and Opensource Software Developers European Meeting</subtitle>
	//  <venue>ULB (Campus Solbosch)</venue>
	//  <city>Brussels</city>
	//  <start>2009-02-07</start>
	//  <end>2009-02-08</end>
	//  <days>2</days>
	//  <day_change>08:00</day_change>
	//  <timeslot_duration>00:15</timeslot_duration>

	private String title;
	private String subtitle;
	private String venue;
	private String city;
	private Date start;
	private Date end;
	private int days;
	private String day_change;
	private String timeslot_duration;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}	
	public String getSubtitle() {
		return subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public String getVenue() {
		return venue;
	}
	public void setVenue(String venue) {
		this.venue = venue;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public int getDays() {
		return days;
	}
	public void setDays(int days) {
		this.days = days;
	}
	public String getDay_change() {
		return day_change;
	}
	public void setDay_change(String dayChange) {
		day_change = dayChange;
	}
	public String getTimeslot_duration() {
		return timeslot_duration;
	}
	public void setTimeslot_duration(String timeslotDuration) {
		timeslot_duration = timeslotDuration;
	}
	
 
}
