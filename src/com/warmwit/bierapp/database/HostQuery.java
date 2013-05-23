package com.warmwit.bierapp.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.models.HostMapping;
import com.warmwit.bierapp.data.models.Hosting;
import com.warmwit.bierapp.data.models.User;

public class HostQuery extends QueryHelper {

	private Dao<Hosting, Integer> hostDao;
	private Dao<HostMapping, Integer> hostMappingDao;
	private Dao<User, Integer> userDao;
	
	public HostQuery(OrmLiteBaseActivity<DatabaseHelper> activity) {
		this(activity.getHelper());
	}
	
	public HostQuery(DatabaseHelper databaseHelper) {
		super(databaseHelper);
		
		this.hostDao = databaseHelper.getHostingDao();
		this.hostMappingDao = databaseHelper.getHostMappingDao();
		this.userDao = databaseHelper.getUserDao();
	}
	
	public void delete(User guest) {
		Hosting hosting = checkNotNull(guest.getHosting());
		
		try {
			guest.setHosting(null);
			this.userDao.update(guest);
			
			this.hostMappingDao.delete(hosting.getHosts());
			this.hostDao.delete(hosting);
		} catch (SQLException e) {
			this.handleException(e);
		}
	}
	
	public void create(User guest, List<User> users) {
		try {
			ForeignCollection<HostMapping> hosts = this.hostDao.getEmptyForeignCollection("hosts");
			
			Hosting hosting = new Hosting();
			hosting.setDescription("Meh");
			hosting.setHosts(hosts);
			
			this.hostDao.create(hosting);
			
			guest.setHosting(hosting);
			this.userDao.update(guest);
			
			for (User user : users) {
				HostMapping hostMapping = new HostMapping();
				
				hostMapping.setHost(user);
				hostMapping.setHosting(hosting);
				
				hosts.add(hostMapping);
			}
		} catch (SQLException e) {
			this.handleException(e);
		}
	}
}