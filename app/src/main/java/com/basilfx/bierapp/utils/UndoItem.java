package com.basilfx.bierapp.utils;

import com.basilfx.bierapp.data.models.Product;
import com.basilfx.bierapp.data.models.User;

public class UndoItem {

	private int count;
	
	private Product product;
	
	private User user;

	public UndoItem(Product product, User user, int count) {
		this.count = count;
		this.product = product;
		this.user = user;
	}
	
	public int getCount() {
		return count;
	}

	public Product getProduct() {
		return product;
	}

	public User getUser() {
		return user;
	}
}
