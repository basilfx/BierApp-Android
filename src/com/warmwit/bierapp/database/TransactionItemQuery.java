package com.warmwit.bierapp.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;

public class TransactionItemQuery extends QueryHelper {
	private Dao<TransactionItem, Integer> transactionItemDao;
	
	public TransactionItemQuery(OrmLiteBaseActivity<DatabaseHelper> activity) {
		this(activity.getHelper());
	}
	
	public TransactionItemQuery(DatabaseHelper databaseHelper) {
		super(databaseHelper);
		
		this.transactionItemDao = databaseHelper.getTransactionItemDao();
	}
	
	public List<TransactionItem> byTransaction(Transaction transaction) {
		try {
			return this.transactionItemDao.queryForEq("transaction_id", transaction.getId());
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public int deleteByTransactionAndUser(Transaction transaction, User user) {
		checkNotNull(transaction);
		checkNotNull(user);
		
		try {
			DeleteBuilder<TransactionItem, Integer> deleteBuilder = this.transactionItemDao.deleteBuilder();
			deleteBuilder.where()
						.eq("transaction_id", transaction.getId())
						.and()
						.eq("user_id", user.getId());
			
			return deleteBuilder.delete();
		} catch (SQLException e) {
			this.handleException(e);
			return 0;
		}
	}
}
