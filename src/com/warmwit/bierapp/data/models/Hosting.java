package com.warmwit.bierapp.data.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Hosting {

	@DatabaseField(index = true, generatedId = true)
	private int id;
	
	@DatabaseField
	private String description;
	
	@ForeignCollectionField(eager = true)
	private ForeignCollection<HostMapping> hosts;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ForeignCollection<HostMapping> getHosts() {
		return hosts;
	}

	public void setHosts(ForeignCollection<HostMapping> hosts) {
		this.hosts = hosts;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
