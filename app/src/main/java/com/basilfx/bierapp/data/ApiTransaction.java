package com.basilfx.bierapp.data;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class ApiTransaction {
	@SerializedName("id")
	public int id;
	
	@SerializedName("description")
	public String description;
	
	@SerializedName("transaction_items")
	public ApiTransactionItem[] transaction_items;
	
	@SerializedName("created")
	public Date created;
	
	@SerializedName("modified")
	public Date modified;
}