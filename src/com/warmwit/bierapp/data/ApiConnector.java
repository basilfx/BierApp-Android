package com.warmwit.bierapp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.http.auth.AuthenticationException;

import android.util.Pair;

import com.google.common.collect.Lists;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.data.models.UserInfo;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.ProductHelper;
import com.warmwit.bierapp.database.TransactionHelper;
import com.warmwit.bierapp.database.TransactionItemHelper;
import com.warmwit.bierapp.database.UserHelper;
import com.warmwit.bierapp.database.UserInfoHelper;
import com.warmwit.bierapp.exceptions.UnexpectedData;
import com.warmwit.bierapp.exceptions.UnexpectedStatusCode;
import com.warmwit.bierapp.exceptions.UnexpectedUrl;

public class ApiConnector {

	private RemoteClient remoteClient;
	private DatabaseHelper databaseHelper;
	
	public ApiConnector(RemoteClient remoteClient, DatabaseHelper databaseHelper) {
		this.remoteClient = checkNotNull(remoteClient);
		this.databaseHelper = checkNotNull(databaseHelper);
	}
	
	//
	// Users
	//
	
	public void loadUsers() throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException {
		UserHelper userHelper = new UserHelper(this.databaseHelper);
		List<Integer> userIds = Lists.newArrayList();
		
		ApiUserPage apiUserPage = (ApiUserPage) this.remoteClient.get("/users?limit=50");
		
		while (true) {
			for (ApiUser apiUser : apiUserPage.results) {
				userIds.add(apiUser.id);
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
		
		for (ApiUser apiUser : apiUserPage.results) {
			boolean created = false;
			User user = null;
			
			if (users.containsKey(apiUser.id)) {
				user = users.get(apiUser.id);
				
				// Check if local user is older
				if (user.getDateChanged() != null) {
					if (user.getDateChanged().equals(apiUser.date_changed)) {
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
			user.setType(apiUser.user_type);
			user.setDateChanged(apiUser.date_changed);
			
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
	
	public void loadUserInfo() throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException {
		UserHelper userHelper = new UserHelper(this.databaseHelper);
		ProductHelper productHelper = new ProductHelper(this.databaseHelper);
		UserInfoHelper userInfoHelper = new UserInfoHelper(this.databaseHelper);
		
		List<Integer> userIds = Lists.newArrayList();
		List<Integer> productIds = Lists.newArrayList();
		
		ApiUserPage apiUserPage = (ApiUserPage) this.remoteClient.get("/users/info?limit=50");
		
		while (true) {
			for (ApiUser apiUser : apiUserPage.results) {
				userIds.add(apiUser.id);
				
				for (ApiUserInfo apiUserInfo : apiUser.product_info) {
					productIds.add(apiUserInfo.product);
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
		Map<Pair<Integer, Integer>, UserInfo> userInfos = userInfoHelper.select()
			.whereUserIdIn(userIds)
			.whereProductIdIn(productIds)
			.asUserProductMap();
		
		for (ApiUser apiUser : apiUserPage.results) {
			User user = users.get(apiUser.id);
			
			for (ApiUserInfo apiUserInfo : apiUser.product_info) {
				Product product = products.get(apiUserInfo.product);
				Pair<Integer, Integer> key = Pair.create(user.getId(), product.getId());
				
				UserInfo userInfo;
				boolean created = false;

				if (userInfos.containsKey(key)) {
					userInfo = userInfos.get(key);
				} else {
					userInfo = new UserInfo();
					created = true;
				}
				
				userInfo.setUser(user);
				userInfo.setProduct(product);
				userInfo.setCount(apiUserInfo.count);
				userInfo.setValue(apiUserInfo.value);
				userInfo.setEstimatedCount(apiUserInfo.estimated_count);
				
				if (created) {
					userInfoHelper.create(userInfo);
				} else {
					userInfoHelper.update(userInfo);
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
		transaction.setDateCreated(apiTransaction.date_created);
		
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
	
	public void loadTransactions() throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException {
		TransactionHelper transactionHelper = new TransactionHelper(this.databaseHelper);
		List<Integer> transactionIds = Lists.newArrayList();
		
		ApiTransactionPage apiTransactionPage = (ApiTransactionPage) this.remoteClient.get("/transactions?limit=50");
		
		while (true) {
			for (ApiTransaction apiTransaction : apiTransactionPage.results) {
				transactionIds.add(apiTransaction.id);
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
		
		for (ApiTransaction apiTransaction : apiTransactionPage.results) {
			if (!transactionIds.contains(apiTransaction.id)) {
				this.convertToTransaction(apiTransaction);
			}
		}
	}
	
	public Transaction saveTransaction(Transaction transaction) throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException {
		ApiTransaction apiTransaction = new ApiTransaction();
		int i = 0;
		
		List<TransactionItem> transactionItems = new TransactionItemHelper(this.databaseHelper).select()
			.whereTransactionIdEq(transaction.getId())
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
	
	public void loadProducts() throws IOException, SQLException, UnexpectedStatusCode, UnexpectedData, UnexpectedUrl, AuthenticationException {
		ProductHelper productHelper = new ProductHelper(this.databaseHelper);
		List<Integer> productIds = Lists.newArrayList();
		
		ApiProductPage apiProductPage = (ApiProductPage) this.remoteClient.get("/products?limit=5");
		
		while (true) {
			for (ApiProduct apiProduct : apiProductPage.results) {
				productIds.add(apiProduct.id);
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
		
		for (ApiProduct apiProduct : apiProductPage.results) {
			boolean created = false;
			Product product = null;
			
			if (products.containsKey(apiProduct.id)) {
				product = products.get(apiProduct.id);
				
				// Check if local user is older
				if (product.getDateChanged() != null) {
					if (product.getDateChanged().equals(apiProduct.date_changed)) {
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
			product.setDateChanged(apiProduct.date_changed);
			
			// Save changes to database
			if (created) {
				productHelper.create(product);
			} else {
				productHelper.update(product);
			}
		}
	}
}
