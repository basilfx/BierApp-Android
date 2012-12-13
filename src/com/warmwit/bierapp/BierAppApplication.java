package com.warmwit.bierapp;

import java.util.List;

import android.app.Application;

import com.warmwit.bierapp.data.RemoteClient;
import com.warmwit.bierapp.data.model.User;

public class BierAppApplication extends Application {
	private RemoteClient remoteClient = new RemoteClient();
	
	public List<User> users;
	
	public RemoteClient getRemoteClient() {
		return this.remoteClient;
	}
}
