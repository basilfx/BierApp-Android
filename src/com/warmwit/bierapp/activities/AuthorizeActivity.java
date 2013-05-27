package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.actions.Action;
import com.warmwit.bierapp.actions.ExchangeTokenAction;
import com.warmwit.bierapp.utils.ProgressAsyncTask;

public class AuthorizeActivity extends Activity {
	/**
	 * @const Logging tag
	 */
	public static final String LOG_TAG = "AuthorizeActivity";
	
	private WebView webView;
	
	private ProgressDialog dialog;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content
        this.setContentView(R.layout.activity_authorize);
        this.setTitle("Verbinden met BierApp");
        
        // Bind Controls
        this.webView = (WebView) this.findViewById(R.id.webview);
        
        // Initialize dialog
        this.dialog = new ProgressDialog(this);
        this.dialog.setMessage("Verbinden met server");
        
        // Configure it
        WebSettings settings = this.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(false);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	}
	
	private class ExchangeTokenTask extends ProgressAsyncTask<Void, Void, Integer> {
		private ExchangeTokenAction exchangeTokenAction;
		
		public ExchangeTokenTask(String code) {
			super(AuthorizeActivity.this);
			
			this.setMessage("Authoriseren");
			this.exchangeTokenAction = new ExchangeTokenAction(code);
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			return this.exchangeTokenAction.exchange();
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
        			// Save access token to preferences
        			BierAppApplication.getRemoteClient().setTokenInfo(this.exchangeTokenAction.getTokenInfo());
        			
        			// Back to splash screen
        			Intent intent = new Intent(AuthorizeActivity.this, SplashActivity.class);
            		AuthorizeActivity.this.startActivity(intent);
            		AuthorizeActivity.this.finish();
					
					break;
				case Action.RESULT_ERROR_CONNECTION:
					AuthorizeActivity.this.showMessage("Internetverbinding", "Kan de server niet benaderen. Controleer of er een actieve internetverbinding is.");
					break;
				case Action.RESULT_ERROR_SQL:
					AuthorizeActivity.this.showMessage("Applicatiefout", "Interne cache is corrupt. Probeer de applicatiegegevens te wissen en probeer het opnieuw.");
					break;
				case Action.RESULT_ERROR_SERVER:
					AuthorizeActivity.this.showMessage("Koppelingsfout", "De server gaf een onverwachts antwoord. Probeer het nogmaals");
					break;
				default:
					throw new IllegalStateException("Code: " + result);
			}
			
			super.onPostExecute(result);
		}
	}
	
	/**
	 * Internal helper to display a dialog with an OK button. After clicking,
	 * the activity will finish, thus quit.
	 * 
	 * @param title Dialog title
	 * @param message Dialog message
	 */
	private void showMessage(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(AuthorizeActivity.this);
		
		// Configure alert dialog
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Closes application
				AuthorizeActivity.this.finish();
			}
		});
		
		// Show dialog
		builder.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.webView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Resume WebView threads
		this.webView.onResume();
		
		// Show spinner while loading
		AuthorizeActivity.this.dialog.show();
		
		// Build request URL
		String url = Uri.parse(BierAppApplication.OAUTH2_AUTHORIZE_URL)
			.buildUpon()
			.appendQueryParameter("client_id", BierAppApplication.OAUTH2_CLIENT_ID)
			.appendQueryParameter("redirect_uri", BierAppApplication.OAUTH2_REDIRECT_URL)
			.appendQueryParameter("response_type","code")
			.toString();
		
		// Load webpage
        this.webView.loadUrl(url);
        this.webView.setWebViewClient(new WebViewClient() {
        	@Override
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
        		if (url.startsWith(BierAppApplication.OAUTH2_REDIRECT_URL)) {
        			Log.d(LOG_TAG, "Caught redirect URL: " + url);
        			
        			// Strip code
        			Uri uri = Uri.parse(url);
        			String code = uri.getQueryParameter("code");
        			
        			if (code == null || code.length()  == 0) {
        				Log.e(LOG_TAG, "Unable to retrieve a code.");
        				return true;
        			}
        			
        			// Start task to exchange code
        			new ExchangeTokenTask(code).execute();
        			
            		// Done
        			return true;
        		}
        		
        		return false;
        	}
        	
        	@Override
        	public void onPageFinished(WebView view, String url) {
        	    super.onPageFinished(view, url);
        	    view.clearCache(true);
        	    
        	    // Hide spinner
        	    if (AuthorizeActivity.this.dialog.isShowing()) {
					AuthorizeActivity.this.dialog.dismiss();
				}
        	}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Log.e(LOG_TAG, "WebView received error " + errorCode);
				
				// Clear view
				view.loadData("<html></html>", "text/html", "utf-8");
				
				// Hide dialog
				if (AuthorizeActivity.this.dialog.isShowing()) {
					AuthorizeActivity.this.dialog.dismiss();
				}
				
				switch (errorCode) {
					case ERROR_FILE_NOT_FOUND:
					case ERROR_CONNECT:
						// Display warning
						AuthorizeActivity.this.showMessage("Internetverbinding", "Kan de server niet benaderen. Controleer of er een actieve internetverbinding is.");
						break;
				}
			}
        });
	}
}
