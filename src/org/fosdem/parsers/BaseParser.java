package org.fosdem.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.fosdem.util.HttpsClient;

import android.content.Context;

public abstract class BaseParser {
	protected InputStream stream;
	
	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public BaseParser(InputStream s){
		this.stream = s;
	}
	
	public BaseParser(String url, Context ctx) throws IOException{
		DefaultHttpClient client = new HttpsClient(ctx);
		HttpGet get = new HttpGet(url);
		// Execute the GET call and obtain the response
		HttpResponse getResponse = client.execute(get);
		HttpEntity responseEntity = getResponse.getEntity();
		this.stream = responseEntity.getContent();
	}
	
}