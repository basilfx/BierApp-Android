package com.warmwit.bierapp.data.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class Transaction extends ArrayList<TransactionItem>{

	private int id;
	
	private String description;
	
	private Date dateCreated;
	
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getTotalAmount() {
		int result = 0;
		
		// Accumulate amounts
		for (TransactionItem transaction : this) {
			result = result + transaction.getAmount();
		}
		
		// Done
		return result;
	}
	
	public int getAmount(User user) {
		int total = 0;
		
		for (TransactionItem transcation : this) {
			if (transcation.getUser().equals(user))
				total = total + 1;
		}
		
		return total;
	}
	
	public void clear(User user) {
		Iterator<TransactionItem> iterator = this.iterator();
		
		while (iterator.hasNext()) {
			TransactionItem transaction = iterator.next();
			
			if (transaction.getUser().equals(user)) {
				iterator.remove();
			}
		}
	}
	
	public Multimap<User, TransactionItem> groupByUser() {
		 return Multimaps.index(this, new Function<TransactionItem, User>() {
			@Override
			public User apply(TransactionItem item) {
				return item.getUser();
			}
		});
	}
}
