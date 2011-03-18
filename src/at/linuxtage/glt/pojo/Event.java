package at.linuxtage.glt.pojo;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event {

	// <event id="564">
	// <start>13:15</start>
	// <duration>00:15</duration>
	// <room>H.1302</room>
	// <tag>gnome_welcome</tag>
	// <title>Welcome to the GNOME devroom</title>
	// <subtitle></subtitle>
	// <track>GNOME</track>
	// <type>Other</type>
	// <language>English</language>
	// <abstract>Welcome to the GNOME developer room at FOSDEM 2009.</abstract>
	// <description></description>
	// <persons>
	// <person id="130">Christophe Fergeau</person>
	// </persons>
	// <links>
	// </links>
	// </event>

	public static final String LOG_TAG = Event.class.getName();
	public static final int DURATION_BLOCK = 15;
	private int id;
	private Date start;
	private int duration; // time in minutes
	private String room;
	private String tag;
	private String title;
	private String subtitle;
	private String track;
	private String type;
	private String language;
	private String abstract_description; // real xml value is abstract
	private String description;
	private int dayindex;
	private ArrayList<Person> persons = new ArrayList<Person>();
	private ArrayList<String> links = new ArrayList<String>();

	public Event() {

	}

	public Event(int id) {
		this.id = id;
	}

	public Event(String title) {
		this.title = title;
	}

	public Event(int id, Date start, int duration, String room, String tag,
			String title, String subtitle, String track, String type,
			String language, String abstract_description, String description,
			ArrayList<Person> persons, ArrayList<String> links) {
		this.id = id;
		this.start = start;
		this.duration = duration;
		this.room = room;
		this.tag = tag;
		this.title = title;
		this.subtitle = subtitle;
		this.track = track;
		this.type = type;
		this.language = language;
		this.abstract_description = abstract_description;
		this.description = description;
		this.persons = persons;
		this.links = links;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

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

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getAbstract_description() {
		return abstract_description;
	}

	public void setAbstract_description(String abstractDescription) {
		abstract_description = abstractDescription;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<Person> getPersons() {
		return persons;
	}

	public void setPersons(ArrayList<Person> persons) {
		this.persons = persons;
	}

	public void addPerson(Person person) {
		this.persons.add(person);
	}

	public List<String> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<String> links) {
		this.links = links;
	}

	public String toString() {
		return "Event: " + id + " " + title;
	}

	public int getDayindex() {
		return dayindex;
	}

	public void setDayindex(int dayindex) {
		this.dayindex = dayindex;
	}

}
