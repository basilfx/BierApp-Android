package com.warmwit.bierapp.data.model;

import java.util.Date;

public class TransactionItem {
	private User user;
	private User payer;
	private Product product;
	private int amount;
	
	private Date start;
	private Date end;

	public TransactionItem(User user, Product product, int amount) {
		this.user = user;
		this.product = product;
		this.amount = amount;
		this.start = new Date();
	}
	
	public int getAmount() {
		return this.amount;
	}
	
	public User getUser() {
		return this.user;
	}
}
