package com.basilfx.bierapp.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.auth.AuthenticationException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.base.Strings;
import com.basilfx.bierapp.BierAppApplication;
import com.basilfx.bierapp.data.Connector;
import com.basilfx.bierapp.data.RemoteClient;
import com.basilfx.bierapp.database.DatabaseHelper;
import com.basilfx.bierapp.exceptions.RetriesExceededException;
import com.basilfx.bierapp.exceptions.UnexpectedData;
import com.basilfx.bierapp.exceptions.UnexpectedStatusCode;
import com.basilfx.bierapp.exceptions.UnexpectedUrl;
import com.basilfx.bierapp.tasks.SaveTransactionTask;
import com.basilfx.bierapp.utils.OrmLiteBaseIntentService;

public class SyncService extends OrmLiteBaseIntentService<DatabaseHelper> {
	public static final String LOG_TAG = "SyncService";
	
	public static final String LOCK_NAME = "com.basilfx.bierapp.service.SyncServiceWakeLock";
	
	public static final String SYNC_COMPLETE = "com.basilfx.bierapp.service.SYNC_COMPLETE";
	public static final String SYNC_REQUEST = "com.basilfx.bierapp.service.SYNC_REQUEST";

	private static PowerManager.WakeLock lock;

	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lock == null) {
			PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			lock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SyncService.LOCK_NAME);
			lock.setReferenceCounted(true);
		}

		return lock;
	}
	
    public SyncService() {
        super(SyncService.class.getName());
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		// Read action from intent
		String action = Strings.nullToEmpty(intent.getAction());
		Log.d(LOG_TAG, "Received intent: " + action);
		
		if (!action.equals(SyncService.SYNC_REQUEST)) {
			return;
		}
		
		// Acquire wake lock
		Log.d(LOG_TAG, "Acquiring wake lock");
		PowerManager.WakeLock lock = SyncService.getLock(this);
		lock.acquire();
		
		try {
			this.onSyncRequest();
		} finally {
			// Release wake lock
			if (lock.isHeld()) {
				Log.d(LOG_TAG, "Releasing wake lock");
				lock.release();
			} else {
				Log.d(LOG_TAG, "Wake lock not held");
			}
		}
	}
	
	private void onSyncRequest() {
		int result = 0;
		
		RemoteClient remoteClient = BierAppApplication.getRemoteClient();
		Connector connector = new Connector(remoteClient, this.getHelper());
		
		// Check for token info
		if (!remoteClient.getTokenInfo().isValid()) {
			Log.i(LOG_TAG, "Sync aborted due to invalid token info.");
		}
		
		// Sync stats
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
		if (preferences.getBoolean("summary_show", true)) {
			Calendar dayEnd = new GregorianCalendar();
			dayEnd.setTimeInMillis(preferences.getLong("summary_day_end", System.currentTimeMillis()));
			int days = Integer.parseInt(preferences.getString("summary_days", "1"));
				
			try {
				Log.d(LOG_TAG, "Loading stats");
				connector.loadStats(dayEnd, days);
			} catch (AuthenticationException e) {
				e.printStackTrace();
			} catch (UnexpectedStatusCode e) {
				e.printStackTrace();
			} catch (UnexpectedData e) {
				e.printStackTrace();
			} catch (UnexpectedUrl e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (RetriesExceededException e) {
				e.printStackTrace();
			}
		}
		
		// Create result intent
		Intent intent = new Intent(SyncService.SYNC_COMPLETE);
		intent.putExtra("result", result);
		
		// Broadcast result intent
		this.sendBroadcast(intent);
	}
}