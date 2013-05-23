package com.warmwit.bierapp.database;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.warmwit.bierapp.R;

public class QueryHelper {

	protected DatabaseHelper databaseHelper;
	
	private boolean throwAsRuntimeException;
	
	public QueryHelper(OrmLiteBaseActivity<DatabaseHelper> activity) {
		this(activity.getHelper());
	}
	
	public QueryHelper(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}
	
	protected void handleException(Exception e) {
		if (this.throwAsRuntimeException) {
			throw new RuntimeException(e);
		}
	}
	
	public QueryHelper setExceptionHandler() {
		return this;
	}
}
