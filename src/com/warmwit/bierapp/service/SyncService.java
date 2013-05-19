package com.warmwit.bierapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.base.Strings;
import com.warmwit.bierapp.utils.TokenInfo;

public class SyncService extends IntentService {
	public static final String LOG_TAG = "SyncService";
	
	public static final String SYNC_COMPLETE = "com.warmwit.bierapp.service.SYNC_COMPLETE";
	public static final String SYNC_REQUEST = "com.warmwit.bierapp.service.SYNC_REQUEST";
	
    public SyncService() {
        super(SyncService.class.getName());
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = Strings.nullToEmpty(intent.getAction());
		Log.d(LOG_TAG, "Received intent: " + action);
		
		// Handle intent based on action
		if (action.equals(SyncService.SYNC_REQUEST)) {
			this.onSyncRequest();
		}
	}
	
	private void onSyncRequest() {
		int result = 0;
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		TokenInfo tokenInfo = null;//TokenInfo.createFromPreferences(preferences);
		
		// Check for token info
		if (!tokenInfo.isValid()) {
			Log.d(LOG_TAG, "Sync aborted due to invalid token info.");
		}
		
		// Renew expired tokens
		if (tokenInfo.isExpired()) {
			// Renew token
		}
		
		// Create result intent
		Intent intent = new Intent(SyncService.SYNC_COMPLETE);
		intent.putExtra("result", result);
		
		// Broadcast result intent
		this.sendBroadcast(intent);
	}
}