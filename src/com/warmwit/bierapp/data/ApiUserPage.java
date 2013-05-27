package com.warmwit.bierapp.data;

import com.google.gson.annotations.SerializedName;


public class ApiUserPage extends ApiPage {
	@SerializedName("results")
	public ApiUser[] results;
}