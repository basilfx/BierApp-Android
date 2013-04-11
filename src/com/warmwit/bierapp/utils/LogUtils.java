package com.warmwit.bierapp.utils;

import android.util.Log;

public class LogUtils {
	/**
	 * Internal helper to log exception and return a return value.
	 * 
	 * @param exception Caught exception
	 * @param returnValue Integer return value
	 * @return given returnValue
	 */
	public static int logException(String tag, Exception exception, int returnValue) {
		Log.e(tag, "Caught exception: " + exception.getMessage());
		return returnValue;
	}
}
