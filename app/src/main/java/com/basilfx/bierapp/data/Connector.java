package com.basilfx.bierapp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.http.auth.AuthenticationException;

import android.util.Pair;

import com.basilfx.bierapp.data.models.Balance;
import com.basilfx.bierapp.data.models.Product;
import com.basilfx.bierapp.data.models.Stats;
import com.basilfx.bierapp.data.models.Transaction;
import com.basilfx.bierapp.data.models.TransactionItem;
import com.basilfx.bierapp.data.models.User;
import com.basilfx.bierapp.database.DatabaseHelper;
import com.basilfx.bierapp.database.ProductBalanceHelper;
import com.basilfx.bierapp.database.ProductHelper;
import com.basilfx.bierapp.database.StatsHelper;
import com.basilfx.bierapp.database.TransactionHelper;
import com.basilfx.bierapp.database.TransactionItemHelper;
import com.basilfx.bierapp.database.UserHelper;
import com.basilfx.bierapp.exceptions.RetriesExceededException;
import com.basilfx.bierapp.exceptions.UnexpectedData;
import com.basilfx.bierapp.exceptions.UnexpectedStatusCode;
import com.basilfx.bierapp.exceptions.UnexpectedUrl;
import com.google.common.collect.Lists;

public class Connector {

	private RemoteClient remoteClient;
	private DatabaseHelper databaseHelper;
	
	public Connector(RemoteClient remoteClient, DatabaseHelper databaseHelper) {
		this.remoteClient = checkNotNull(remoteClient);
		this.databaseHelper = checkNotNull(databaseHelper);
	}
	
	//
	// Users
	//
	
	public void loadUsers() throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException, RetriesExceededException {
		UserHelper userHelper = new UserHelper(this.databaseHelper);
		List<Integer> userIds = Lists.newArrayList();
		List<ApiUser> apiUsers = Lists.newArrayList();
		
		ApiUserPage apiUserPage = (ApiUserPage) this.remoteClient.get("/users?limit=50");
		
		while (true) {
			for (ApiUser apiUser : apiUserPage.results) {
				userIds.add(apiUser.id);
				apiUsers.add(apiUser);
			}
			
			// Fetch next page
			if (apiUserPage.next == null) {
				break;
			} else {
				apiUserPage = (ApiUserPage) this.remoteClient.get(apiUserPage.next);
				checkNotNull(apiUserPage.previous);
			}
		}
		
		Map<Integer, User> users = userHelper.select()
			.whereIdIn(userIds)
			.asMap();
		
		for (ApiUser apiUser : apiUsers) {
			boolean created = false;
			User user = null;
			
			if (users.containsKey(apiUser.id)) {
				user = users.get(apiUser.id);
				
				// Check if local user is older
				if (user.getModified() != null) {
					if (user.getModified().equals(apiUser.modified)) {
						continue;
					}
				}
			} else {
				user = new User();
				created = true;
			}
			
			user.setId(apiUser.id);
			user.setFirstName(apiUser.first_name);
			user.setLastName(apiUser.last_name);
			user.setAvatarUrl(apiUser.avatar);
			user.setRole(apiUser.role);
			user.setModified(apiUser.modified);
			user.setCreated(apiUser.created);
			
			user.setDirty(false);
			user.setSynced(true);
			
			// Save changes to database
			if (created) {
				userHelper.create(user);
			} else {
				userHelper.update(user);
			}
		}
	}
	
