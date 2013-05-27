package com.warmwit.bierapp.data.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "hostmapping")
public class HostMapping {

	@DatabaseField(columnName = "id", index = true, generatedId = true)
	private int id;
	
	@DatabaseField(columnName = "hosting_id", foreign = true)
	private Hosting hosting;
	
	@DatabaseField(columnName = "host_id", foreign = true)
	private User host;

	@DatabaseField(columnName = "timesPaid")
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
