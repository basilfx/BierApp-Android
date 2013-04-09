package com.warmwit.bierapp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.SQLException;

import android.util.Log;

import com.j256.ormlite.dao.ForeignCollection;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.UserInfo;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.ProductQuery;
import com.warmwit.bierapp.database.UserQuery;

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
	
	public void loadUsers() throws IOException, SQLException {
		ApiUser[] apiUsers = (ApiUser[]) this.remoteClient.get("/users", null);
		
		for (ApiUser apiUser : apiUsers) {
			if (!this.databaseHelper.getUserDao().idExists(apiUser.id)) {
				User user = new User();
				
				user.setId(apiUser.id);
				user.setFirstName(apiUser.first_name);
				user.setLastName(apiUser.last_name);
				user.setAvatarUrl(apiUser.avatar);
				user.setType(apiUser.type);
				
				user.setDirty(false);
				user.setSynced(true);
				
				// Save changes to database
				this.databaseHelper.getUserDao().create(user);
			}
		}
	}
	
	public void loadUserInfo() throws IOException, SQLException {
		UserQuery userQuery = new UserQuery(this.databaseHelper);
		ProductQuery productQuery = new ProductQuery(this.databaseHelper);
		
		ApiUser[] apiUsers = (ApiUser[]) this.remoteClient.get("/users/info", null);
		
		for (ApiUser apiUser : apiUsers) {
			User user = userQuery.byId(apiUser.id);
			
			for (ApiUserInfo apiUserInfo : apiUser.product_info) {
				Product product = productQuery.byId(apiUserInfo.product_id);
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
	private void convertToTransaction(ApiTransaction apiTransaction) throws IOException, SQLException {
		Transaction transaction = new Transaction();
		ForeignCollection<TransactionItem> transactionItems = 
			this.databaseHelper.getTransactionDao().getEmptyForeignCollection("transactionItems");
		
		transaction.setId(apiTransaction.id);
		transaction.setDescription(apiTransaction.description);
		transaction.setDateCreated(apiTransaction.date_created);
		transaction.setTransactionItems(transactionItems);
		transaction.setDirty(false);
		transaction.setSynced(true);
		
		this.databaseHelper.getTransactionDao().create(transaction);
		
		for (ApiTransactionItem apiTransactionItem : apiTransaction.transaction_items) {
			TransactionItem transactionItem = new TransactionItem();
			
			transactionItem.setId(apiTransactionItem.id);
			transactionItem.setUser(this.databaseHelper.getUserDao().queryForId(apiTransactionItem.executing_user_id));
			transactionItem.setPayer(this.databaseHelper.getUserDao().queryForId(apiTransactionItem.accounted_user_id));
			transactionItem.setProduct(this.databaseHelper.getProductDao().queryForId(apiTransactionItem.product_id));
			transactionItem.setCount(apiTransactionItem.count);
			transactionItem.setTransaction(transaction);
			
			transactionItems.add(transactionItem);
		}
	}
	
	public void loadTransactions() throws IOException, SQLException {
		ApiTransaction[] apiTransactions = (ApiTransaction[]) this.remoteClient.get("/transactions", null);
		
		for (ApiTransaction apiTransaction : apiTransactions) {
			if (!this.databaseHelper.getTransactionDao().idExists(apiTransaction.id)) {
				this.convertToTransaction(apiTransaction);
			}
		}
	}
	
	public boolean saveTransaction(Transaction transaction) throws IOException, SQLException {
		ApiTransaction apiTransaction = new ApiTransaction();
		ForeignCollection<TransactionItem> transactionItems = transaction.getTransactionItems();
		int i = 0;
		
		apiTransaction.description = transaction.getDescription();
		apiTransaction.transaction_items = new ApiTransactionItem[transactionItems.size()];
		
		for (TransactionItem transactionItem : transactionItems) {
			ApiTransactionItem apiTransactionItem = new ApiTransactionItem();
			
			apiTransactionItem.accounted_user_id = transactionItem.getPayer().getId();
			apiTransactionItem.executing_user_id = transactionItem.getUser().getId();
			apiTransactionItem.count = transactionItem.getCount();
			apiTransactionItem.product_id = transactionItem.getProduct().getId();
			
			apiTransaction.transaction_items[i] = apiTransactionItem;
			i++;
		}
		
		// Send to server
		Object result= this.remoteClient.post(apiTransaction, "/transactions/", null);
		
		// Parse result
		if (result != null) {
			apiTransaction = (ApiTransaction) result;
			
			// Save changes to database
			this.databaseHelper.getTransactionDao().createOrUpdate(transaction);
			
			// Done
			return true;
		} else {
			return false;
		}
	}
	
	//
	// Products
	//
	
	public void loadProducts() throws IOException, SQLException {
		ApiProduct[] apiProducts = (ApiProduct[]) this.remoteClient.get("/products", null);
		
		for (ApiProduct apiProduct : apiProducts) {
			if (!this.databaseHelper.getProductDao().idExists(apiProduct.id)) {
				Product product = new Product();
				
				product.setId(apiProduct.id);
				product.setTitle(apiProduct.title);
				product.setCost(apiProduct.cost);
				product.setLogo(apiProduct.logo);
				
				// Save changes to database
				this.databaseHelper.getProductDao().create(product);
			}
		}
	}
}
