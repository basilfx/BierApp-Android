package com.warmwit.bierapp.actions;


public class Action {
	public static final int RESULT_OK = 0;
	
	public static final int RESULT_ERROR_CONNECTION = 1;
	public static final int RESULT_ERROR_SQL = 2;
	public static final int RESULT_ERROR_SERVER = 3;
	public static final int RESULT_ERROR_INTERNAL = 4;
	public static final int RESULT_ERROR_AUTHENTICATION = 5;
	
	public static final int RESULT_ERROR_UNKNOWN = Integer.MAX_VALUE;
}
