package com.basilfx.bierapp.exceptions;

import org.apache.http.HttpException;

public class UnexpectedStatusCode extends HttpException{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2261400615322872338L;

	public UnexpectedStatusCode(int statusCode) {
		super("Unexpected HTTP status code " + statusCode);
	}
	
	public UnexpectedStatusCode(int statusCode, int expected) {
		super("Unexpected HTTP status code " + statusCode + ". Expected " + expected);
	}
}
