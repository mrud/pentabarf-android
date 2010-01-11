package org.fosdem.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
	
	public BaseParser(String url) throws IOException{
		URL urlObj;
		urlObj = new URL(url);
		this.stream = (InputStream)urlObj.getContent();
	}
	
}