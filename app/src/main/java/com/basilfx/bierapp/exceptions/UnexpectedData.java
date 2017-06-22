package com.basilfx.bierapp.exceptions;

import org.apache.http.HttpException;

public class UnexpectedData extends HttpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6406214586601196443L;

	
	public UnexpectedData(String message) {
		super(message);
	}
}
