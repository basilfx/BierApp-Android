package com.basilfx.bierapp.data.models;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "hosting")
public class Hosting {

	@DatabaseField(columnName = "id", index = true, generatedId = true)
	private int id;
	
	@DatabaseField(columnName = "active")
	private boolean active;
	
	@DatabaseField(columnName = "userId", foreign = true, foreignAutoRefresh = true)
	private User user;
	
	@DatabaseField(columnName = "dirty")
	private boolean dirty;
	
	@DatabaseField(columnName = "dateChanged", canBeNull = true)
	private Date dateChanged;
	
	public int getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		this.dirty = true;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		this.dirty = true;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
		this.dirty = true;
	}
	
	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
