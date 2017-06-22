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
import com.basilfx.bierapp.data.models.Product;

public class ProductHelper extends QueryHelper {
	
	private DatabaseHelper helper;
	
	public ProductHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(Product product) {
		checkNotNull(product);
		
		try {
			return this.helper.getProductDao().create(product);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int update(Product product) {
		checkNotNull(product);
		
		try {
			return this.helper.getProductDao().update(product);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int delete(Product product) {
		checkNotNull(product);
		
		try {
			return this.helper.getProductDao().delete(product);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public Select select() {
		return new Select(this);
	}
	
	public static class Select {
		ProductHelper helper;
		
		QueryBuilder<Product, Integer> builder;
		
		Where<Product, Integer> where;
		
		boolean addedOr = false;
		
		private Select(ProductHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getProductDao().queryBuilder();
		}

		public Select whereIdIn(List<Integer> ids) {
			try {
				if (this.where == null) {
					this.where = builder.where();
				} else if (!this.addedOr) {
					this.where = this.where.and();
				}
				
				this.where = this.where.in("id", ids);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Product first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
		
		public List<Product> all() {
			try {
				return this.builder.query();
			} catch (SQLException e) {
				this.helper.handleException(e);
				return null;
			}
		}
		
		public Map<Integer, Product> asMap() {
			HashMap<Integer, Product> result = Maps.newHashMap();
			CloseableIterator<Product> iterator = null;
			
			try {
				iterator = this.builder.iterator();
				
				while (iterator.hasNext()) {
					Product product = iterator.next();
					result.put(product.getId(), product);
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
	}
}
