package com.warmwit.bierapp.data.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.warmwit.bierapp.R;

@DatabaseTable
public class HostMapping {

	@DatabaseField(index = true, generatedId = true)
	private int id;
	
	@DatabaseField(foreign = true)
	private Hosting hosting;
	
	@DatabaseField(foreign = true)
	private User host;

	@DatabaseField
	private int timesPaid;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Hosting getHosting() {
		return hosting;
	}

	public void setHosting(Hosting hosting) {
		this.hosting = hosting;
	}

	public User getHost() {
		return host;
	}

	public void setHost(User host) {
		this.host = host;
	}

	public int getTimesPaid() {
		return timesPaid;
	}

	public void setTimesPaid(int timesPaid) {
		this.timesPaid = timesPaid;
	}
}
