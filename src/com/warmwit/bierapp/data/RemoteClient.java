package com.warmwit.bierapp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.Gson;


public class RemoteClient {
	
	private String baseUrl;
	
	/**
	 * API URL: /
	 */
	public class ApiRoot {
		public String transactions;
		public String products;
		public String users;
		
		public String name;
	}
	
	public class ApiUser {
		public int id;
		
		public String first_name;
		public String last_name;
		public String avatar;
	}
	
	public class ApiProduct {
		public int id;
		
		public String title;
		public int cost;
	}
	
	public class ApiTransaction {
		public int id;
		public String description;
		
		public ApiTransactionItem[] transaction_items;
	}
	
	public class ApiTransactionItem {
		public int accounted_user;
		public int executing_user;
		public int product;
		public int count;
	}
	
	public RemoteClient(String baseUrl) {
		this.baseUrl = checkNotNull(baseUrl);
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
		InputStreamReader reader = getInputStream(completeUrl);
		
		// Parse result
		if (url.equals("/")) {
			return new Gson().fromJson(getInputStream(completeUrl), ApiRoot.class);
		} else if (url.startsWith("/users")) {
			if (url.equals("/users")) { // All users
				return new Gson().fromJson(reader, ApiUser[].class);
			} else { // Single user
				return new Gson().fromJson(reader, ApiUser.class);
			}
		} else if (url.startsWith("/guests")) {
			if (url.equals("/guests")) { // All guests
				return new Gson().fromJson(reader, ApiUser[].class);
			} else { // Single guest
				return new Gson().fromJson(reader, ApiUser.class);
			}
		} else if (url.startsWith("/products")) {
			if (url.equals("/products")) { // All products
				return new Gson().fromJson(reader, ApiProduct[].class);
			} else { // Single product
				return new Gson().fromJson(reader, ApiProduct.class);
			}
		} else if (url.startsWith("/transactions")) {
			if (url.equals("/transactions")) { // All transactions
				return new Gson().fromJson(reader, ApiTransaction[].class);
			} else { // Single transaction
				return new Gson().fromJson(reader, ApiTransaction.class);
			}
		}
		
		// If we arrive here, the URL is not caught.
		throw new UnsupportedOperationException(url);
	}
	
	private InputStreamReader getInputStream(String relativeUrl) throws IOException {
		URL url = new URL(this.baseUrl + relativeUrl);
		return new InputStreamReader(url.openStream());
	}
}
