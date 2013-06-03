package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.warmwit.bierapp.actions.Action;
import com.warmwit.bierapp.actions.SyncAction;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.data.RemoteClient;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.utils.TokenInfo;

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
	 * 
	 */
	private TokenInfo tokenInfo;
	
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
         
        // Continue main logic
        this.start();
    }
    
    private void start() {
    	RemoteClient remoteClient = BierAppApplication.getRemoteClient();
    	
    	// Decide to authorize or not
        if (remoteClient.getTokenInfo().isValid()) {
        	// Create API instance
        	Log.d(LOG_TAG, "Using token info: " + remoteClient.getTokenInfo());
        	this.apiConnector = new ApiConnector(remoteClient, this.getHelper());
        	
        	// Load data and advance to next screen
            new LoadDataTask().execute();
            
            // Done
            return;
        }
        
        // Catch for authorization
    	Intent intent = new Intent(SplashActivity.this, AuthorizeActivity.class);
		startActivity(intent);

		// Finish this activity so it is cleared from the stack
		this.finish();
    }
    
	/**
     * Sync data task.
     */
    private class LoadDataTask extends AsyncTask<Void, Void, Integer> {
    	/**
    	 * Execute the sync action.
    	 */
		@Override
		protected Integer doInBackground(Void... params) {
			return new SyncAction(SplashActivity.this.apiConnector).basicSync();
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
				case Action.RESULT_OK:
					// Advance to next screen, with little delay
					new Handler().postDelayed(new Runnable(){
						public void run() {
							Log.d(LOG_TAG, "Loading HomeActivity.");
							
							// Advance to next activity
							Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
							startActivity(intent);
							
							// Finish this activity so it is cleared from the stack
							SplashActivity.this.finish();
						}
					}, SplashActivity.SPLASH_TIMEOUT);
					
					break;
				case Action.RESULT_ERROR_CONNECTION:
					SplashActivity.this.showMessage("Internetverbinding", "Kan de server niet benaderen. Controleer of er een actieve internetverbinding is.");
					break;
				case Action.RESULT_ERROR_SQL:
					SplashActivity.this.showMessage("Applicatiefout", "Interne cache is corrupt. Probeer de applicatiegegevens te wissen en probeer het opnieuw.");
					break;
				case Action.RESULT_ERROR_SERVER:
					SplashActivity.this.showMessage("Serverfout", "De server gaf een onverwacht resultaat terug. Probeer het nogmaals.");
					break;
				case Action.RESULT_ERROR_AUTHENTICATION:
					SplashActivity.this.start();
					break;
				default:
					throw new IllegalStateException("Code: " + result);
			}
		}
    }
    
    /**
	 * Helper to display a dialog with an OK button. After clicking, the 
	 * activity will finish, thus quit.
	 * 
	 * @param title Dialog title
	 * @param message Dialog message
	 */
	private void showMessage(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
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
