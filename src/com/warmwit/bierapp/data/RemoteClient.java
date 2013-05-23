package com.warmwit.bierapp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.actions.RefreshTokenAction;
import com.warmwit.bierapp.exceptions.RetryException;
import com.warmwit.bierapp.exceptions.UnexpectedStatusCode;
import com.warmwit.bierapp.utils.TokenInfo;


public class RemoteClient {
	/**
	 * @const Logging tag
	 */
	public static final String LOG_TAG = "RemoteClient";
	
	private String baseUrl;
	
	private TokenInfo tokenInfo;
	
	private HttpClient client;
	
	private Context context;
	
	public RemoteClient(Context context, String baseUrl) {
		this.context = checkNotNull(context);
		this.baseUrl = checkNotNull(baseUrl);
		
		// Initialize token
		this.tokenInfo = TokenInfo.createFromPreferences(context);
	}
	
	public TokenInfo getTokenInfo() {
		return this.tokenInfo;
	}
	
	public void setTokenInfo(TokenInfo tokenInfo) {
		if (tokenInfo != this.tokenInfo)  {
			tokenInfo.saveToPreferences(this.context);
		}
		
		this.tokenInfo = tokenInfo;
	}
	
	public Object post(Object object, String url, String query) throws IOException, AuthenticationException, UnexpectedStatusCode {
		checkNotNull(url);
		
		String completeUrl = url + (query != null ? "?" + query : "");
		
		if (url.equals("/transactions/")){
			String data = new Gson().toJson(object);
			HttpResponse response = this.postRequest(completeUrl, data);
			
			data = EntityUtils.toString(response.getEntity());
			
			if (response.getStatusLine().getStatusCode() == 201) {
				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
				return gson.fromJson(data, ApiTransaction.class);
			} else {
				return null;
			}
		}
		
		// If we arrive here, the URL is not caught.
		throw new UnsupportedOperationException(url);
	}
	
	public Object get(String url, String query) throws IOException, AuthenticationException, UnexpectedStatusCode {
		checkNotNull(url);
		
		// Build complete URL
		String completeUrl = url + (query != null ? "?" + query : "");
		
		// Remove last slash
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		
		// Request URL. Throws IOException on error
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		HttpResponse response = this.getRequest(completeUrl);
		
		String data = EntityUtils.toString(response.getEntity());
		Log.d(LOG_TAG, data);
		
		// Parse result
		if (url.equals("/")) {
			return new Gson().fromJson(data, ApiRoot.class);
		} else if (url.startsWith("/users")) {
			if (url.equals("/users")) { // All users
				return gson.fromJson(data, ApiUserPage.class);
			} else if (url.equals("/users/info")) {
				return gson.fromJson(data, ApiUserPage.class);
			} else { // Single user
				return gson.fromJson(data, ApiUser.class);
			}
		} else if (url.startsWith("/products")) {
			if (url.equals("/products")) { // All products
				return gson.fromJson(data, ApiProductPage.class);
			} else { // Single product
				return gson.fromJson(data, ApiProduct.class);
			}
		} else if (url.startsWith("/transactions")) {
			if (url.equals("/transactions")) { // All transactions
				return gson.fromJson(data, ApiTransactionPage.class);
			} else { // Single transaction
				return gson.fromJson(data, ApiTransaction.class);
			}
		}
		
		// If we arrive here, the URL is not caught.
		throw new UnsupportedOperationException(url);
	}
	
	private HttpResponse handleResponse(HttpResponse response) throws IOException, UnexpectedStatusCode, AuthenticationException, RetryException {
		// Check for HTTP response code
		if (response.getStatusLine() == null) {
			throw new IOException("Server did not return a HTTP status code");
		}
		
		// Status code 200-399 should be passed on.
		int code = response.getStatusLine().getStatusCode();
		
		if (code >= 200 && code <= 399) {
			return response;
		} 
		
		// Consume connection to release resources
		if (response.getEntity() != null) {
			response.getEntity().consumeContent();
		}
		
		// Handle errors
		if (code == 401) {
			// Try to refresh token
			if (this.tokenInfo.isValid()) {
				if (this.client == null) {
					this.client = new DefaultHttpClient();
				}
				
				// Setup refresh action
				RefreshTokenAction action = new RefreshTokenAction(this.tokenInfo, this.client);
				
				// Execute action
				if (action.refresh() == 0) {
					// Successfully refreshed token 
					this.setTokenInfo(action.getTokenInfo());
					
					// Instruct invoker to retry
					throw new RetryException();
				}
			}
			
			// At this point, we could not refresh token, so clear it.
			this.tokenInfo.deleteInPreferences(this.context);
			
			// Continue with an exception
			throw new AuthenticationException();
		} else {
			throw new UnexpectedStatusCode(code);
		}
	}
	
	private HttpResponse getRequest(String relativeUrl) throws IOException, AuthenticationException, UnexpectedStatusCode {
		if (this.client == null) {
			this.client = new DefaultHttpClient();
		}
		
		// Create request
		HttpGet request = new HttpGet(this.baseUrl + relativeUrl);
		
		request.addHeader("Content-type", "application/json");
		if (this.tokenInfo.isValid()) {
			request.addHeader("Authorization", "Bearer " + this.tokenInfo.getAccessToken());
		}
		
		// Execute request
		try {
			return this.handleResponse(this.client.execute(request));
		} catch (RetryException e) {
			return this.getRequest(relativeUrl);
		} catch (IOException e) {
			this.client = null;
			throw e;
		}
	}
	
	private HttpResponse postRequest(String relativeUrl, String data) throws IOException, AuthenticationException, UnexpectedStatusCode {
		if (this.client == null) {
			this.client = new DefaultHttpClient();
		}
		
		// Create request
		HttpPost request = new HttpPost(this.baseUrl + relativeUrl);
		
		request.addHeader("Content-type", "application/json");
		if (this.tokenInfo.isValid()) {
			request.addHeader("Authorization", "Bearer " + this.tokenInfo.getAccessToken());
		}
		
		// Set request body
		request.setEntity(new StringEntity(data));
		
		// Execute request
		try {
			return this.handleResponse(this.client.execute(request));
		} catch (RetryException e) {
			return this.postRequest(relativeUrl, data);
		} catch (IOException e) {
			this.client = null;
			throw e;
		}
	}
}
