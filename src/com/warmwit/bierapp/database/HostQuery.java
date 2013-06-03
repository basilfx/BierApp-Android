package com.warmwit.bierapp.database;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.QueryBuilder;
import com.warmwit.bierapp.data.models.HostMapping;
import com.warmwit.bierapp.data.models.Hosting;
import com.warmwit.bierapp.data.models.User;

public class HostQuery extends QueryHelper {

	private Dao<Hosting, Integer> hostDao;
	private Dao<HostMapping, Integer> hostMappingDao;
	
	public HostQuery(OrmLiteBaseActivity<DatabaseHelper> activity) {
		this(activity.getHelper());
	}
	
	public HostQuery(DatabaseHelper databaseHelper) {
		super(databaseHelper);
		
		this.hostDao = databaseHelper.getHostingDao();
		this.hostMappingDao = databaseHelper.getHostMappingDao();
	}
	
	public Hosting byUser(User user) {
		try {
			QueryBuilder<Hosting, Integer> queryBuilder = this.hostDao.queryBuilder();
			
			queryBuilder.where()
						.eq("user_id", user.getId());
						
			return this.hostDao.queryForFirst(queryBuilder.prepare());
		} catch (SQLException e) {
			this.handleException(e);
			return null;
		}
	}
	
	public void delete(Hosting hosting) {
		try {
			this.hostMappingDao.delete(hosting.getHosts());
			this.hostDao.delete(hosting);
		} catch (SQLException e) {
			this.handleException(e);
		}
	}
	
	public void create(User user, List<User> hosts) {
		try {
			ForeignCollection<HostMapping> hostCollection = this.hostDao.getEmptyForeignCollection("hosts");
			
			Hosting hosting = new Hosting();
			hosting.setDescription("Meh");
			hosting.setHosts(hostCollection);
			hosting.setUser(user);
			
			this.hostDao.create(hosting);
			
			for (User host : hosts) {
				HostMapping hostMapping = new HostMapping();
				
				hostMapping.setHost(host);
				hostMapping.setHosting(hosting);
				
				hostCollection.add(hostMapping);
			}
		} catch (SQLException e) {
			this.handleException(e);
		}
	}
}