package com.warmwit.bierapp.data.model;


public class TransactionItem {
	private int id;
	
	private User user;
	private User payer;
	private Product product;
	private int amount;
	
	public int getAmount() {
		return this.amount;
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

	public void setAmount(int amount) {
		this.amount = amount;
	}
}
