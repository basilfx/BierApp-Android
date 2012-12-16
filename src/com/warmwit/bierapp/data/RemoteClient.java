package com.warmwit.bierapp.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.warmwit.bierapp.data.model.Guest;
import com.warmwit.bierapp.data.model.Product;
import com.warmwit.bierapp.data.model.User;


public class RemoteClient {
	
	private static final String BASE_URL = "http://10.0.0.21/BierApp/index.php";
	
	public List<User> getUsers() throws IOException {
		return Lists.newArrayList(new Gson().fromJson(getInputStream("/users"), User[].class));
	}
	
	public User getUser(long userId) throws IOException {
		return new Gson().fromJson(getInputStream("/users/" + userId), User.class);
	}
	
	public List<Product> getProducts() throws IOException {
		return Lists.newArrayList(new Gson().fromJson(getInputStream("/products"), Product[].class));
	}
	
	public List<Guest> getGuests() throws IOException {
		Guest[] guests = {
			new Guest(),
			new Guest(),
		};
		
		
		
		return Lists.newArrayList(guests);
	}
	
	public Product getProduct(long productId) throws IOException {
		return new Gson().fromJson(getInputStream("/products/" + productId), Product.class);
	}
	
	private static InputStreamReader getInputStream(String relativeUrl) throws IOException {
		URL url = new URL(BASE_URL + relativeUrl);
		return new InputStreamReader(url.openStream());
	}
}
