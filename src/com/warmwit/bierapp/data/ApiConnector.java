package com.warmwit.bierapp.data;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.warmwit.bierapp.data.model.Guest;
import com.warmwit.bierapp.data.model.Product;
import com.warmwit.bierapp.data.model.Transaction;
import com.warmwit.bierapp.data.model.TransactionItem;
import com.warmwit.bierapp.data.model.User;

public class ApiConnector {

	private RemoteClient remoteClient;
	
	// Users
	private Map<Integer, User> userCache;
	private List<Integer> userIndexCache;
	private boolean usersAreLoaded;
	
	// Guests
	private Map<Integer, Guest> guestCache;
	private List<Integer> guestIndexCache;
	private boolean guestsAreLoaded;
	
	// Products
	private Map<Integer, Product> productCache;
	private List<Integer> productIndexCache;
	private boolean productsAreLoaded;
	
	// Transactions
	private Map<Integer, Transaction> transactionCache;
	private List<Integer> transactionIndexCache;
	private boolean transactionsAreLoaded;
	
	public ApiConnector(RemoteClient remoteClient) {
		this.remoteClient = remoteClient;
		
		// Users
		this.userCache = Maps.newHashMap();
		this.userIndexCache = Lists.newArrayList();
		this.usersAreLoaded = false;
		
		// Guests
		this.guestCache = Maps.newHashMap();
		this.guestIndexCache = Lists.newArrayList();
		this.guestsAreLoaded = false;
		
		// Products
		this.productCache = Maps.newHashMap();
		this.productIndexCache = Lists.newArrayList();
		this.productsAreLoaded = false;
		
		// Transactions
		this.transactionCache = Maps.newHashMap();
		this.transactionIndexCache = Lists.newArrayList();
		this.transactionsAreLoaded = false;
	}
	
	//
	// General
	//
	
	private <T> T getByIndex(int index, Map<Integer, T> cache, List<Integer> indexCache) {
		// Index cannot exceed number of users
		checkArgument(index < indexCache.size());
		
		int key = indexCache.get(index);
		return cache.get(key);
	}
	
	//
	// Users
	//
	
	public User getUserByIndex(int index) {
		checkArgument(this.usersAreLoaded);
		return this.getByIndex(index, this.userCache, this.userIndexCache);
	}
	
	public User getUserById(int id) {
		checkArgument(this.usersAreLoaded);
		return this.userCache.get(id);
	}
	
	public List<User> getUsers() {
		checkArgument(this.usersAreLoaded);
		return Lists.newArrayList(this.userCache.values());
	}
	
	public User getUserOrGuestById(int id) {
		if (this.userCache.containsKey(id)) {
			return this.userCache.get(id);
		} else {
			return this.guestCache.get(id);
		}
	}
	
	public void loadUsers() throws IOException {
		RemoteClient.ApiUser[] apiUsers = (RemoteClient.ApiUser[]) this.remoteClient.get("/users", null);
		
		for (RemoteClient.ApiUser apiUser : apiUsers) {
			User user = new User();
			
			user.setId(apiUser.id);
			user.setFirstName(apiUser.first_name);
			user.setLastName(apiUser.last_name);
			user.setAvatarUrl(apiUser.avatar);
			
			// TODO: validation
			this.userCache.put(apiUser.id, user);
			this.userIndexCache.add(apiUser.id);
		}
		
		this.usersAreLoaded = true;
	}
	
	public void loadUserById(int id) throws IOException {
		checkArgument(id > 0);
	}
	
	// 
	// Guests
	//
	public Guest getGuestByIndex(int index) {
		checkArgument(this.guestsAreLoaded);
		return this.getByIndex(index, this.guestCache, this.guestIndexCache);
	}
	
	public Guest getGuestById(int id) {
		checkArgument(this.guestsAreLoaded);
		return this.guestCache.get(id);
	}
	
