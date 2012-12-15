package com.warmwit.bierapp.data.model;

import com.warmwit.bierapp.R;


public class Product {
	
	private String name;
	private int logo;
	
	public String toString() {
		return this.name;
	}	
	
	public int getBuiltinLogo() {
		int resources[] = {
			R.drawable.product_beer_grolsch,
			R.drawable.product_beer_grolsch,
			R.drawable.product_beer_hertogjan,
			R.drawable.product_beer_palm,
			R.drawable.product_beer_jupiler,
			R.drawable.product_beer_brand,
			R.drawable.product_beer_warsteiner,
		};
		
		return this.logo < resources.length ? resources[this.logo - 1] : R.drawable.product_beer_none;
	}
}
