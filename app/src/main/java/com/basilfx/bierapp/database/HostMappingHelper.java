package com.basilfx.bierapp.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.basilfx.bierapp.data.models.HostMapping;

public class HostMappingHelper extends QueryHelper {
	
	private DatabaseHelper helper;
	
	public HostMappingHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(HostMapping hosting) {
		checkNotNull(hosting);
		
		try {
			return this.helper.getHostMappingDao().create(hosting);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int update(HostMapping hosting) {
		checkNotNull(hosting);
		
		try {
			return this.helper.getHostMappingDao().update(hosting);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int delete(HostMapping hosting) {
		checkNotNull(hosting);
		
		try {
			return this.helper.getHostMappingDao().delete(hosting);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public Select select() {
		return new Select(this);
	}
	
	public static class Select {
		private QueryBuilder<HostMapping, Integer> builder;
		
		private HostMappingHelper helper;

		private Where<HostMapping, Integer> where;
		
		private boolean addedOr = false;
		
		private Select(HostMappingHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getHostMappingDao().queryBuilder();
		}
		
		private void checkWhere() {
			if (this.where == null) {
				this.where = builder.where();
			} else if (!this.addedOr) {
				this.where = this.where.and();
			}
		}
		
		public QueryBuilder<HostMapping, Integer> getBuilder() {
			return this.builder;
		}
		
		public Select selectIds() {
			this.builder = this.builder.selectColumns("id");
			return this;
		}
		
		public Select selectUserIds() {
			this.builder = this.builder.selectColumns("userId");
			return this;
		}
		
		public Select whereHostingIdIn(QueryBuilder<?, ?> subQuery) {
			try {
				this.checkWhere();
				this.where = this.where.in("hostingId", subQuery);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public HostMapping first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
		
		public List<HostMapping> all() {
			try {
				return this.builder.query();
			} catch (SQLException e) {
				this.helper.handleException(e);
				return null;
			}
		}
	}
}
