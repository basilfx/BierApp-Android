package com.warmwit.bierapp.data.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.warmwit.bierapp.R;

@DatabaseTable
public class UserInfo {
	
	@DatabaseField(index = true, generatedId = true)
	private int id;
	
	@DatabaseField(foreign = true)
	private User user;
	
	@DatabaseField(foreign = true)
	private Product product;
	
	@DatabaseField
	private int count;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
