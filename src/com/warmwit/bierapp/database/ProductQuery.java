package com.warmwit.bierapp.database;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
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
	
	public boolean shouldSync(int id, Date dateChanged) {
		try {
			QueryBuilder<Product, Integer> queryBuilder = this.productDao.queryBuilder();
			
			queryBuilder.selectRaw("COUNT(*)")
						.where()
						.eq("id", id)
						.and()
						.eq("dateChanged", dateChanged);
						
			return (int) this.productDao.queryRawValue(queryBuilder.prepareStatementString()) == 0;
		} catch (SQLException e) {
			this.handleException(e);
			return true;
		}
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