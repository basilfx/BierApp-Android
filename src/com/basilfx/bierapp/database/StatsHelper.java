package com.basilfx.bierapp.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;

import com.basilfx.bierapp.data.models.Stats;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class StatsHelper extends QueryHelper {
	
	private DatabaseHelper helper;
	
	public StatsHelper(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	public int create(Stats stats) {
		checkNotNull(stats);
		
		try {
			return this.helper.getStatsDao().create(stats);
		} catch (SQLException e) {
			this.handleException(e);
		}
		
		return -1;
	}
	
	public Select select() {
		return new Select(this);
	}
	
	public static class Select {
		StatsHelper helper;
		
		QueryBuilder<Stats, Integer> builder;
		
		Where<Stats, Integer> where;
		
		boolean addedOr = false;
		
		private Select(StatsHelper helper) {
			this.helper = helper;
			this.builder = helper.helper.getStatsDao().queryBuilder();
		}
		
		public Stats first() {
			try {
				return this.builder.queryForFirst();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
		
		public Stats last() {
			try {
				return this.builder.orderBy("id", false).queryForFirst();
			} catch (SQLException e) {
				this.helper.handleException(e);
			}
			
			return null;
		}
	}
}
