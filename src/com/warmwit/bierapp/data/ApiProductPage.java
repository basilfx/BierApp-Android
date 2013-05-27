package com.warmwit.bierapp.data;

import com.google.gson.annotations.SerializedName;


public class ApiProductPage extends ApiPage {
	@SerializedName("results")
	public ApiProduct[] results;
}