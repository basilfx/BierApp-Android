package com.basilfx.bierapp.data.models;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "transaction")
public class Transaction {
	@DatabaseField(columnName = "id", index = true, generatedId = true)
	private int id;
	
	@DatabaseField(columnName = "remoteId", index = true, canBeNull = true)
	private Integer remoteId;
	
	@DatabaseField(columnName = "description")
	private String description;
	
	@DatabaseField(columnName = "created")
	private Date created;
	
	@DatabaseField(columnName = "modified", canBeNull = true)
	private Date modified;

	@DatabaseField(columnName = "dirty")
	private boolean dirty;

	@DatabaseField(columnName = "tag", canBeNull = true)
	private String tag;

	public int getId() {
		return id;
	}
	
	public int getRemoteId() {
		return this.remoteId;
	}
	
	public void setRemoteId(int remoteId) {
		this.remoteId = remoteId;
		this.dirty = true;
	}
	
	public Date getCreated() {
		return this.created;
	}
	
	public void setCreated(Date created) {
		this.created = created;
		this.dirty = true;
	}
	
	public Date getModified() {
		return this.modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
		this.dirty = true;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		this.dirty = true;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isSynced() {
		return this.remoteId > 0;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
		this.dirty = true;
	}
}
