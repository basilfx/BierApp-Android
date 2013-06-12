package com.warmwit.bierapp.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.warmwit.bierapp.data.models.TransactionItem;

public class TransactionItemHelper extends QueryHelper {
	private DatabaseHelper helper;
	
	public TransactionItemHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(TransactionItem transactionItem) {
		try {
			return this.helper.getTransactionItemDao().create(transactionItem);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int update(TransactionItem transactionItem) {
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
		
		public Select orderByPayer(boolean ascending) {
			this.builder.orderBy("payerId", ascending);
			return this;
		}
		
		public Select orderByUser(boolean ascending) {
			this.builder.orderBy("userId", ascending);
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
				return this.builder.query();
			} catch (SQLException e) {
				this.helper.handleException(e);
				return null;
			}
		}
		
		public int firstInt() {
			try {
				return (int) this.helper.helper.getTransactionItemDao().queryRawValue(this.builder.prepareStatementString());
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return 0;
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
		
		public Update whereProductIdEq(int productId) {
			try {
				this.checkWhere();
				this.where = this.where.eq("productId", productId);
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
