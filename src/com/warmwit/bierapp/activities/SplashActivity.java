package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.SQLException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.utils.LogUtils;

/**
 * Splash screen activity. Loads data from server, then advances to 
 * HomeActivity.
 * 
 * @author Bas Stottelaar
 */
public class SplashActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	
	/**
	 * @const Logging tag
	 */
	public static final String LOG_TAG = "SplashActivity";
	
	/**
	 * @const Timeout before advancing to next screen in ms
	 */
	public static final int SPLASH_TIMEOUT = 250;
	
	/**
	 * @var Reference to API connector
	 */
	private ApiConnector apiConnector;
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Logging
        Log.i(LOG_TAG, "Started application");
        
        // Configure activity
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_splash);
         
        // Decide to authorize or not
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (preferences.contains("access_token")) {
        	String accessToken = preferences.getString("access_token", null);
        	int expires = preferences.getInt("expires_in", -1);
        	
        	if (accessToken != null) {
	        	// Setup remote client
	        	Log.d(LOG_TAG, "Using access token: " + accessToken);
	        	((BierAppApplication) this.getApplication()).initRemoteClient(accessToken);
	        	
	        	// Create API instance
	        	this.apiConnector = new ApiConnector(BierAppApplication.remoteClient, this.getHelper());
	        	
	        	// Load data and advance to next screen
	            new LoadDataTask().execute();
	            
	            // Done
	            return;
        	}
        }
        
        // Catch for authorization
    	Intent intent = new Intent(SplashActivity.this, AuthorizeActivity.class);
		startActivity(intent);
    }

	/**
     * Sync data task.
     */
    private class LoadDataTask extends AsyncTask<Void, Void, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			try {
				// Products
				Log.d(LOG_TAG, "Loading products");
				SplashActivity.this.apiConnector.loadProducts();
				
				// Users
				Log.d(LOG_TAG, "Loading users");
				SplashActivity.this.apiConnector.loadUsers();
				
				// Transactions
				Log.d(LOG_TAG, "Loading transactions");
				SplashActivity.this.apiConnector.loadTransactions();
				
				// Users info
				Log.d(LOG_TAG, "Loading users info");
				SplashActivity.this.apiConnector.loadUserInfo();
			} catch (IOException e) {
				return LogUtils.logException(LOG_TAG, e, 1);
			} catch (SQLException e) {
				return LogUtils.logException(LOG_TAG, e, 2);
			}
	        
			// Done
	        return 0;
		}

		/**
		 * Runs the appropriate action for a given result. The result is either
		 * zero to indicate success or another integer to indicate failure.
		 * 
		 * @param result Integer result of the task
		 */
		@Override
		protected void onPostExecute(Integer result) {
			checkNotNull(result);
			
			switch (result) {
				case 0:
					// Advance to next screen, with little delay
					new Handler().postDelayed(new Runnable(){
						public void run() {
							Log.d(LOG_TAG, "Loading HomeActivity.");
							
							Intent intent = new Intent(SplashActivity.this, AuthorizeActivity.class);
							startActivity(intent);             
						}
					}, SplashActivity.SPLASH_TIMEOUT);
					
					break;
				case 1:
					this.showDialog("Internetverbinding", "Kan de server niet benaderen. Controleer of er een actieve internetverbinding is.");
					break;
				case 2:
					this.showDialog("Applicatiefout", "Interne cache is corrupt. Probeer de applicatiegegevens te wissen en probeer het opnieuw.");
					break;
				default:
					// Should not land here
					throw new IllegalStateException();
			}
		}
		
		/**
		 * Internal helper to display a dialog with an OK button. After clicking,
		 * the activity will finish, thus quit.
		 * 
		 * @param title Dialog title
		 * @param message Dialog message
		 */
		private void showDialog(String title, String message) {
			AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
			
			// Configure alert dialog
			builder.setTitle(title);
			builder.setMessage(message);
			builder.setCancelable(false);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Closes application
					SplashActivity.this.finish();
				}
			});
			
			// Show dialog
			builder.show();
		}
    }
}
