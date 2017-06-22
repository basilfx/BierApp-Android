package com.basilfx.bierapp.data;

import com.google.gson.annotations.SerializedName;


public class ApiTransactionPage extends ApiPage {
	@SerializedName("results")
	public ApiTransaction[] results;
}