package com.warmwit.bierapp.utils;

import android.util.Log;

public class LogUtils {
	/**
	 * Helper to log exception and return a return value. Useful for oneliners.
	 * 
	 * @param tag Log tag
	 * @param exception Caught exception
	 * @param returnValue Integer return value
	 * @return given returnValue
	 */
	public static int logException(String tag, Exception exception, int returnValue) {
		Log.e(tag, "Caught " + exception.getClass().getName() + ": " + exception.getMessage());
		return returnValue;
	}
}
