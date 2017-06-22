package com.basilfx.bierapp.database;

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
import com.basilfx.bierapp.data.models.Balance;

public class ProductBalanceHelper extends QueryHelper {
	
	private DatabaseHelper helper;
	
	public ProductBalanceHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(Balance productBalance) {
		checkNotNull(productBalance);
		
		try {
			return this.helper.getproductBalanceDao().create(productBalance);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int update(Balance productBalance) {
		checkNotNull(productBalance);
		
		try {
			return this.helper.getproductBalanceDao().update(productBalance);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int delete(Balance productBalance) {
		checkNotNull(productBalance);
		
		try {
			return this.helper.getproductBalanceDao().delete(productBalance);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public Select select() {
		return new Select(this);
	}
	
	public static class Select {
		private QueryBuilder<Balance, Integer> builder;
		
		private ProductBalanceHelper helper;

		private Where<Balance, Integer> where;
		
		private boolean addedOr = false;
		
		private Select(ProductBalanceHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getproductBalanceDao().queryBuilder();
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
		
		public Map<Pair<Integer, Integer>, Balance> asUserProductMap() {
			HashMap<Pair<Integer, Integer>, Balance> result = Maps.newHashMap();
			CloseableIterator<Balance> iterator = null;
			
			try {
				iterator = this.builder.iterator();
				
				while (iterator.hasNext()) {
					Balance productBalance = iterator.next();
					Pair<Integer, Integer> key = Pair.create(
						productBalance.getUser().getId(), productBalance.getProduct().getId());
					
					result.put(key, productBalance);
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
		
		public Balance first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
		
		public List<Balance> all() {
			try {
				return this.builder.query();
			} catch (SQLException e) {
				this.helper.handleException(e);
				return null;
			}
		}
	}
}
