package org.fosdem.test;

import java.io.IOException;
import java.net.MalformedURLException;

import org.fosdem.util.FileUtil;

import android.test.AndroidTestCase;

public class RemoteFileCache extends AndroidTestCase {
	public void testRemoteFetch(){
		try {
			FileUtil.fetchCached("http://fosdem.org/2010/map/room/aw1105/small");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException during remote fetch");
		}
	}
}
