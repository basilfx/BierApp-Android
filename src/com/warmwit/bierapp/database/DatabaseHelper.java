package com.warmwit.bierapp.database;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.warmwit.bierapp.data.models.HostMapping;
import com.warmwit.bierapp.data.models.Hosting;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.data.models.UserInfo;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION = 35;

	private Dao<User, Integer> userDao;
	private Dao<UserInfo, Integer> userInfoDao;
	private Dao<Product, Integer> productDao;
	private Dao<Transaction, Integer> transactionDao;
	private Dao<TransactionItem, Integer> transactionItemDao;
	private Dao<Hosting, Integer> hostingDao;
	private Dao<HostMapping, Integer> hostMappingDao;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, UserInfo.class);
			TableUtils.createTable(connectionSource, Product.class);
			TableUtils.createTable(connectionSource, Transaction.class);
			TableUtils.createTable(connectionSource, TransactionItem.class);
			TableUtils.createTable(connectionSource, Hosting.class);
			TableUtils.createTable(connectionSource, HostMapping.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, UserInfo.class, true);
			TableUtils.dropTable(connectionSource, Product.class, true);
			TableUtils.dropTable(connectionSource, Transaction.class, true);
			TableUtils.dropTable(connectionSource, TransactionItem.class, true);
			TableUtils.dropTable(connectionSource, Hosting.class, true);
			TableUtils.dropTable(connectionSource, HostMapping.class, true);
			
			this.onCreate(sqliteDatabase, connectionSource);
		} catch (SQLException e) {
			Log.e(
				DatabaseHelper.class.getName(),
				"Unable to upgrade database from version " + oldVer + " to new "+ newVer, 
				e
			);
		}
	}
	
	//
	// DAO
	//
	
	public Dao<User, Integer> getUserDao() {
		if (this.userDao == null) {
			try {
				this.userDao = this.getDao(User.class);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		return this.userDao;
	}

	public Dao<UserInfo, Integer> getUserInfoDao() {
		if (this.userInfoDao == null) {
			try {
				this.userInfoDao = this.getDao(UserInfo.class);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		return this.userInfoDao;
	}
	
	public Dao<Product, Integer> getProductDao() {
		if (this.productDao == null) {
			try {
				this.productDao = this.getDao(Product.class);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		return this.productDao;
	}
	
	public Dao<Transaction, Integer> getTransactionDao() {
		if (this.transactionDao == null) {
			try {
				this.transactionDao = this.getDao(Transaction.class);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		return this.transactionDao;
	}
	
	public Dao<TransactionItem, Integer> getTransactionItemDao() {
		if (this.transactionItemDao == null) {
			try {
				this.transactionItemDao = this.getDao(TransactionItem.class);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		return this.transactionItemDao;
	}
	
	public Dao<Hosting, Integer> getHostingDao() {
		if (this.hostingDao == null) {
			try {
				this.hostingDao = this.getDao(Hosting.class);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		return this.hostingDao;
	}
	
	public Dao<HostMapping, Integer> getHostMappingDao() {
		if (this.hostMappingDao == null) {
			try {
				this.hostMappingDao = this.getDao(HostMapping.class);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		return this.hostMappingDao;
	}
}