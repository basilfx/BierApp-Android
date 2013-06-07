package com.warmwit.bierapp.data;

import com.google.gson.annotations.SerializedName;


public class ApiUserInfo {
	@SerializedName("product")
	public int product;
	
	@SerializedName("count")
	public int count;
	
	@SerializedName("value")
	public double value;
	
	@SerializedName("estimated_count")
	public int estimated_count;
}
