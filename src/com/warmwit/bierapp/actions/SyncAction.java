package com.warmwit.bierapp.actions;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.http.auth.AuthenticationException;

import android.util.Log;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.exceptions.UnexpectedStatusCode;
import com.warmwit.bierapp.utils.LogUtils;

public class SyncAction extends Action {
	public static final String LOG_TAG = "SyncAction";

	private ApiConnector apiConnector;
	
	public SyncAction(ApiConnector apiConnector) {
		this.apiConnector = apiConnector;
	}
	
	public int basicSync() {
		try {
			// Products
			Log.d(LOG_TAG, "Loading products");
			this.apiConnector.loadProducts();
			
			// Users
			Log.d(LOG_TAG, "Loading users");
			this.apiConnector.loadUsers();
			
			// Transactions
			Log.d(LOG_TAG, "Loading transactions");
			this.apiConnector.loadTransactions();
			
			// Users info
			Log.d(LOG_TAG, "Loading users info");
			this.apiConnector.loadUserInfo();
		} catch (IOException e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_CONNECTION);
		} catch (SQLException e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SQL);
		} catch (UnexpectedStatusCode e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
		} catch (AuthenticationException e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_AUTHENTICATION);
		}
			
		// No error
		return Action.RESULT_OK;
	}
}
