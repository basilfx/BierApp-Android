package com.warmwit.bierapp.data;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class ApiTransaction {
	@SerializedName("id")
	public int id;
	
	@SerializedName("description")
	public String description;
	
	@SerializedName("transaction_items")
	public ApiTransactionItem[] transaction_items;
	
	@SerializedName("date_created")
	public Date date_created;
}