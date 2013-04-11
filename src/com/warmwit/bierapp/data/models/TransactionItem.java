package com.warmwit.bierapp.data.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class TransactionItem {
	
	@DatabaseField(index = true, generatedId = true)
	private int id;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private User user;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private User payer;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private Product product;
	
	@DatabaseField
	private int count;
	
	@DatabaseField(foreign = true)
	private Transaction transaction;
	
	public int getCount() {
		return this.count;
	}
	
	public User getUser() {
		return this.user;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getPayer() {
		return payer;
	}

	public void setPayer(User payer) {
		this.payer = payer;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
}
