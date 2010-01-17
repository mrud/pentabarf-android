package org.fosdem.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class DateUtil {
	public static Date getDate(String date) throws ParseException {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date d = formatter.parse(date);
		Log.v(DateUtil.class.getName(), d.toString());
		return d;
	}

	public static int convertStringToMinutes(String minutes) {
		return Integer.parseInt(minutes.substring(0, 2)) * 60
				+ Integer.parseInt(minutes.substring(3, 5));
	}
}
