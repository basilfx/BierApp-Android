package com.basilfx.bierapp.actions;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.http.auth.AuthenticationException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.basilfx.bierapp.data.ApiConnector;
import com.basilfx.bierapp.exceptions.RetriesExceededException;
import com.basilfx.bierapp.exceptions.UnexpectedData;
import com.basilfx.bierapp.exceptions.UnexpectedStatusCode;
import com.basilfx.bierapp.exceptions.UnexpectedUrl;
import com.basilfx.bierapp.utils.LogUtils;

public class SyncAction extends Action {
	public static final String LOG_TAG = "SyncAction";

	private Context context;
	
	private ApiConnector apiConnector;
	
	public SyncAction(Context context, ApiConnector apiConnector) {
		this.context = context;
		this.apiConnector = apiConnector;
	}
	
	public int basicSync() {
		// Check internet connection and stop prematurely 
		NetworkInfo info = ((ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	        
		if (info == null || !info.isConnectedOrConnecting()) {
			return LogUtils.logException(LOG_TAG, new IOException("No internet connectivity"), Action.RESULT_ERROR_CONNECTION);
		}
		
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
		} catch (UnexpectedData e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
		} catch (UnexpectedUrl e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
		} catch (RetriesExceededException e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
		} catch (AuthenticationException e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_AUTHENTICATION);
		}
			
		// No error
		return Action.RESULT_OK;
	}
}
