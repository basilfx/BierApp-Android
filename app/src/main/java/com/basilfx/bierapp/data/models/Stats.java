package com.basilfx.bierapp.data.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stats")
public class Stats {
	
	@DatabaseField(columnName = "id", index = true, generatedId = true)
	private int id;
	
	@DatabaseField(columnName = "count")
	private int count;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCount() {
		return this.count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
}