	public void loadUserInfo() throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException, RetriesExceededException {
		UserHelper userHelper = new UserHelper(this.databaseHelper);
		ProductHelper productHelper = new ProductHelper(this.databaseHelper);
		ProductBalanceHelper productBalanceHelper = new ProductBalanceHelper(this.databaseHelper);
		
		List<Integer> userIds = Lists.newArrayList();
		List<Integer> productIds = Lists.newArrayList();
		List<ApiUser> apiUsers = Lists.newArrayList();
		
		ApiUserPage apiUserPage = (ApiUserPage) this.remoteClient.get("/users/info?limit=50");
		
		while (true) {
			for (ApiUser apiUser : apiUserPage.results) {
				userIds.add(apiUser.id);
				apiUsers.add(apiUser);
				
				for (ApiBalance apiBalanceInfo : apiUser.balance) {
					productIds.add(apiBalanceInfo.product);
				}
			}
			
			// Fetch next page
			if (apiUserPage.next == null) {
				break;
			} else {
				apiUserPage = (ApiUserPage) this.remoteClient.get(apiUserPage.next);
				checkNotNull(apiUserPage.previous);
			}
		}
		
		Map<Integer, User> users = userHelper.select()
			.whereIdIn(userIds)
			.asMap();
		Map<Integer, Product> products = productHelper.select()
			.whereIdIn(productIds)
			.asMap();
		Map<Pair<Integer, Integer>, Balance> productBalances = productBalanceHelper.select()
			.whereUserIdIn(userIds)
			.whereProductIdIn(productIds)
			.asUserProductMap();
		
		for (ApiUser apiUser : apiUsers) {
			User user = users.get(apiUser.id);

			user.setXp(apiUser.xp);
			
			userHelper.update(user);
			
			for (ApiBalance apiBalanceInfo: apiUser.balance) {
				Product product = products.get(apiBalanceInfo.product);
				Pair<Integer, Integer> key = Pair.create(user.getId(), product.getId());
				
				Balance productBalance;
				boolean created = false;

				if (productBalances.containsKey(key)) {
					productBalance = productBalances.get(key);
				} else {
					productBalance = new Balance();
					created = true;
				}
				
				productBalance.setUser(user);
				productBalance.setProduct(product);
				productBalance.setCount(apiBalanceInfo.count);
				productBalance.setValue(apiBalanceInfo.value);
				productBalance.setEstimatedCount(apiBalanceInfo.estimated_count);
				
				if (created) {
					productBalanceHelper.create(productBalance);
				} else {
					productBalanceHelper.update(productBalance);
				}
			}
		}
	}
	
	//
	// Transactions
	//
	
	// http://stackoverflow.com/questions/12885499/problems-saving-collection-using-ormlite-on-android
	private Transaction convertToTransaction(ApiTransaction apiTransaction) throws IOException, SQLException {
		Transaction transaction = new Transaction();
		
		transaction.setRemoteId(apiTransaction.id);
		transaction.setDescription(apiTransaction.description);
		transaction.setCreated(apiTransaction.created);
		transaction.setModified(apiTransaction.modified);
		
		new TransactionHelper(this.databaseHelper).create(transaction);
		
		UserHelper userHelper = new UserHelper(this.databaseHelper);
		ProductHelper productHelper = new ProductHelper(this.databaseHelper);
		
		List<Integer> userIds = Lists.newArrayList();
		List<Integer> payerIds = Lists.newArrayList();
		List<Integer> productIds = Lists.newArrayList();
		
		for (ApiTransactionItem apiTransactionItem : apiTransaction.transaction_items) {
			userIds.add(apiTransactionItem.executing_user);
			payerIds.add(apiTransactionItem.accounted_user);
			productIds.add(apiTransactionItem.product);
		}
		
		Map<Integer, User> users = userHelper.select().whereIdIn(userIds).asMap();
		Map<Integer, User> payers = userHelper.select().whereIdIn(payerIds).asMap();
		Map<Integer, Product> products = productHelper.select().whereIdIn(productIds).asMap();
		
		for (ApiTransactionItem apiTransactionItem : apiTransaction.transaction_items) {
			TransactionItem transactionItem = new TransactionItem();
			
			transactionItem.setRemoteId(apiTransactionItem.id);
			transactionItem.setUser(users.get(apiTransactionItem.executing_user));
			transactionItem.setPayer(payers.get(apiTransactionItem.accounted_user));
			transactionItem.setProduct(products.get(apiTransactionItem.product));
			transactionItem.setCount(apiTransactionItem.count);
			transactionItem.setTransaction(transaction);
			
			this.databaseHelper.getTransactionItemDao().create(transactionItem);
		}
		
		// Done
		return transaction;
	}
	
	public void loadTransactions() throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException, RetriesExceededException {
		TransactionHelper transactionHelper = new TransactionHelper(this.databaseHelper);
		List<Integer> transactionIds = Lists.newArrayList();
		List<ApiTransaction> apiTransactions = Lists.newArrayList();
		
		ApiTransactionPage apiTransactionPage = (ApiTransactionPage) this.remoteClient.get("/transactions?limit=50");
		
		while (true) {
			for (ApiTransaction apiTransaction : apiTransactionPage.results) {
				transactionIds.add(apiTransaction.id);
				apiTransactions.add(apiTransaction);
			}
		
			// Fetch next page
			if (apiTransactionPage.next == null) {
				break;
			} else {
				if (transactionIds.size() < 250) {
					apiTransactionPage = (ApiTransactionPage) this.remoteClient.get(apiTransactionPage.next);
					checkNotNull(apiTransactionPage.previous);
				} else {
					break;
				}
			}
		}

		transactionIds = transactionHelper.select()
			.selectRemoteIds()
			.whereRemoteIdIn(transactionIds)
			.asIntList();
		
		for (ApiTransaction apiTransaction : apiTransactions) {
			if (!transactionIds.contains(apiTransaction.id)) {
				this.convertToTransaction(apiTransaction);
			}
		}
	}
	
