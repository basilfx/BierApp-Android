package com.basilfx.bierapp.database;

import com.basilfx.bierapp.utils.LogUtils;

public class QueryHelper {
	public static final String LOG_TAG = "QueryHelper";
	
	protected DatabaseHelper databaseHelper;
	
	private boolean throwAsRuntimeException;
	
	protected void handleException(Exception e) {
		LogUtils.logException(LOG_TAG, e);
		
		if (this.throwAsRuntimeException) {
			throw new RuntimeException(e);
		}
	}
	
	public QueryHelper setExceptionHandler() {
		return this;
	}
}
