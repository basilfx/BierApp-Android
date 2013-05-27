package com.warmwit.bierapp.data;

import com.google.gson.annotations.SerializedName;


public class ApiAccessToken {
	@SerializedName("access_token")
	public String access_token;
	
	@SerializedName("scope")
	public String scope;
	
	@SerializedName("expires_in")
	public int expires_in;
	
	@SerializedName("refresh_token")
	public String refresh_token;
}
