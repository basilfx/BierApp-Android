package com.warmwit.bierapp.data.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "hosting")
public class Hosting {

	@DatabaseField(columnName = "id", index = true, generatedId = true)
	private int id;
	
	@DatabaseField(columnName = "description")
	private String description;
	
	@ForeignCollectionField(eager = true)
	private ForeignCollection<HostMapping> hosts;
	
	@DatabaseField(columnName = "user_id", foreign = true)
	private User user;
	
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
