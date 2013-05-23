package com.warmwit.bierapp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.http.auth.AuthenticationException;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.data.models.UserInfo;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.ProductQuery;
import com.warmwit.bierapp.database.TransactionItemQuery;
import com.warmwit.bierapp.database.TransactionQuery;
import com.warmwit.bierapp.database.UserQuery;
import com.warmwit.bierapp.exceptions.UnexpectedStatusCode;

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
	
	public void loadUsers() throws IOException, SQLException, UnexpectedStatusCode, AuthenticationException {
		ApiUserPage apiUserPage = (ApiUserPage) this.remoteClient.get("/users/", null);
		UserQuery userQuery = new UserQuery(this.databaseHelper);
		
		for (ApiUser apiUser : apiUserPage.results) {
			if (userQuery.shouldSync(apiUser.id, apiUser.date_changed)) {
				User user = new User();
				
				user.setId(apiUser.id);
				user.setFirstName(apiUser.first_name);
				user.setLastName(apiUser.last_name);
				user.setAvatarUrl(apiUser.avatar);
				user.setType(apiUser.user_type);
				user.setDateChanged(apiUser.date_changed);
				
				user.setDirty(false);
				user.setSynced(true);
				
				// Save changes to database
				this.databaseHelper.getUserDao().createOrUpdate(user);
			}
		}
	}
	
	public void loadUserInfo() throws IOException, SQLException, UnexpectedStatusCode, AuthenticationException {
		UserQuery userQuery = new UserQuery(this.databaseHelper);
		ProductQuery productQuery = new ProductQuery(this.databaseHelper);
		
		ApiUserPage apiUserPage = (ApiUserPage) this.remoteClient.get("/users/info/", null);
		
		for (ApiUser apiUser : apiUserPage.results) {
			User user = userQuery.byId(apiUser.id);
			
			for (ApiUserInfo apiUserInfo : apiUser.product_info) {
				Product product = productQuery.byId(apiUserInfo.product);
				UserInfo userInfo = userQuery.userProductInfo(user, product);
				boolean exists = true;

				if (userInfo == null) {
					userInfo = new UserInfo();
					exists = false;
				}
				
				userInfo.setUser(user);
				userInfo.setProduct(product);
				userInfo.setCount(apiUserInfo.count);
				
				if (exists) {
					this.databaseHelper.getUserInfoDao().update(userInfo);
				} else {
					this.databaseHelper.getUserInfoDao().create(userInfo);
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
		
		transaction.setId(apiTransaction.id);
		transaction.setDescription(apiTransaction.description);
		transaction.setDateCreated(apiTransaction.date_created);
		transaction.setDirty(false);
		transaction.setSynced(true);
		
		this.databaseHelper.getTransactionDao().create(transaction);
		
		for (ApiTransactionItem apiTransactionItem : apiTransaction.transaction_items) {
			TransactionItem transactionItem = new TransactionItem();
			
			transactionItem.setId(apiTransactionItem.id);
			transactionItem.setUser(this.databaseHelper.getUserDao().queryForId(apiTransactionItem.executing_user));
			transactionItem.setPayer(this.databaseHelper.getUserDao().queryForId(apiTransactionItem.accounted_user));
			transactionItem.setProduct(this.databaseHelper.getProductDao().queryForId(apiTransactionItem.product));
			transactionItem.setCount(apiTransactionItem.count);
			transactionItem.setTransaction(transaction);
			
			this.databaseHelper.getTransactionItemDao().create(transactionItem);
		}
		
		// Done
		return transaction;
	}
	
	public void loadTransactions() throws IOException, SQLException, UnexpectedStatusCode, AuthenticationException {
		ApiTransactionPage apiTransactionPage = (ApiTransactionPage) this.remoteClient.get("/transactions/", null);
		
		for (ApiTransaction apiTransaction : apiTransactionPage.results) {
			if (!this.databaseHelper.getTransactionDao().idExists(apiTransaction.id)) {
				this.convertToTransaction(apiTransaction);
			}
		}
	}
	
	public Transaction saveTransaction(Transaction transaction) throws IOException, SQLException, UnexpectedStatusCode, AuthenticationException {
		ApiTransaction apiTransaction = new ApiTransaction();
		int i = 0;
		
		List<TransactionItem> transactionItems = new TransactionItemQuery(this.databaseHelper).byTransaction(transaction);
		
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
		Object result = this.remoteClient.post(apiTransaction, "/transactions/", null);
		
		// Parse result
		if (result == null) {
			return null;
		}
		
		// Convert to ApiTransaction
		apiTransaction = (ApiTransaction) result;
		
		// Remove the old transaction
		new TransactionQuery(this.databaseHelper).delete(transaction);
		
		// Save a new one
		return this.convertToTransaction(apiTransaction);
	}
	
	//
	// Products
	//
	
	public void loadProducts() throws IOException, SQLException, UnexpectedStatusCode, AuthenticationException {
		ApiProductPage apiProductPage = (ApiProductPage) this.remoteClient.get("/products/", null);
		ProductQuery productQuery = new ProductQuery(this.databaseHelper);
		
		for (ApiProduct apiProduct : apiProductPage.results) {
			if (productQuery.shouldSync(apiProduct.id, apiProduct.date_changed)) {
				Product product = new Product();
				
				product.setId(apiProduct.id);
				product.setTitle(apiProduct.title);
				product.setCost(apiProduct.cost);
				product.setLogo(apiProduct.logo);
				product.setDateChanged(apiProduct.date_changed);
				
				// Save changes to database
				this.databaseHelper.getProductDao().createOrUpdate(product);
			}
		}
	}
}
