package com.warmwit.bierapp.data.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "transactionitem")
public class TransactionItem {
	
	@DatabaseField(columnName = "id", index = true, generatedId = true)
	private int id;
	
	@DatabaseField(columnName = "remote_id", index = true, canBeNull = true)
	private Integer remoteId;
	
	@DatabaseField(columnName = "user_id", foreign = true, foreignAutoRefresh = true)
	private User user;
	
	@DatabaseField(columnName = "payer_id", foreign = true, foreignAutoRefresh = true)
	private User payer;
	
	@DatabaseField(columnName = "product_id", foreign = true, foreignAutoRefresh = true)
	private Product product;
	
	@DatabaseField(columnName = "count")
	private int count;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "transaction_id")
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

	public Integer getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(Integer remoteId) {
		this.remoteId = remoteId;
	}
}
