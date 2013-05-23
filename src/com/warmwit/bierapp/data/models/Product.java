package com.warmwit.bierapp.data.models;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.warmwit.bierapp.R;

@DatabaseTable
public class Product {
	
	@DatabaseField(index = true, id = true)
	private int id;
	
	@DatabaseField
	private String title;
	
	@DatabaseField
	private int cost;
	
	@DatabaseField
	private String logo;
	
	@DatabaseField(canBeNull = true)
	private Date dateChanged;
	
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
		return this.title;
	}

	public Date getDateChanged() {
		return this.dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}	
}
