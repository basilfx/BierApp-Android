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
		this.remoteClient = new RemoteClient("http://10.0.0.11:8000/apps/bierapp/api");
		this.apiConnector = new ApiConnector(remoteClient);
	}
}
