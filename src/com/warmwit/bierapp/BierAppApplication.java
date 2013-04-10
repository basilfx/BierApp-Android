package com.warmwit.bierapp;

import android.app.Application;

import com.warmwit.bierapp.data.RemoteClient;

public class BierAppApplication extends Application {
	private RemoteClient remoteClient;
	
	public RemoteClient getRemoteClient() {
		return this.remoteClient;
	}
	
	public BierAppApplication() {
		//this.remoteClient = new RemoteClient("http://beterlijst.apps.basilfx.net/apps/bierapp/api");
		this.remoteClient = new RemoteClient("http://10.0.0.3:9000/apps/bierapp/api");
	}
	
	public static String getHostUrl() {
		return "http://10.0.0.3:9000";
	}
}
