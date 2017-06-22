package com.basilfx.bierapp.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.DatabaseResults;
import com.basilfx.bierapp.data.models.TransactionItem;
import com.basilfx.bierapp.data.models.User;
import com.basilfx.bierapp.data.models.Balance;
import com.basilfx.bierapp.database.UserHelper.Select;

public class TransactionItemHelper extends QueryHelper {
	private DatabaseHelper helper;
	
	public TransactionItemHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(TransactionItem transactionItem) {
		checkNotNull(transactionItem);
		
		try {
			return this.helper.getTransactionItemDao().create(transactionItem);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int update(TransactionItem transactionItem) {
		checkNotNull(transactionItem);
		
		try {
			return this.helper.getTransactionItemDao().update(transactionItem);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public void delete(TransactionItem transactionItem) {
		checkNotNull(transactionItem);
		
		try {
			this.helper.getTransactionItemDao().delete(transactionItem);
		} catch (SQLException e) {
			this.handleException(e);
		}
	}
	
	public Select select() {
		return new Select(this);
	}
	
	public Update update() {
		return new Update(this);
	}
	
	public Delete delete() {
		return new Delete(this);
	}
	
	public static class Select {
		private QueryBuilder<TransactionItem, Integer> builder;
		
		private TransactionItemHelper helper;

		private Where<TransactionItem, Integer> where;
		
		private boolean addedOr = false;

		private boolean rawToObjects = false;
		
		private Select(TransactionItemHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getTransactionItemDao().queryBuilder();
		}
		
		private void checkWhere() {
			if (this.where == null) {
				this.where = builder.where();
			} else if (!this.addedOr) {
				this.where = this.where.and();
			}
		}
		
		public Select selectIds() {
			this.builder = this.builder.selectColumns("id");
			return this;
		}
		
		public Select sumCount() {
			this.builder = builder.selectRaw("SUM(count)");
			return this;
		}
		
		public Select count() {
			this.builder = builder.selectRaw("COUNT(*)");
			return this;
		}
		
		public Select whereIdEq(int id) {
			try {
				this.checkWhere();
				this.where = this.where.eq("id", id);
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
		
		public Select wherePayerIdEq(int payerId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("payerId", payerId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereTransactionIdEq(int transactionId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("transactionId", transactionId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereProductIdEq(int productId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("productId", productId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereProductIdIn(List<Integer> ids) {
			try {
				this.checkWhere();
				this.where = this.where.in("productId", ids);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereUserIdIn(List<Integer> ids) {
			try {
				this.checkWhere();
				this.where = this.where.in("userId", ids);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereRemoteIdEq(Integer remoteId) {
			try {
				this.checkWhere();
				
				if (remoteId == null) {
					this.where = this.where.isNull("remoteId");
				} else {
					this.where = this.where.eq("remoteId", remoteId);
				}
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereRemoteIdNeq(Integer remoteId) {
			try {
				this.checkWhere();
				
				if (remoteId == null) {
					this.where = this.where.isNotNull("remoteId");
				} else {
					this.where = this.where.not().eq("remoteId", remoteId);
				}
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select groupByUserAndProduct() {
			this.builder.groupBy("userId");
			this.builder.groupBy("productId");
			
			this.builder = builder.selectRaw("*, SUM(count) as count");
			this.rawToObjects = true;
			
			return this;
		}
		
		public Select orderByPayer(boolean ascending) {
			this.builder.orderBy("payerId", ascending);
			return this;
		}
		
		public Select orderByUser(boolean ascending) {
			this.builder.orderBy("userId", ascending);
			return this;
		}
		
		public Select orderById(boolean ascending) {
			this.builder.orderBy("id", ascending);
			return this;
		}
		
		public TransactionItem first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
		
		public List<TransactionItem> all() {
			try {
				if (!this.rawToObjects) {
					return this.builder.query();
				} else {
					GenericRawResults<TransactionItem> result =  this.helper.helper.getTransactionItemDao().queryRaw(this.builder.prepareStatementString(), this.helper.helper.getTransactionItemDao().getRawRowMapper());
					return result.getResults();
				}
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
		
		public int firstInt() {
			try {
				return (int) this.helper.helper.getTransactionItemDao().queryRawValue(this.builder.prepareStatementString());
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return 0;
		}

		public Map<Pair<Integer, Integer>, Integer> asUserProductCountMap() {
			// Prepare query so the right fields match
			this.builder = this.builder.selectRaw("userId, productId, SUM(count) as count");
			
			this.builder.groupBy("userId");
			this.builder.groupBy("productId");
			
			// Iterate trough results
			HashMap<Pair<Integer, Integer>, Integer> result = Maps.newHashMap();
			
			try {
				RawRowMapper<Integer[]> mapper = new RawRowMapper<Integer[]>() {
					public Integer[] mapRow(String[] columnNames, String[] resultColumns) {
						return new Integer[] { Integer.parseInt(resultColumns[0]), Integer.parseInt(resultColumns[1]), Integer.parseInt(resultColumns[2]) }; 
					}
				};
				
				GenericRawResults<Integer[]> iterator = this.helper.helper.getHostingDao().queryRaw(this.builder.prepareStatementString(), mapper); 
			
				for (Integer[] row : iterator) {
					result.put(Pair.create(row[0], row[1]), row[2]);
				}
				
				return result;
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
	}
	
	public static class Update {
		private UpdateBuilder<TransactionItem, Integer> builder;
		
		private TransactionItemHelper helper;

		private Where<TransactionItem, Integer> where;
		
		private boolean addedOr = false;
		
		private Update(TransactionItemHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getTransactionItemDao().updateBuilder();
		}
		
		private void checkWhere() {
			if (this.where == null) {
				this.where = builder.where();
			} else if (!this.addedOr) {
				this.where = this.where.and();
			}
		}
		
		public Update whereIdEq(int id) {
			try {
				this.checkWhere();
				this.where = this.where.eq("id", id);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
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
		
		public Update wherePayerIdEq(int payerId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("payerId", payerId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Update whereTransactionIdEq(int transactionId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("transactionId", transactionId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Update whereRemoteIdEq(Integer remoteId) {
			try {
				this.checkWhere();
				
				if (remoteId == null) {
					this.where = this.where.isNull("remoteId");
				} else {
					this.where = this.where.eq("remoteId", remoteId);
				}
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Update whereRemoteIdNeq(Integer remoteId) {
			try {
				this.checkWhere();
				
				if (remoteId == null) {
					this.where = this.where.isNotNull("remoteId");
				} else {
					this.where = this.where.not().eq("remoteId", remoteId);
				}
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Update addCount(int count) {
			try {
				this.builder.updateColumnExpression("count", "count + " + count);
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
	
	public static class Delete {
		
		private DeleteBuilder<TransactionItem, Integer> builder;
		
		private TransactionItemHelper helper;

		private Where<TransactionItem, Integer> where;
		
		private boolean addedOr = false;
		
		private Delete(TransactionItemHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getTransactionItemDao().deleteBuilder();
		}
		
		private void checkWhere() {
			if (this.where == null) {
				this.where = builder.where();
			} else if (!this.addedOr) {
				this.where = this.where.and();
			}
		}
		
		public Delete whereIdEq(int id) {
			try {
				this.checkWhere();
				this.where = this.where.eq("id", id);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Delete whereUserIdEq(int userId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("userId", userId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Delete wherePayerIdEq(int payerId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("payerId", payerId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Delete whereTransactionIdEq(int transactionId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("transactionId", transactionId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Delete whereProductIdEq(int productId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("productId", productId);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Delete whereRemoteIdEq(Integer remoteId) {
			try {
				this.checkWhere();
				
				if (remoteId == null) {
					this.where = this.where.isNull("remoteId");
				} else {
					this.where = this.where.eq("remoteId", remoteId);
				}
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Delete whereRemoteIdNeq(Integer remoteId) {
			try {
				this.checkWhere();
				
				if (remoteId == null) {
					this.where = this.where.isNotNull("remoteId");
				} else {
					this.where = this.where.not().eq("remoteId", remoteId);
				}
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public int execute() {
			try {
				return helper.helper.getTransactionItemDao().delete(this.builder.prepare());
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return -1;
		}
	}
}