	public List<Guest> getGuests() {
		checkArgument(this.guestsAreLoaded);
		return Lists.newArrayList(this.guestCache.values());
	}
	
	public void loadGuestById(int id) throws IOException {
		checkArgument(id > 0);
	}
	
	public void loadGuests() throws IOException {
		RemoteClient.ApiUser[] apiGuests = (RemoteClient.ApiUser[]) this.remoteClient.get("/guests", null);
		
		for (RemoteClient.ApiUser apiGuest : apiGuests) {
			Guest guest = new Guest();
			
			guest.setId(apiGuest.id);
			guest.setFirstName(apiGuest.first_name);
			guest.setLastName(apiGuest.last_name);
			guest.setAvatarUrl(apiGuest.avatar);
			
			// TODO: validation
			this.guestCache.put(apiGuest.id, guest);
			this.guestIndexCache.add(apiGuest.id);
		}
		
		this.guestsAreLoaded = true;
	}
	
	//
	// Transactions
	//
	
	public Transaction getTransactionById(int id) {
		checkArgument(this.transactionsAreLoaded);
		return this.transactionCache.get(id);
	}
	
	public Transaction getTransactionByIndex(int index) {
		checkArgument(this.transactionsAreLoaded);
		return getByIndex(index, this.transactionCache, this.transactionIndexCache);
	}
	
	public List<Transaction> getTransactions() {
		checkArgument(this.transactionsAreLoaded);
		return Lists.newArrayList(this.transactionCache.values());
	}
	
	public void loadTransactionById(int id) throws IOException {
		checkArgument(id > 0);
	}
	
	public void loadTransactions() throws IOException {
		RemoteClient.ApiTransaction[] apiTransactions = (RemoteClient.ApiTransaction[]) this.remoteClient.get("/transactions", null);
		
		for (RemoteClient.ApiTransaction apiTransaction : apiTransactions) {
			Transaction transaction = new Transaction();
			
			transaction.setId(apiTransaction.id);
			transaction.setDescription(apiTransaction.description);
			
			for (RemoteClient.ApiTransactionItem apiTransactionItem : apiTransaction.transaction_items) {
				TransactionItem transactionItem = new TransactionItem();
				
				transactionItem.setUser(this.getUserOrGuestById(apiTransactionItem.executing_user));
				transactionItem.setPayer(this.getUserOrGuestById(apiTransactionItem.accounted_user));
				transactionItem.setProduct(this.getProductById(apiTransactionItem.product));
				transactionItem.setAmount(apiTransactionItem.count);
				
				// TODO: validatie
				transaction.add(transactionItem);
			}
			
			this.transactionCache.put(apiTransaction.id, transaction);
		}
		
		this.transactionsAreLoaded = true;
	}
	
	//
	// Products
	//
	
	public Product getProductById(int id) {
		checkArgument(this.productsAreLoaded);
		return this.productCache.get(id);
	}
	
	public Product getProductByIndex(int index) {
		checkArgument(this.productsAreLoaded);
		return getByIndex(index, this.productCache, this.productIndexCache);
	}
	
	public List<Product> getProducts() {
		checkArgument(this.productsAreLoaded);
		return Lists.newArrayList(this.productCache.values());
	}
	
	public void loadProductById(int id) throws IOException {
		checkArgument(id > 0);
	}
	
	public void loadProducts() throws IOException {
		RemoteClient.ApiProduct[] apiProducts = (RemoteClient.ApiProduct[]) this.remoteClient.get("/products", null);
		
		for (RemoteClient.ApiProduct apiProduct : apiProducts) {
			Product product = new Product();
			
			product.setId(apiProduct.id);
			product.setTitle(apiProduct.title);
			product.setCost(apiProduct.cost);
			
			// TODO: validation
			this.productCache.put(apiProduct.id, product);
			this.productIndexCache.add(apiProduct.id);
		}
		
		this.productsAreLoaded = true;
	}
}
