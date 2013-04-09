package com.warmwit.bierapp.database;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.User;

public class ProductQuery extends QueryHelper {

	private Dao<Product, Integer> productDao;
	
	public ProductQuery(OrmLiteBaseActivity<DatabaseHelper> activity) {
		this(activity.getHelper());
	}
	
	public ProductQuery(DatabaseHelper databaseHelper) {
		super(databaseHelper);
		
		this.productDao = databaseHelper.getProductDao();
	}
	
	public List<Product> all() {
		try {
			return this.productDao.queryForAll();
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public Product byId(int id) {
		try {
			return productDao.queryForId(id);
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
}