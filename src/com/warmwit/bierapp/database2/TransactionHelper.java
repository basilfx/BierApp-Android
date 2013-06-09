package com.warmwit.bierapp.database2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.stmt.QueryBuilder;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.database.DatabaseHelper;

public class TransactionHelper {
	
	private DatabaseHelper helper;
	
	public TransactionHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int costByTransaction(Transaction transaction) {
		checkNotNull(transaction);
		
		try {
			QueryBuilder<TransactionItem, Integer> queryBuilder = this.helper.getTransactionItemDao().queryBuilder();
			queryBuilder.selectRaw("SUM(count)")
						.where()
						.eq("transaction_id", transaction.getId());
			
			return (int) this.helper.getTransactionItemDao().queryRawValue(queryBuilder.prepareStatementString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}

	public int costByUserAndProduct(TransactionItem transactionItem) {
		return this.costByUserAndProduct(
			transactionItem.getTransaction(),
			transactionItem.getUser(),
			transactionItem.getProduct()
		);
	}
	
	public int costByUserAndProduct(Transaction transaction, User user, Product product) {
		checkNotNull(transaction);
		checkNotNull(user);
		checkNotNull(product);
		
		try {
			QueryBuilder<TransactionItem, Integer> queryBuilder = this.helper.getTransactionItemDao().queryBuilder();
			queryBuilder.selectRaw("SUM(count)")
						.where()
						.eq("transaction_id", transaction.getId())
						.and()
						.eq("user_id", user.getId())
						.and()
						.eq("product_id", product.getId());
						
			return (int) this.helper.getTransactionItemDao().queryRawValue(queryBuilder.prepareStatementString());
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int create(Transaction transaction) {
		transaction.setDirty(false);
		
		try {
			return this.helper.getTransactionDao().create(transaction);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public void delete(Transaction transaction) {
		checkNotNull(transaction);
		
		try {
			//this.helper.getTransactionItemDao().delete();
			this.helper.getTransactionDao().delete(transaction);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Select select() {
		return new Select(this.helper);
	}
	
	public static class Select {
		QueryBuilder<Transaction, Integer> builder;
		
		public Select(DatabaseHelper helper) {
			this.builder = helper.getTransactionDao().queryBuilder();
		}
		
		public Select whereIdEq(int id) {
			try {
				builder.where().eq("id", id);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return this;
		}
		
		public Select whereTagEq(String tag) {
			try {
				builder.where().eq("tag", tag);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return this;
		}
		
		public Select whereRemoteIdEq(Integer remoteId) {
			try {
				if (remoteId == null) {
					builder.where().isNull("remote_id");
				} else {
					builder.where().eq("remote_id", remoteId);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return this;
		}
		
		public Select whereRemoteIdNeq(Integer remoteId) {
			try {
				if (remoteId == null) {
					builder.where().isNotNull("remote_id");
				} else {
					builder.where().not().eq("remote_id", remoteId);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return this;
		}
		
		public Transaction first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		public List<Transaction> all() {
			try {
				return this.builder.query();
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
