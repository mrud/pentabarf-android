package org.fosdem.exceptions;

public class ParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4939457288603675955L;
	
	public ParserException(Exception e){
		this.initCause(e);
	}

}
