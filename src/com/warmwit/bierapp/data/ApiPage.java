package com.warmwit.bierapp.data;

import com.google.gson.annotations.SerializedName;


public class ApiPage {
	@SerializedName("count")
	public int count;
	
	@SerializedName("next")
	public String next;
	
	@SerializedName("previous")
	public String previous;

}
