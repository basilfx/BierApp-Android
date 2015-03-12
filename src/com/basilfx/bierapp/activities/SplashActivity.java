package com.basilfx.bierapp.activities;

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

import com.basilfx.bierapp.BierAppApplication;
import com.basilfx.bierapp.R;
import com.basilfx.bierapp.actions.Action;
import com.basilfx.bierapp.actions.SyncAction;
import com.basilfx.bierapp.data.Connector;
import com.basilfx.bierapp.data.RemoteClient;
import com.basilfx.bierapp.database.DatabaseHelper;
import com.basilfx.bierapp.utils.TokenInfo;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

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
	 * @const Timeout before advancing to next screen in milliseconds
	 */
	public static final int SPLASH_TIMEOUT = 250;
	
	/**
	 * @var Reference to API connector
	 */
	private Connector connector;
	
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
        	this.connector = new Connector(remoteClient, this.getHelper());
        	
        	// Load data and advance to next screen
            new LoadDataTask().execute();
            
            // Done
            return;
        }
        
        // Catch for authorization
    	Intent intent = new Intent(SplashActivity.this, AuthorizeActivity.class);
		
    	this.startActivity(intent);
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
			return new SyncAction(SplashActivity.this, SplashActivity.this.connector).basicSync();
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
						}
					}, SplashActivity.SPLASH_TIMEOUT);
					
					break;
				case Action.RESULT_ERROR_CONNECTION:
					// Show info message and continue as nothing happened
					SplashActivity.this.showMessage("Internetverbinding", "Er kan geen verbinding gemaakt worden met de server. De app is bruikbaar maar de data is wellicht out-of-date. Transacties kunnen pas worden opgeslagen zodra er weer verbinding is.", 
						new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							LoadDataTask.this.onPostExecute(Action.RESULT_OK); 
						}
					});

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
	 * @param callback Callback to associate with OK button, or null for activity finish.
	 */
	private void showMessage(String title, String message, DialogInterface.OnClickListener callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		// Set default callback
		if (callback == null) {
			callback = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SplashActivity.this.finish();
				}
			};
		}
		
		// Configure alert dialog
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setPositiveButton(android.R.string.ok, callback);
		
		// Show dialog
		builder.show();
	}
	
	/**
	 * {@inheritDoc}
	 */
	private void showMessage(String title, String message) {
		this.showMessage(title, message, null);
	}
}
