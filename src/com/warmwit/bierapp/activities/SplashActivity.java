package com.warmwit.bierapp.activities;

import java.io.IOException;
import java.sql.SQLException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.data.RemoteClient;
import com.warmwit.bierapp.database.DatabaseHelper;

public class SplashActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	
	private RemoteClient remoteClient;
	private ApiConnector apiConnector;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.remoteClient = ((BierAppApplication) this.getApplication()).getRemoteClient();
		this.apiConnector = new ApiConnector(remoteClient, this.getHelper());
        
        // Make splash screen full size
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        
        // Set content
        this.setContentView(R.layout.activity_splash);
        
        // Load data and advance to next screen
        new LoadDataTask().execute();
    }
    
    private class LoadDataTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			// Warmup cache
			try {
				SplashActivity.this.apiConnector.loadProducts();
				SplashActivity.this.apiConnector.loadUsers();
				SplashActivity.this.apiConnector.loadTransactions();
				SplashActivity.this.apiConnector.loadUserInfo();
			} catch (IOException e) {
				Log.e(this.getClass().getName(), e.getMessage());
			} catch (SQLException e) {
				Log.e(this.getClass().getName(), e.getMessage());
			}
	        
	        return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			// Advance to next screen, with little delay
			new Handler().postDelayed(new Runnable(){
				public void run() {
					Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
					startActivity(intent);             
				}
			}, 1);
		}
    }
}
