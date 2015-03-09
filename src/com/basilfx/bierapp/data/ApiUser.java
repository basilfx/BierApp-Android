package com.basilfx.bierapp.data;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class ApiUser {
	@SerializedName("id")
	public int id;
	
	@SerializedName("first_name")
	public String first_name;
	
	@SerializedName("last_name")
	public String last_name;
	
	@SerializedName("avatar")
	public String avatar;
	
	@SerializedName("role")
	public int role;
	
	@SerializedName("created")
	public Date created;
	
	@SerializedName("modified")
	public Date modified;
	
	@SerializedName("xp")
	public int xp;
	
	@SerializedName("balance")
	public ApiBalance[] balance;
}
