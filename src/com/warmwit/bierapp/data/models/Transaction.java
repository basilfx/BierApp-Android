package com.warmwit.bierapp.data.models;

import java.util.Date;

import com.google.common.base.Strings;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "transaction")
public class Transaction {
	public static final int ERROR_NONE = 0;
	public static final int ERROR_DESCRIPTION_EMPTY = 1;

	@DatabaseField(columnName = "id", index = true, generatedId = true)
	private int id;
	
	@DatabaseField(columnName = "description")
	private String description;
	
	@DatabaseField(columnName = "dateCreated", canBeNull = true)
	private Date dateCreated;

	@DatabaseField(columnName = "dirty")
	private boolean dirty;
	
	@DatabaseField(columnName = "synced")
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
	
	public int isValid() {
		if (Strings.nullToEmpty(this.description).isEmpty()) {
			return ERROR_DESCRIPTION_EMPTY;
		}
		
		return ERROR_NONE;
	}
}
