package com.warmwit.bierapp.data.models;

import java.util.Date;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "transaction")
public class Transaction {
	@DatabaseField(columnName = "id", index = true, generatedId = true)
	private int id;
	
	@DatabaseField(columnName = "remote_id", index = true, canBeNull = true)
	private Integer remoteId;
	
	@DatabaseField(columnName = "description")
	private String description;
	
	@DatabaseField(columnName = "dateCreated", canBeNull = true)
	private Date dateCreated;

	@DatabaseField(columnName = "dirty")
	private boolean dirty;

	@DatabaseField(columnName = "tag", canBeNull = true)
	private String tag;
	
	@ForeignCollectionField
	private ForeignCollection<TransactionItem> transactionItems;

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
	
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
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
	
	public ForeignCollection<TransactionItem> getTransactionItems() {
		return transactionItems;
	}
}
