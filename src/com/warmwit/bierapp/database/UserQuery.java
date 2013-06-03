package com.warmwit.bierapp.database;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.warmwit.bierapp.data.models.Hosting;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.data.models.UserInfo;

public class UserQuery extends QueryHelper {

	private Dao<User, Integer> userDao;
	private Dao<UserInfo, Integer> userInfoDao;
	private Dao<Hosting, Integer> hostingDao;
	
	public UserQuery(OrmLiteBaseActivity<DatabaseHelper> activity) {
		this(activity.getHelper());
	}
	
	public UserQuery(DatabaseHelper databaseHelper) {
		super(databaseHelper);
		
		this.userDao = databaseHelper.getUserDao();
		this.userInfoDao = databaseHelper.getUserInfoDao();
		this.hostingDao = databaseHelper.getHostingDao();
	}
	
	public List<User> all() {
		try {
			return userDao.queryForAll();
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public User byId(int id) {
		try {
			return userDao.queryForId(id);
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public List<User> byType(int type) {
		try {
			return userDao.queryForEq("type", type);
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public boolean shouldSync(int id, Date dateChanged) {
		try {
			QueryBuilder<User, Integer> queryBuilder = this.userDao.queryBuilder();
			
			queryBuilder.selectRaw("COUNT(*)")
						.where()
						.eq("id", id)
						.and()
						.eq("dateChanged", dateChanged);
						
			return (int) this.userDao.queryRawValue(queryBuilder.prepareStatementString()) == 0;
		} catch (SQLException e) {
			this.handleException(e);
			return true;
		}
	}
	
	public List<User> guests() {
		return this.byType(User.GUEST);
	}
	
	public List<User> inhabitants() {
		return this.byType(User.INHABITANT);
	}
	
	public List<User> activeGuests() {
		try {
			QueryBuilder<User, Integer> queryBuilder = this.userDao.queryBuilder();
			QueryBuilder<Hosting, Integer> subQueryBuilder = this.hostingDao.queryBuilder();
			
			subQueryBuilder.selectColumns("user_id");
			queryBuilder.where()
						.eq("type", User.GUEST)
						.and()
						.in("id", subQueryBuilder);
			
			return userDao.query(queryBuilder.prepare());
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public List<User> inactiveGuests() {
		try {
			QueryBuilder<User, Integer> queryBuilder = this.userDao.queryBuilder();
			QueryBuilder<Hosting, Integer> subQueryBuilder = this.hostingDao.queryBuilder();
			
			subQueryBuilder.selectColumns("user_id");
			queryBuilder.where()
						.eq("type", User.GUEST)
						.and()
						.notIn("id", subQueryBuilder);
			
			return userDao.query(queryBuilder.prepare());
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public int count() {
		try {
			return (int) userDao.countOf();
		} catch (SQLException e) {
			this.handleException(e);
			return 0;
		}
	}
	
	public UserInfo userProductInfo(User user, Product product) {
		try {
			QueryBuilder<UserInfo, Integer> queryBuilder = this.userInfoDao.queryBuilder();
			
			queryBuilder.where()
						.eq("product_id", product.getId())
						.and()
						.eq("user_id", user.getId());
			
			return this.userInfoDao.queryForFirst(queryBuilder.prepare());
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
}
