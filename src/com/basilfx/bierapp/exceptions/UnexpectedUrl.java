package com.basilfx.bierapp.exceptions;

import org.apache.http.HttpException;

public class UnexpectedUrl extends HttpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5550666468972618481L;
	
	public UnexpectedUrl(String message) {
		super(message);
	}
}
