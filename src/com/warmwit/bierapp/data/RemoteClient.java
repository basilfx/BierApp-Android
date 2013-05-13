package com.warmwit.bierapp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class RemoteClient {
	/**
	 * @const Logging tag
	 */
	public static final String LOG_TAG = "RemoteClient";
	
	
	private String baseUrl;
	
	private String accessToken;
	
	private String refreshToken;
	
	private HttpClient client;
	
	public RemoteClient(String baseUrl, String accessToken) {
		this.baseUrl = checkNotNull(baseUrl);
		this.accessToken = checkNotNull(accessToken);
	}
	
	public Object post(Object object, String url, String query) throws IOException {
		checkNotNull(url);
		
		String completeUrl = url + (query != null ? "?" + query : "");
		
		if (url.equals("/transactions/")){
			String data = new Gson().toJson(object);
			HttpResponse response = this.postRequest(completeUrl, data);
			
			if (response.getStatusLine().getStatusCode() == 200) {
				data = EntityUtils.toString(response.getEntity());
				
				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
				return gson.fromJson(data, ApiTransaction.class);
			} else {
				return null;
			}
		}
		
		// If we arrive here, the URL is not caught.
		throw new UnsupportedOperationException(url);
	}
	
	public Object get(String url, String query) throws IOException {
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
		Log.d(LOG_TAG, "Data: " + data);
		
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
	
	private HttpResponse getRequest(String relativeUrl) throws IOException {
		if (this.client == null) {
			this.client = new DefaultHttpClient();
		}
		
		try {
			URL url = new URL(this.baseUrl + relativeUrl);
			HttpGet request = new HttpGet(this.baseUrl + relativeUrl);
			
			if (this.accessToken != null) {
				request.addHeader("Authorization", "Bearer " + this.accessToken);
			}
			
			return this.client.execute(request);
		} catch (IOException e) {
			this.client = null;
			throw e;
		}
	}
	
	private HttpResponse postRequest(String relativeUrl, String data) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(this.baseUrl + relativeUrl);
		
		// Set header and data
		request.addHeader("Content-type", "application/json");
		request.addHeader("Authorization", "Bearer " + this.accessToken);
		request.setEntity(new StringEntity(data));
		
		// Execute and return
		return client.execute(request);
	}
}
