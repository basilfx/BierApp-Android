package com.warmwit.bierapp.data;

import com.google.gson.annotations.SerializedName;


public class ApiUserInfo {
	@SerializedName("product")
	public int product;
	
	@SerializedName("count")
	public int count;
}
