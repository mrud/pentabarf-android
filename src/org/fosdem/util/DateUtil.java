package org.fosdem.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static Date getDate(String date) throws ParseException{
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.parse(date);
	} 
	
	public static int convertStringToMinutes(String minutes){
		return Integer.parseInt(minutes.substring(0,1))*60+Integer.parseInt(minutes.substring(3,4));
	}
}
