package com.warmwit.bierapp.database2;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.stmt.QueryBuilder;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database2.TransactionHelper.Select;

public class TransactionItemHelper {
	private DatabaseHelper helper;
	
	public TransactionItemHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(TransactionItem transactionItem) {
		try {
			return this.helper.getTransactionItemDao().create(transactionItem);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public int update(TransactionItem transactionItem) {
		try {
			return this.helper.getTransactionItemDao().update(transactionItem);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public Select select() {
		return new Select(this.helper);
	}
	
	public static class Select {
		QueryBuilder<TransactionItem, Integer> builder;
		
		public Select(DatabaseHelper helper) {
			this.builder = helper.getTransactionItemDao().queryBuilder();
		}
		
		public Select whereIdEq(int id) {
			try {
				builder.where().eq("id", id);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return this;
		}
		
		public Select whereTransactionIdEq(int transactionId) {
			try {
				builder.where().eq("transaction_id", transactionId);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return this;
		}
		
		public TransactionItem first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		public List<TransactionItem> all() {
			try {
				return this.builder.query();
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