	public Transaction saveTransaction(Transaction transaction) throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException, RetriesExceededException {
		ApiTransaction apiTransaction = new ApiTransaction();
		int i = 0;
		
		List<TransactionItem> transactionItems = new TransactionItemHelper(this.databaseHelper).select()
			.whereTransactionIdEq(transaction.getId())
			.groupByUserAndProduct()
			.all();
		
		apiTransaction.description = transaction.getDescription();
		apiTransaction.transaction_items = new ApiTransactionItem[transactionItems.size()];
		
		for (TransactionItem transactionItem : transactionItems) {
			ApiTransactionItem apiTransactionItem = new ApiTransactionItem();
			
			apiTransactionItem.accounted_user = transactionItem.getPayer().getId();
			apiTransactionItem.executing_user = transactionItem.getUser().getId();
			apiTransactionItem.count = transactionItem.getCount();
			apiTransactionItem.product = transactionItem.getProduct().getId();
			
			apiTransaction.transaction_items[i] = apiTransactionItem;
			i++;
		}
		
		// Send to server
		Object result = this.remoteClient.post(apiTransaction, "/transactions/");
		
		// Parse result
		if (result == null) {
			return null;
		}
		
		// Convert to ApiTransaction
		apiTransaction = (ApiTransaction) result;
		
		// Remove the old transaction
		new TransactionHelper(this.databaseHelper).delete(transaction);
		
		// Save a new one
		return this.convertToTransaction(apiTransaction);
	}
	
	//
	// Products
	//
	
	public void loadProducts() throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException, RetriesExceededException {
		ProductHelper productHelper = new ProductHelper(this.databaseHelper);
		List<Integer> productIds = Lists.newArrayList();
		List<ApiProduct> apiProducts = Lists.newArrayList();
		
		ApiProductPage apiProductPage = (ApiProductPage) this.remoteClient.get("/products?limit=50");
		
		while (true) {
			for (ApiProduct apiProduct : apiProductPage.results) {
				productIds.add(apiProduct.id);
				apiProducts.add(apiProduct);
			}
			
			// Fetch next page
			if (apiProductPage.next == null) {
				break;
			} else {
				apiProductPage = (ApiProductPage) this.remoteClient.get(apiProductPage.next);
				checkNotNull(apiProductPage.previous);
			}
		}
		
		Map<Integer, Product> products = productHelper.select()
			.whereIdIn(productIds)
			.asMap();
		
		for (ApiProduct apiProduct : apiProducts) {
			boolean created = false;
			Product product = null;
			
			if (products.containsKey(apiProduct.id)) {
				product = products.get(apiProduct.id);
				
				// Check if local user is older
				if (product.getModified() != null) {
					if (product.getModified().equals(apiProduct.modified)) {
						continue;
					}
				}
			} else {
				product = new Product();
				created = true;
			}
			
			product.setId(apiProduct.id);
			product.setTitle(apiProduct.title);
			product.setCost(apiProduct.cost);
			product.setLogo(apiProduct.logo);
			product.setCreated(apiProduct.created);
			product.setModified(apiProduct.modified);
			
			// Save changes to database
			if (created) {
				productHelper.create(product);
			} else {
				productHelper.update(product);
			}
		}
	}
	
	//
	// Stats
	//
	
	public void loadStats(Calendar dayEnd, int days) throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException, RetriesExceededException {
		StatsHelper statsHelper = new StatsHelper(this.databaseHelper); 
		
		// Determine overflow point
		Calendar now = new GregorianCalendar();
		Calendar overflow = new GregorianCalendar();
		
		overflow.set(Calendar.HOUR_OF_DAY, dayEnd.get(Calendar.HOUR_OF_DAY));
		overflow.set(Calendar.MINUTE, dayEnd.get(Calendar.MINUTE));
		overflow.set(Calendar.SECOND, 0);
		overflow.set(Calendar.MILLISECOND, 0);
		
		if (now.before(overflow)) {
			overflow.add(Calendar.DAY_OF_YEAR, -1);
		}
		
		// Fetch page
		ApiStats apiStats = (ApiStats) this.remoteClient.get("/stats/?after=" + overflow.get(Calendar.YEAR) + "-" + (overflow.get(Calendar.MONTH) + 1) + "-" + overflow.get(Calendar.DAY_OF_MONTH) + "+" + overflow.get(Calendar.HOUR_OF_DAY) + "%3A" + overflow.get(Calendar.MINUTE));
		
		Stats stats = new Stats();
		stats.setCount(apiStats.count);
		
		// Save changes to database
		statsHelper.create(stats);
	}
}
