package com.warmwit.bierapp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class RemoteClient {
	
	private String baseUrl;
	
	public RemoteClient(String baseUrl) {
		this.baseUrl = checkNotNull(baseUrl);
	}
	
	public Object post(Object object, String url, String query) throws IOException {
		checkNotNull(url);
		
		if (url.equals("/transactions/")){
			String data = new Gson().toJson(object);
			HttpResponse response = this.postRequest(url, data);
			
			if (response.getStatusLine().getStatusCode() == 200) {
				data = EntityUtils.toString(response.getEntity());
				
				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
				return gson.fromJson(data, ApiTransaction[].class)[0];
			} else {
				return null;
			}
		}
		
		// If we arrive here, the URL is not caught.
		throw new UnsupportedOperationException(url);
	}
	
	public Object get(String url, String query) throws IOException {
		checkNotNull(url);
		
		// Remove last slash
		if (url.endsWith("/")) {
			url = url.substring(1, url.length() - 1);
		}
		
		// Build complete URL
		String completeUrl = url + (query != null ? "?" + query : "");
		
		// Request URL. Throws IOException on error
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		InputStreamReader reader = getInputStream(completeUrl);
		
		// Parse result
		if (url.equals("/")) {
			return new Gson().fromJson(getInputStream(completeUrl), ApiRoot.class);
		} else if (url.startsWith("/users")) {
			if (url.equals("/users")) { // All users
				return gson.fromJson(reader, ApiUser[].class);
			} else if (url.equals("/users/info")) {
				return gson.fromJson(reader, ApiUser[].class);
			} else { // Single user
				return gson.fromJson(reader, ApiUser.class);
			}
		} else if (url.startsWith("/guests")) {
			if (url.equals("/guests")) { // All guests
				return gson.fromJson(reader, ApiUser[].class);
			} else { // Single guest
				return gson.fromJson(reader, ApiUser.class);
			}
		} else if (url.startsWith("/products")) {
			if (url.equals("/products")) { // All products
				return gson.fromJson(reader, ApiProduct[].class);
			} else { // Single product
				return gson.fromJson(reader, ApiProduct.class);
			}
		} else if (url.startsWith("/transactions")) {
			if (url.equals("/transactions")) { // All transactions
				return gson.fromJson(reader, ApiTransaction[].class);
			} else { // Single transaction
				return gson.fromJson(reader, ApiTransaction.class);
			}
		}
		
		// If we arrive here, the URL is not caught.
		throw new UnsupportedOperationException(url);
	}
	
	private InputStreamReader getInputStream(String relativeUrl) throws IOException {
		URL url = new URL(this.baseUrl + relativeUrl);
		return new InputStreamReader(url.openStream());
	}
	
	private HttpResponse postRequest(String relativeUrl, String data) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(this.baseUrl + relativeUrl);
		
		// Set header and data
		request.addHeader("Content-type", "application/json");
		request.setEntity(new StringEntity(data));
		
		// Execute and return
		return client.execute(request);
	}
}
