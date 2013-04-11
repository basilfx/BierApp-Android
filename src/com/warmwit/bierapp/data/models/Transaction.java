package com.warmwit.bierapp.data.models;

import java.util.Date;

import com.google.common.collect.Multimap;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Transaction {

	@DatabaseField(index = true, generatedId = true)
	private int id;
	
	@DatabaseField
	private String description;
	
	@DatabaseField(canBeNull = true)
	private Date dateCreated;

	@DatabaseField
	private boolean dirty;
	
	@DatabaseField
	private boolean synced;

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isSynced() {
		return synced;
	}

	public void setSynced(boolean synced) {
		this.synced = synced;
	}
}
