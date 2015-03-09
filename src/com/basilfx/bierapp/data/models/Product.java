package com.basilfx.bierapp.data.models;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "product")
public class Product {
	
	@DatabaseField(columnName = "id", index = true, id = true)
	private int id;
	
	@DatabaseField(columnName = "title")
	private String title;
	
	@DatabaseField(columnName = "cost")
	private int cost;
	
	@DatabaseField(columnName = "logo")
	private String logo;
	
	@DatabaseField(columnName = "created")
	private Date created;
	
	@DatabaseField(columnName = "modified", canBeNull = true)
	private Date modified;
	
	@DatabaseField(columnName = "dirty")
	private boolean dirty;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public String toString() {
		return this.getTitle();
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

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	
}
