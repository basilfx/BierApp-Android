package com.warmwit.bierapp.utils;

import com.warmwit.bierapp.R;
import java.util.Observable;

public class ProductInfo extends Observable {
	private int count;
	private int change;
	
	public ProductInfo(int count, int change) {
		this.count = count;
		this.change = change;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public int getChange() {
		return change;
	}
	
	public void setChange(int change) {
		this.change = change;
	}
}
