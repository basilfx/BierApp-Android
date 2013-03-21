package com.warmwit.bierapp.data.models;

import com.warmwit.bierapp.R;


public class Product {
	
	private int id;
	private String title;
	private int cost;
	private int logo;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getLogo() {
		return logo;
	}

	public void setLogo(int logo) {
		this.logo = logo;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public String toString() {
		return this.title;
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
		
		return this.logo < resources.length ? resources[this.id] : R.drawable.product_beer_none;
	}
}
