package com.warmwit.bierapp.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.ApiConnector;

public class SplashActivity extends Activity {

	private class LoadDataTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			ApiConnector apiConnector = ((BierAppApplication) SplashActivity.this.getApplication()).getApiConnector();
			
			// Warmup cache
			try {
				apiConnector.loadUsers();
				apiConnector.loadGuests();
				apiConnector.loadProducts();
				apiConnector.loadTransactions();
			} catch (IOException e) {
				Log.e("SPLASH", e.getMessage());
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
			}, 1000);
		}
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make splash screen full size
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        
        // Set content
        this.setContentView(R.layout.activity_splash);
        
        // Load data and advance to next screen
        new LoadDataTask().execute();
    }
}
