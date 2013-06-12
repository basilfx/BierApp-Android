package com.warmwit.bierapp.data.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "userinfo")
public class UserInfo {
	
	@DatabaseField(columnName = "id", index = true, generatedId = true)
	private int id;
	
	@DatabaseField(columnName = "userId", foreign = true)
	private User user;
	
	@DatabaseField(columnName = "productId", foreign = true)
	private Product product;
	
	@DatabaseField(columnName = "count")
	private int count;

	@DatabaseField(columnName = "value")
	private Double value;
	
	@DatabaseField(columnName = "estimatedCount")
	private int estimatedCount;
	
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

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getEstimatedCount() {
		return estimatedCount;
	}

	public void setEstimatedCount(int estimatedCount) {
		this.estimatedCount = estimatedCount;
	}
}
