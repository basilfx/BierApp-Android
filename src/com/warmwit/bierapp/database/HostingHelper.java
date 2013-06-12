package com.warmwit.bierapp.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.warmwit.bierapp.data.models.Hosting;

public class HostingHelper extends QueryHelper {
	
	private DatabaseHelper helper;
	
	public HostingHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(Hosting hosting) {
		checkNotNull(hosting);
		
		try {
			return this.helper.getHostingDao().create(hosting);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int update(Hosting hosting) {
		checkNotNull(hosting);
		
		try {
			return this.helper.getHostingDao().update(hosting);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int delete(Hosting hosting) {
		checkNotNull(hosting);
		
		try {
			return this.helper.getHostingDao().delete(hosting);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public Select select() {
		return new Select(this);
	}
	
	public Update update() {
		return new Update(this);
	}
	
	public static class Select {
		private QueryBuilder<Hosting, Integer> builder;
		
		private HostingHelper helper;

		private Where<Hosting, Integer> where;
		
		private boolean addedOr = false;
		
		private Select(HostingHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getHostingDao().queryBuilder();
		}
		
		private void checkWhere() {
			if (this.where == null) {
				this.where = builder.where();
			} else if (!this.addedOr) {
				this.where = this.where.and();
			}
		}
		
		public QueryBuilder<Hosting, Integer> getBuilder() {
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
		
		public Select whereActiveEq(boolean active) {
			try {
				this.checkWhere();
				this.where = this.where.eq("active", active);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereUserIdEq(int userId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("userId", userId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Hosting first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
		
		public List<Hosting> all() {
			try {
				return this.builder.query();
			} catch (SQLException e) {
				this.helper.handleException(e);
				return null;
			}
		}
		
		public List<Integer> asIntList() {
			try {
				RawRowMapper<Integer> mapper = new RawRowMapper<Integer>() {
					public Integer mapRow(String[] columnNames, String[] resultColumns) {
						return Integer.parseInt(resultColumns[0]);
					}
				};
				GenericRawResults<Integer> result = this.helper.helper.getHostingDao().queryRaw(this.builder.prepareStatementString(), mapper); 
				        
				return result.getResults();
			} catch (SQLException e) {
				this.helper.handleException(e);
				return null;
			}
		}
	}
	
	public static class Update {
		private UpdateBuilder<Hosting, Integer> builder;
		
		private HostingHelper helper;

		private Where<Hosting, Integer> where;
		
		private boolean addedOr = false;
		
		private Update(HostingHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getHostingDao().updateBuilder();
		}
		
		private void checkWhere() {
			if (this.where == null) {
				this.where = builder.where();
			} else if (!this.addedOr) {
				this.where = this.where.and();
			}
		}
		
		public Update whereUserIdEq(int userId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("userId", userId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Update setActive(boolean active) {
			try {
				this.builder.updateColumnValue("active", active);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public int execute() {
			try {
				return this.builder.update();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return -1;
		}
	}
}
