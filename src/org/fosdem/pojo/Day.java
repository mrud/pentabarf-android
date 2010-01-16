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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.fosdem.schedules.Main;

import android.util.Log;

public class Day {

	private Date date;
	private int index;
	private ArrayList<Room> rooms = new ArrayList<Room>();
	
		
	public Day(){
		;
	}
	
	public Day(Date date, int index) {
		this.date = date;
		this.index = index;
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public ArrayList<Room> getRooms() {
		return rooms;
	}
	
	public Room getRoom(int number) {
		return rooms.get(number);
	}
	public Room getRoom(String name) {
		for (Room room : rooms) {
			if (0 == room.getName().compareTo(name))
				return room;
		}
		Log.e(Main.LOG_TAG, "Room '"+name+"' not found");
		return null;
	}
	
	public void setRooms(ArrayList<Room> rooms) {
		this.rooms = rooms;
	}
	public void addRoom(Room room) {
		this.rooms.add(room);
	}
	public void addRooms(Collection<Room> rooms){
		this.rooms.addAll(rooms);
	}
	
	
}
