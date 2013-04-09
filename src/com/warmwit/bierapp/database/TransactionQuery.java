package com.warmwit.bierapp.database;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.QueryBuilder;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;

public class TransactionQuery extends QueryHelper {

	private Dao<Transaction, Integer> transactionDao;
	private Dao<TransactionItem, Integer> transactionItemDao;
	
	public TransactionQuery(OrmLiteBaseActivity<DatabaseHelper> activity) {
		this(activity.getHelper());
	}
	
	public TransactionQuery(DatabaseHelper databaseHelper) {
		super(databaseHelper);
		
		this.transactionDao = databaseHelper.getTransactionDao();
		this.transactionItemDao = databaseHelper.getTransactionItemDao();
	}

	// 
	// Queries
	// 

	public List<Transaction> all() {
		try {
			return transactionDao.queryForAll();
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public List<Transaction> bySynced(boolean synced) {
		try {
			return transactionDao.queryForEq("synced", synced);
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public List<Transaction> byUser(User user) {
		checkNotNull(user);
		
		try {
			return transactionDao.queryForEq("user_id", user.getId());
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public Transaction create(String description) {
		Transaction transaction = new Transaction();
		ForeignCollection<TransactionItem> transactionItems;
		
		try {
			transactionItems =
				this.transactionDao.getEmptyForeignCollection("transactionItems");
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
			
		transaction.setDescription("Verkoop via Tablet");
		transaction.setTransactionItems(transactionItems);
		transaction.setDirty(true);
		transaction.setSynced(false);
		
		try {
			this.transactionDao.create(transaction);
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
		
		return transaction;
	}
	
	public int costByTransaction(Transaction transaction) {
		checkNotNull(transaction);
		
		try {
			QueryBuilder<TransactionItem, Integer> queryBuilder = this.transactionItemDao.queryBuilder();
			queryBuilder.selectRaw("SUM(count)")
						.where()
						.eq("transaction_id", transaction.getId());
			
			return (int) this.transactionItemDao.queryRawValue(queryBuilder.prepareStatementString());
		} catch (SQLException e) {
			this.handleException(e);
			return -1;
		}
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
			QueryBuilder<TransactionItem, Integer> queryBuilder = this.transactionItemDao.queryBuilder();
			queryBuilder.selectRaw("SUM(count)")
						.where()
						.eq("transaction_id", transaction.getId())
						.and()
						.eq("user_id", user.getId())
						.and()
						.eq("product_id", product.getId());
						
			return (int) this.transactionItemDao.queryRawValue(queryBuilder.prepareStatementString());
		} catch (SQLException e) {
			this.handleException(e);
			return -1;
		}
	}

	public void delete(Transaction transaction) {
		checkNotNull(transaction);
		checkArgument(transaction.isSynced() == false);
		
		try {
			this.transactionItemDao.delete(transaction.getTransactionItems());
			this.transactionDao.delete(transaction);
		} catch (SQLException e) {
			this.handleException(e);
		}
	}
}