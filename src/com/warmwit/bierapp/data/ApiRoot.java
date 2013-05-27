package com.warmwit.bierapp.data;

import com.google.gson.annotations.SerializedName;


public class ApiRoot {
	@SerializedName("transactions")
	public String transactions;
	
	@SerializedName("products")
	public String products;
	
	@SerializedName("users")
	public String users;
	
	@SerializedName("name")
	public String name;
}
