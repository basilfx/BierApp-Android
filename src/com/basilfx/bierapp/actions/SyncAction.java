package com.basilfx.bierapp.actions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.auth.AuthenticationException;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.basilfx.bierapp.data.Connector;
import com.basilfx.bierapp.exceptions.RetriesExceededException;
import com.basilfx.bierapp.exceptions.UnexpectedData;
import com.basilfx.bierapp.exceptions.UnexpectedStatusCode;
import com.basilfx.bierapp.exceptions.UnexpectedUrl;
import com.basilfx.bierapp.utils.LogUtils;

public class SyncAction extends Action {
	public static final String LOG_TAG = "SyncAction";

	private Context context;
	
	private Connector connector;
	
	public SyncAction(Context context, Connector connector) {
		this.context = context;
		this.connector = connector;
	}
	
	public int basicSync() {
		// Check internet connection and stop prematurely 
		NetworkInfo info = ((ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	        
		if (info == null || !info.isConnectedOrConnecting()) {
			return LogUtils.logException(LOG_TAG, new IOException("No internet connectivity"), Action.RESULT_ERROR_CONNECTION);
		}
		
		try {
			// Products
			Log.d(LOG_TAG, "Loading products");
			this.connector.loadProducts();
			
			// Users
			Log.d(LOG_TAG, "Loading users");
			this.connector.loadUsers();
			
			// Transactions
			Log.d(LOG_TAG, "Loading transactions");
			this.connector.loadTransactions();
			
			// Users info
			Log.d(LOG_TAG, "Loading user info");
			this.connector.loadUserInfo();
			
			// Stats
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
			
			if (preferences.getBoolean("summary_show", true)) {
				Calendar dayEnd = new GregorianCalendar();
				dayEnd.setTimeInMillis(preferences.getLong("summary_day_end", System.currentTimeMillis()));
				int days = Integer.parseInt(preferences.getString("summary_days", "1"));
					
				Log.d(LOG_TAG, "Loading stats");
				this.connector.loadStats(dayEnd, days);
			}
		} catch (IOException e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_CONNECTION);
		} catch (SQLException e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SQL);
		} catch (UnexpectedStatusCode e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
		} catch (UnexpectedData e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
		} catch (UnexpectedUrl e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
		} catch (RetriesExceededException e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
		} catch (AuthenticationException e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_AUTHENTICATION);
		}
			
		// No error
		return Action.RESULT_OK;
	}
}
