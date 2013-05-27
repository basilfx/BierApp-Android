package com.warmwit.bierapp.data;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class ApiProduct {
	@SerializedName("id")
	public int id;
	
	@SerializedName("title")
	public String title;
	
	@SerializedName("logo")
	public String logo;
	
	@SerializedName("cost")
	public int cost;
	
	@SerializedName("date_changed")
	public Date date_changed;
}
