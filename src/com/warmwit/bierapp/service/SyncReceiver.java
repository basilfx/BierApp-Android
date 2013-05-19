package com.warmwit.bierapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SyncReceiver extends BroadcastReceiver {
	public static final String LOG_TAG = "SyncReceiver";

	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_TAG, "Received intent: " + intent.getAction());
	}
}
