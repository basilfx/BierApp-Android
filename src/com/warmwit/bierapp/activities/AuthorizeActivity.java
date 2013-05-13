package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.ApiAccessToken;

public class AuthorizeActivity extends Activity {
	/**
	 * @const Logging tag
	 */
	public static final String LOG_TAG = "AuthorizeActivity";
	
	private WebView webView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content
        this.setContentView(R.layout.activity_authorize);
        this.setTitle("Verbinden met BierApp");
        
        // Bind Webview
        this.webView = (WebView) this.findViewById(R.id.webview);
        
        // Configure it
        WebSettings settings = this.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(false);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        
        // Start webpage
        this.webView.loadUrl(BierAppApplication.getAuthorizeUrl());
        this.webView.setWebViewClient(new WebViewClient() {
        	@Override
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
        		if (url.startsWith(BierAppApplication.REDIRECT_URL)) {
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
        	}
        });
	}
	
	private class ExchangeTokenTask extends AsyncTask<Void, Void, Integer> {
		private String code;
		
		private ApiAccessToken tokenInfo;
		
		public ExchangeTokenTask(String code) {
			this.code = code;
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			String url = BierAppApplication.getAccessTokenFromCode();
			
			// Exchange code for an access token
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost(url);
			
			try {
				List<NameValuePair> form = Lists.newArrayList();
				form.add(new BasicNameValuePair("client_id", BierAppApplication.CLIENT_ID));
				form.add(new BasicNameValuePair("client_secret", BierAppApplication.CLIENT_SECRET));
				form.add(new BasicNameValuePair("code", this.code));
				form.add(new BasicNameValuePair("grant_type", "authorization_code"));
				
				request.setEntity(new UrlEncodedFormEntity(form));
			} catch (UnsupportedEncodingException e) {
				return 2;
			}
			
			// Execute request
			try {
				HttpResponse response = client.execute(request);
				String json = EntityUtils.toString(response.getEntity());
				
				// Store result
				this.tokenInfo = new Gson().fromJson(json, ApiAccessToken.class);
				
				return 0;
			} catch (IOException e) {
				return 1;
			}
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
        			// Handle access token
        			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AuthorizeActivity.this);
        			SharedPreferences.Editor editor = preferences.edit();
        			
        			editor.putString("access_token", this.tokenInfo.access_token);
        			editor.putInt("expires_in", this.tokenInfo.expires_in);
        			editor.commit();
        			
        			// Back to splash screen
        			Intent intent = new Intent(AuthorizeActivity.this, SplashActivity.class);
            		AuthorizeActivity.this.startActivity(intent);
            		AuthorizeActivity.this.finish();
					
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
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.webView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.webView.onResume();
	}
}
