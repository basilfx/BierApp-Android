package com.basilfx.bierapp.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.basilfx.bierapp.data.models.User;

public class UserHelper extends QueryHelper {
	
	private DatabaseHelper helper;
	
	public UserHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(User user) {
		checkNotNull(user);
		
		try {
			return this.helper.getUserDao().create(user);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int update(User user) {
		checkNotNull(user);
		
		try {
			return this.helper.getUserDao().update(user);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int delete(User user) {
		checkNotNull(user);
		
		try {
			return this.helper.getUserDao().delete(user);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public Select select() {
		return new Select(this);
	}
	
	public static class Select {
		private QueryBuilder<User, Integer> builder;
		
		private UserHelper helper;

		private Where<User, Integer> where;
		
		private boolean addedOr = false;
		
		private Select(UserHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getUserDao().queryBuilder();
		}
		
		private void checkWhere() {
			if (this.where == null) {
				this.where = builder.where();
			} else if (!this.addedOr) {
				this.where = this.where.and();
			}
		}
		
		public Select whereRoleEq(int role) {
			try {
				this.checkWhere();
				this.where = this.where.eq("role", role);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereRoleIn(List<Integer> roles) {
			try {
				this.checkWhere();
				this.where = this.where.in("role", roles);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereIdIn(List<Integer> ids) {
			try {
				this.checkWhere();
				this.where = this.where.in("id", ids);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}

		public Select whereIdIn(QueryBuilder<?, ?> subQuery) {
			try {
				this.checkWhere();
				this.where = this.where.in("id", subQuery);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereIdNotIn(QueryBuilder<?, ?> subQuery) {
			try {
				this.checkWhere();
				this.where = this.where.notIn("id", subQuery);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Map<Integer, User> asMap() {
			HashMap<Integer, User> result = Maps.newHashMap();
			CloseableIterator<User> iterator = null;
			
			try {
				iterator = this.builder.iterator();
				
				while (iterator.hasNext()) {
					User user = iterator.next();
					result.put(user.getId(), user);
				}
			} catch (SQLException e) {
				this.helper.handleException(e);
			} finally {
				if (iterator != null) {
					iterator.closeQuietly();
				}
			}
			
			return result;
		}
		
		public User first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
		
		public List<User> all() {
			try {
				return this.builder.query();
			} catch (SQLException e) {
				this.helper.handleException(e);
				return null;
			}
		}
	}
}
