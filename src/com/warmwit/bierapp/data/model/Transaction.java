package com.warmwit.bierapp.data.model;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class Transaction extends ArrayList<TransactionItem>{

	public void add(User user, Product product) {
		this.add(user, product, 1);
	}
	
	public void add(User user, Product product, int amount) {
		this.add(new TransactionItem(user, product, amount));
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
