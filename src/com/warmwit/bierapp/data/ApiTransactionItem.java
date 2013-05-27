package com.warmwit.bierapp.data;

import com.google.gson.annotations.SerializedName;


public class ApiTransactionItem {
	@SerializedName("id")
	public int id;
	
	@SerializedName("accounted_user")
	public int accounted_user;
	
	@SerializedName("executing_user")
	public int executing_user;
	
	@SerializedName("product")
	public int product;
	
	@SerializedName("count")
	public int count;
}