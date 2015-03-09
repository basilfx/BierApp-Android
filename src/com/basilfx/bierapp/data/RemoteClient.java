package com.basilfx.bierapp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
import com.google.gson.JsonSyntaxException;
import com.j256.ormlite.logger.Logger;
import com.basilfx.bierapp.actions.RefreshTokenAction;
import com.basilfx.bierapp.exceptions.RetriesExceededException;
import com.basilfx.bierapp.exceptions.RetryException;
import com.basilfx.bierapp.exceptions.UnexpectedData;
import com.basilfx.bierapp.exceptions.UnexpectedStatusCode;
import com.basilfx.bierapp.exceptions.UnexpectedUrl;
import com.basilfx.bierapp.utils.TokenInfo;


public class RemoteClient {
	/**
	 * @const Logging tag
	 */
	public static final String LOG_TAG = "RemoteClient";
	
	private String apiUrl;
	
	private TokenInfo tokenInfo;
	
	private HttpClient client;
	
	private Context context;
	
	public RemoteClient(Context context, String apiUrl) {
		this.context = checkNotNull(context);
		this.apiUrl = checkNotNull(apiUrl);
		
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
	
	public String validateUrl(String url) throws UnexpectedUrl {
		checkNotNull(url);

		if (url.startsWith("/")) {
			url = this.apiUrl + url;
		} else if (url.contains(this.apiUrl)) {
			url = this.apiUrl + url.substring(url.indexOf(this.apiUrl) + apiUrl.length());
		} else {
			throw new UnexpectedUrl("Not an API url");
		}
		
		// Add slash
		if (url.contains("?")) {
			String first = url.substring(0, url.indexOf("?"));
			String second = url.substring(url.indexOf("?"));

			if (!first.endsWith("/")) {
				url = first + "/" + second; 
			}
		} else {
			if (!url.endsWith("/")) {
				url = url + "/";
			}
		}
		
		// Done
		return url;
	}
	
	/**
	 * Split URL to retrieve the path between the API url and query 
	 * parameters. Make sure that the result ends with a slash.
	 * 
	 * @param url
	 * @return
	 */
	public String splitUrl(String url) {
		String result;

		// Get the part between API url and query parameters.
		if (url.contains("?")) {
			result = url.substring(this.apiUrl.length(), url.indexOf("?"));
		} else {
			result = url.substring(this.apiUrl.length());
		}
		
		// Make sure it ends with a slash.
		if (!result.endsWith("/")) {
			result = result + "/";
		}
		
		return result;
	}
	
	public Object post(Object object, String url) throws IOException, AuthenticationException, UnexpectedUrl, UnexpectedStatusCode, UnexpectedData, RetriesExceededException {
		String completeUrl = validateUrl(url);
		String path = splitUrl(completeUrl);
		
		if (path.equals("/transactions/")){
			String data = new Gson().toJson(object);
			HttpResponse response = this.postRequest(completeUrl, data, 0);
			
			data = EntityUtils.toString(response.getEntity());
			
			if (response.getStatusLine().getStatusCode() == 201) {
				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
				
				try {
					return gson.fromJson(data, ApiTransaction.class);
				} catch (JsonSyntaxException e) {
					throw new UnexpectedData("JSON parse error");
				}	
			} else {
				return null;
			}
		}
		
		// If we arrive here, the URL is not caught.
		throw new UnsupportedOperationException(url);
	}
	
	public Object get(String url) throws IOException, AuthenticationException, UnexpectedUrl, UnexpectedStatusCode, UnexpectedData, RetriesExceededException {
		String completeUrl = validateUrl(url);
		String path = splitUrl(completeUrl);
		
		// Request URL. Throws IOException on error
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		HttpResponse response = this.getRequest(completeUrl, 0);
		
		String data = EntityUtils.toString(response.getEntity());
		//Log.d(LOG_TAG, data);
		
		// Parse result
		try {
			if (path.equals("/")) {
				return new Gson().fromJson(data, ApiRoot.class);
			} else if (path.startsWith("/users/")) {
				if (path.equals("/users/")) { // All users
					return gson.fromJson(data, ApiUserPage.class);
				} else if (path.equals("/users/info/")) {
					return gson.fromJson(data, ApiUserPage.class);
				} else { // Single user
					return gson.fromJson(data, ApiUser.class);
				}
			} else if (path.startsWith("/products/")) {
				if (path.equals("/products/")) { // All products
					return gson.fromJson(data, ApiProductPage.class);
				} else { // Single product
					return gson.fromJson(data, ApiProduct.class);
				}
			} else if (path.startsWith("/transactions/")) {
				if (path.equals("/transactions/")) { // All transactions
					return gson.fromJson(data, ApiTransactionPage.class);
				} else { // Single transaction
					return gson.fromJson(data, ApiTransaction.class);
				}
			}
		} catch (JsonSyntaxException e) {
			throw new UnexpectedData("JSON parse error");
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
	
	private HttpResponse getRequest(String url, int retries) throws IOException, AuthenticationException, UnexpectedStatusCode, RetriesExceededException {		
		if (this.client == null) {
			this.client = new DefaultHttpClient();
		}
		
		// Create request
		HttpGet request = new HttpGet(url);
		
		request.addHeader("Content-type", "application/json");
		if (this.tokenInfo.isValid()) {
			request.addHeader("Authorization", "Bearer " + this.tokenInfo.getAccessToken());
		}
		
		// Execute request
		try {
			Log.d(LOG_TAG, "Requesting URL: " + url);
			return this.handleResponse(this.client.execute(request));
		} catch (RetryException e) {
			if (retries < 3) {
				return this.getRequest(url, retries + 1);
			} else {
				throw new RetriesExceededException();
			}
		} catch (IOException e) {
			this.client = null;
			throw e;
		}
	}
	
	private HttpResponse postRequest(String url, String data, int retries) throws IOException, AuthenticationException, UnexpectedStatusCode, RetriesExceededException {
		if (this.client == null) {
			this.client = new DefaultHttpClient();
		}
		
		// Create request
		HttpPost request = new HttpPost(url);
		
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
			if (retries < 3) {
				return this.postRequest(url, data, retries + 1);
			} else {
				throw new RetriesExceededException();
			}
		} catch (IOException e) {
			this.client = null;
			throw e;
		}
	}
}
