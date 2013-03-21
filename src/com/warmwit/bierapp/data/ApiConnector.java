package com.warmwit.bierapp.data;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.warmwit.bierapp.data.models.Guest;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;

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
		ApiUser[] apiUsers = (ApiUser[]) this.remoteClient.get("/users", null);
		
		for (ApiUser apiUser : apiUsers) {
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
	
	public void loadUsersInfo() throws IOException {
		ApiUserInfo[] apiUsersInfo = (ApiUserInfo[]) this.remoteClient.get("/users/info", null);
		
		for (ApiUserInfo apiUserInfo : apiUsersInfo) {
			User user = this.getUserOrGuestById(apiUserInfo.id);
			
			if (user != null) {
				user.setScore(apiUserInfo.score);
				user.setBalance(apiUserInfo.balance);
			} else {
				Log.w(this.getClass().getName(), "Received infor for user with ID " + apiUserInfo.id + ", but user not in cache.");
			}
		}
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
		ApiUser[] apiGuests = (ApiUser[]) this.remoteClient.get("/guests", null);
		
		for (ApiUser apiGuest : apiGuests) {
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
	
	private Transaction convertToTransaction(ApiTransaction apiTransaction) {
		Transaction transaction = new Transaction();
		
		transaction.setId(apiTransaction.id);
		transaction.setDescription(apiTransaction.description);
		transaction.setDateCreated(apiTransaction.date_created);
		
		for (ApiTransactionItem apiTransactionItem : apiTransaction.transaction_items) {
			TransactionItem transactionItem = new TransactionItem();
			
			transactionItem.setUser(this.getUserOrGuestById(apiTransactionItem.executing_user));
			transactionItem.setPayer(this.getUserOrGuestById(apiTransactionItem.accounted_user));
			transactionItem.setProduct(this.getProductById(apiTransactionItem.product));
			transactionItem.setAmount(-1 * apiTransactionItem.count);
			
			// TODO: validatie
			transaction.add(transactionItem);
		}
		
		return transaction;
	}
	
	public void loadTransactions() throws IOException {
		ApiTransaction[] apiTransactions = (ApiTransaction[]) this.remoteClient.get("/transactions", null);
		
		for (ApiTransaction apiTransaction : apiTransactions) {
			this.transactionCache.put(apiTransaction.id, this.convertToTransaction(apiTransaction));
		}
		
		this.transactionsAreLoaded = true;
	}
	
	public boolean saveTransaction(Transaction transaction) throws IOException {
		ApiTransaction apiTransaction = new ApiTransaction();
		
		apiTransaction.description = transaction.getDescription();
		apiTransaction.transaction_items = new ApiTransactionItem[transaction.size()];
		
		for (int i = 0; i < transaction.size(); i++) {
			TransactionItem transactionItem = transaction.get(i);
			ApiTransactionItem apiTransactionItem = new ApiTransactionItem();
			
			apiTransactionItem.accounted_user = transactionItem.getPayer().getId();
			apiTransactionItem.executing_user = transactionItem.getUser().getId();
			apiTransactionItem.count = -1 * transactionItem.getAmount();
			apiTransactionItem.product = transactionItem.getProduct().getId();
			
			apiTransaction.transaction_items[i] = apiTransactionItem;
		}
		
		// Send to server
		Object result= this.remoteClient.post(apiTransaction, "/transactions/", null);
		
		// Parse result
		if (result != null) {
			apiTransaction = (ApiTransaction) result;
			this.transactionCache.put(apiTransaction.id, this.convertToTransaction(apiTransaction));
			
			// Load new user info
			this.loadUsersInfo();
			
			// Done
			return true;
		} else {
			return false;
		}
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
		ApiProduct[] apiProducts = (ApiProduct[]) this.remoteClient.get("/products", null);
		
		for (ApiProduct apiProduct : apiProducts) {
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
