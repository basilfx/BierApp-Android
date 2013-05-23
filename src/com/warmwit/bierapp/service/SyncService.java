package com.warmwit.bierapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.google.common.base.Strings;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.RemoteClient;

public class SyncService extends IntentService {
	public static final String LOG_TAG = "SyncService";
	
	public static final String LOCK_NAME = "com.warmwit.bierapp.service.SyncServiceWakeLock";
	
	public static final String SYNC_COMPLETE = "com.warmwit.bierapp.service.SYNC_COMPLETE";
	public static final String SYNC_REQUEST = "com.warmwit.bierapp.service.SYNC_REQUEST";

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
		
		// Check for token info
		if (!remoteClient.getTokenInfo().isValid()) {
			Log.i(LOG_TAG, "Sync aborted due to invalid token info.");
		}
		
		// Create result intent
		Intent intent = new Intent(SyncService.SYNC_COMPLETE);
		intent.putExtra("result", result);
		
		// Broadcast result intent
		this.sendBroadcast(intent);
	}
}