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
import java.util.Collection;

public class Schedule {
	private Conference conference;
	private Collection<Day> days;
	public Schedule() {
		days=new ArrayList<Day>();
	}

	public Schedule(Conference conference,Collection<Day> days) {
		this.conference=conference;
		this.days=days;
	}

	public Conference getConference() {
		return conference;
	}
	public void setConference(Conference conference) {
		this.conference = conference;
	}
	public Collection<Day> getDays() {
		return days;
	}
	public void setDays(ArrayList<Day> days) {
		this.days = days;
	}
	public void addDay(Day day) {
		this.days.add(day);
	}

	/**
	 * Get the x-th day of the conference
	 * @param number The x-th day of the conference
	 * @return Day
	 */
	public Day getDay(int number) {
		return (Day)(days.toArray()[number]);
	}

}
