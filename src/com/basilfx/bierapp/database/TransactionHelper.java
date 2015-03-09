package com.basilfx.bierapp.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.basilfx.bierapp.data.models.Transaction;
import com.basilfx.bierapp.data.models.TransactionItem;
import com.basilfx.bierapp.database.HostingHelper.Select;

public class TransactionHelper extends QueryHelper {
	
	private DatabaseHelper helper;
	
	public TransactionHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(Transaction transaction) {
		transaction.setDirty(false);
		
		try {
			return this.helper.getTransactionDao().create(transaction);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public int update(Transaction transaction) {
		transaction.setDirty(false);
		
		try {
			return this.helper.getTransactionDao().update(transaction);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public void delete(Transaction transaction) {
		checkNotNull(transaction);
		
		try {
			// Delete transaction items
			DeleteBuilder<TransactionItem, Integer> deleteBuilder = this.helper.getTransactionItemDao().deleteBuilder();
			deleteBuilder.where().eq("transactionId", transaction.getId());
			this.helper.getTransactionItemDao().delete(deleteBuilder.prepare());

			// Delete transaction
			this.helper.getTransactionDao().delete(transaction);
		} catch (SQLException e) {
			this.handleException(e);
		}
	}
	
	public Select select() {
		return new Select(this);
	}
	
	public static class Select {
		TransactionHelper helper;
		
		QueryBuilder<Transaction, Integer> builder;
		
		Where<Transaction, Integer> where;
		
		boolean addedOr = false;
		
		private Select(TransactionHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getTransactionDao().queryBuilder();
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
		
		public Select selectRemoteIds() {
			this.builder = this.builder.selectColumns("remoteId");
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
		
		public Select whereRemoteIdIn(List<Integer> remoteIds) {
			try {
				this.checkWhere();
				this.where = this.where.in("remoteId", remoteIds);
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return this;
		}
		
		public Select whereTagEq(String tag) {
			try {
				this.checkWhere();
				this.where = this.where.eq("tag", tag);
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
		
		public Select orderByCreated(boolean ascending) {
			builder.orderBy("created", ascending);
			return this;
		}
		
		public Transaction first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
		
		public List<Transaction> all() {
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
}
