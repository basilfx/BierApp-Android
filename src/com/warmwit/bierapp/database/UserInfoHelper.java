package com.warmwit.bierapp.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Pair;

import com.google.common.collect.Maps;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.UserInfo;

public class UserInfoHelper extends QueryHelper {
	
	private DatabaseHelper helper;
	
	public UserInfoHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(UserInfo userInfo) {
		checkNotNull(userInfo);
		
		try {
			return this.helper.getUserInfoDao().create(userInfo);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int update(UserInfo userInfo) {
		checkNotNull(userInfo);
		
		try {
			return this.helper.getUserInfoDao().update(userInfo);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int delete(UserInfo userInfo) {
		checkNotNull(userInfo);
		
		try {
			return this.helper.getUserInfoDao().delete(userInfo);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public Select select() {
		return new Select(this);
	}
	
	public static class Select {
		private QueryBuilder<UserInfo, Integer> builder;
		
		private UserInfoHelper helper;

		private Where<UserInfo, Integer> where;
		
		private boolean addedOr = false;
		
		private Select(UserInfoHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getUserInfoDao().queryBuilder();
		}
		
		private void checkWhere() {
			if (this.where == null) {
				this.where = builder.where();
			} else if (!this.addedOr) {
				this.where = this.where.and();
			}
		}
		
		public Select whereUserIdEq(int type) {
			try {
				this.checkWhere();
				this.where = this.where.eq("userId", type);
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

		public Select whereUserIdIn(List<Integer> userIds) {
			try {
				this.checkWhere();
				this.where = this.where.in("userId", userIds);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereProductIdIn(List<Integer> productIds) {
			try {
				this.checkWhere();
				this.where = this.where.in("productId", productIds);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Map<Pair<Integer, Integer>, UserInfo> asUserProductMap() {
			HashMap<Pair<Integer, Integer>, UserInfo> result = Maps.newHashMap();
			CloseableIterator<UserInfo> iterator = null;
			
			try {
				iterator = this.builder.iterator();
				
				while (iterator.hasNext()) {
					UserInfo userInfo = iterator.next();
					Pair<Integer, Integer> key = Pair.create(
						userInfo.getUser().getId(), userInfo.getProduct().getId());
					
					result.put(key, userInfo);
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
		
		public UserInfo first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
		
		public List<UserInfo> all() {
			try {
				return this.builder.query();
			} catch (SQLException e) {
				this.helper.handleException(e);
				return null;
			}
		}
	}
}
