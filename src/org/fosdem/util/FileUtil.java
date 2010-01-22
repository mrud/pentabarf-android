package org.fosdem.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.fosdem.schedules.Main;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class FileUtil {
	public static final String LOG_TAG=FileUtil.class.getName();
    public static final String CACHELOCATION = "/data/data/org.fosdem/files/";

	public static Object fetch(String address) throws MalformedURLException,
			IOException {
		URL url = new URL(address);
		Object content = url.getContent();
		return content;
	}

	public static Object fetchCached(String address)
			throws MalformedURLException, IOException {
		return fetchCached(address, -1);
	}

	public static Object fetchCached(String address, int hours)
			throws MalformedURLException, IOException {
		String cacheName = md5(address);
		checkAndCreateDirectoryIfNeeded();

		File r = new File(CACHELOCATION + cacheName);

		Date d = new Date();
		long limit = d.getTime() - (1000 * 60 * 60 * hours);

		if (!r.exists() || (hours != -1 && r.lastModified() < limit)) {
			InputStream is = (InputStream) fetch(address);

			FileOutputStream fos = new FileOutputStream(CACHELOCATION
					+ cacheName);
			int nextChar;
			while ((nextChar = is.read()) != -1)
				fos.write((char) nextChar);

			fos.flush();
		}

		FileInputStream fis = new FileInputStream(CACHELOCATION + cacheName);
		return fis;
	}

	public static Drawable fetchCachedDrawable(String url)
			throws MalformedURLException, IOException {
		Log.d(LOG_TAG, "Fetching cached : " + url);
		String cacheName = md5(url);
		checkAndCreateDirectoryIfNeeded();

		File r = new File(CACHELOCATION + cacheName);
		
		// LATER download images if the timestamp on the server is new
		if (!r.exists()) {
			InputStream is = (InputStream) fetch(url);

			FileOutputStream fos = new FileOutputStream(CACHELOCATION + cacheName);
			int nextChar;
			while ((nextChar = is.read()) != -1)
				fos.write((char) nextChar);

			fos.flush();
		}

		FileInputStream fis = new FileInputStream(CACHELOCATION + cacheName);

		Drawable d = Drawable.createFromStream(fis, "src");

		return d;
	}

	public static void checkAndCreateDirectoryIfNeeded() {
		File f = new File(CACHELOCATION);
		if (!f.exists())
			f.mkdir();
	}

	public static String md5(String str) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(str.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			String md5 = hexString.toString();
			Log.v(FileUtil.class.getName(), md5);
			return md5;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
}
