package com.warmwit.bierapp;

import android.app.Application;

import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.data.RemoteClient;

public class BierAppApplication extends Application {
	private RemoteClient remoteClient;
	
	private ApiConnector apiConnector;
	
	public ApiConnector getApiConnector() {
		return this.apiConnector;
	}
	
	public BierAppApplication() {
		this.remoteClient = new RemoteClient("http://beterlijst.apps.basilfx.net/apps/bierapp/api");
		this.apiConnector = new ApiConnector(remoteClient);
	}
}
