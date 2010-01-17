package org.fosdem.util;

import java.util.ArrayList;
import java.util.List;

import org.fosdem.pojo.Person;

import android.test.AndroidTestCase;

public class StringUtilTest extends AndroidTestCase {
	
	public void testPersonsToString() {
		
		final List<Person> persons = new ArrayList<Person>();
		assertEquals("Empty list should return empty string", "", StringUtil.personsToString(persons));
		
		persons.add( new Person(1, "Name1"));
		assertEquals("One person should return just the name of that one person", "Name1", StringUtil.personsToString(persons));		
		
		persons.add( new Person(2, "Name2"));
		assertEquals("Two persons should be listed as the two names separated by a comma and a space", "Name1, Name2", StringUtil.personsToString(persons));		
	}

	public void testNiceify() {
		
		assertEquals("Single returns should be disposed of and have a space", "a b c", StringUtil.niceify("a\nb\nc"));
		assertEquals("Double returns should be kept, with one space in front", "a \n\nb \n\nc", StringUtil.niceify("a\n\nb\n\nc"));		
		assertEquals("Double returns or more should be kept, with one space in front", "a \n\nb \n\n\nc", StringUtil.niceify("a\n\nb\n\n\nc"));		
		assertEquals("Multiple spaces or tabs should be converted to one space", "a b c d e", StringUtil.niceify("a\tb  c \t d\t \te"));
		assertEquals("Multiple spaces or tabs should be converted to one space, together with single returns", "a b c d e f g", StringUtil.niceify("a\n\tb \n c \t d\t \te  f g"));
		assertEquals("Should all work together nicely", "a \n\nb c d e f g", StringUtil.niceify("a\n\nb\nc \t d\t \te f \n \n g"));
	}
	
}
