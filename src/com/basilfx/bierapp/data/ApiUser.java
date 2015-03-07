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
	
	@SerializedName("user_type")
	public int user_type;
	
	@SerializedName("date_changed")
	public Date date_changed;
	
	@SerializedName("xp")
	public int xp;
	
	@SerializedName("balance")
	public ApiBalance[] balance;
}
