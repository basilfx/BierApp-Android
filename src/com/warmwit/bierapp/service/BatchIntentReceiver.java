package com.warmwit.bierapp.service;

import com.google.common.base.Strings;
import com.warmwit.bierapp.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BatchIntentReceiver extends BroadcastReceiver {
	public static final String LOG_TAG = "BatchIntentReceiver";
	
	public static final String BOOT_ACTION_NAME = "android.intent.action.BOOT_COMPLETED";
	public static final String INITIAL_ACTION_NAME = "com.warmwit.bierapp.service.INITIAL_INTENT";
	public static final String PENDING_INTENT_NAME = "com.warmwit.bierapp.service.PENDING_INTENT";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = Strings.nullToEmpty(intent.getAction());
		Log.d(LOG_TAG, "Received intent: " + action);
		
		if (action.equals(BatchIntentReceiver.BOOT_ACTION_NAME)) {
			this.onInitialIntent(context, intent);
		} else if (action.equals(BatchIntentReceiver.INITIAL_ACTION_NAME)) {
			this.onInitialIntent(context, intent);
		} else if (action.equals(BatchIntentReceiver.PENDING_INTENT_NAME)) {
			this.onPendingIntent(context, intent);
		} else {
			Log.w(LOG_TAG, "Unhandled intent: " + action);
		}
	}
	
	private void onPendingIntent(Context context, Intent intent) {
		String type = Strings.nullToEmpty(intent.getStringExtra("type"));
		
		if (type.equals("service")) {
			context.startService((Intent) intent.getParcelableExtra("intent"));
		} else {
			Log.w(LOG_TAG, "Received a pending intent with unknown type: " + type);
		}
	}
	
	private void onInitialIntent(Context context, Intent intent) {
		Intent packedIntent = new Intent(context, SyncService.class);
		packedIntent.setAction(SyncService.SYNC_REQUEST);
		
		this.scheduleService(context, packedIntent, AlarmManager.INTERVAL_HALF_HOUR);
	}

	private void scheduleService(Context context, Intent intent, Long interval) {
		// Wrap future intent
		Intent nextIntent = new Intent(context, BatchIntentReceiver.class);
		nextIntent.setAction(BatchIntentReceiver.PENDING_INTENT_NAME);
		nextIntent.putExtra("intent", intent);
		nextIntent.putExtra("type", "service");
		
		// Create a pending intent
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		// Schedule it
		this.scheduleIntent(context, pendingIntent, interval);
	}
	
	private void scheduleIntent(Context context, PendingIntent pendingIntent, long interval) {
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pendingIntent);
		Log.i(LOG_TAG, "Scheduled repeating intent");
	}
}
