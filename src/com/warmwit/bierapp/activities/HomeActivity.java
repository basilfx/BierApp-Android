package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.ProductClickedCallback;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.data.RemoteClient;
import com.warmwit.bierapp.data.adapters.UserListAdapter;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.data.models.UserInfo;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.HostQuery;
import com.warmwit.bierapp.database.ProductQuery;
import com.warmwit.bierapp.database.TransactionQuery;
import com.warmwit.bierapp.database.UserQuery;
import com.warmwit.bierapp.utils.ProductInfo;
import com.warmwit.bierapp.views.UserRowView;

public class HomeActivity extends OrmLiteBaseActivity<DatabaseHelper> implements ProductClickedCallback {
	private RemoteClient remoteClient;
	private ApiConnector apiConnector;
	
	private int[] userRandomAvatars;
	private UserListAdapter userListAdapter; 
	private ListView userListView;
	private MenuItem purchaseMenu;
	
	private List<User> inhabitants;
	private List<User> guests;
	private List<Product> products;
	
	/**
	 * @var Reference to an on-going transaction
	 */
	private Transaction transaction;
	private int amount;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.remoteClient = ((BierAppApplication) this.getApplication()).getRemoteClient();
		this.apiConnector = new ApiConnector(remoteClient, this.getHelper());
        
        // Set content
        this.setContentView(R.layout.activity_home);
        
        // Bind controls
        this.userListView = (ListView) this.findViewById(R.id.list_users);
        
        // Restore state data
 		if (savedInstanceState != null) {
 			this.userRandomAvatars = savedInstanceState.getIntArray("userRandomAvatars");
 			
 			try {
 				this.transaction = this.getHelper().getTransactionDao().queryForId(
 					savedInstanceState.getInt("transactionId")
				);
 				
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			// In advance, declare size of array
 			int size = new UserQuery(this).count();
 			this.userRandomAvatars = new int[size];
 		}
        
 		// Bind data
 		this.initList();
    	this.refreshList();
    	
    	if (this.transaction != null) {
    		TransactionQuery transactionQuery = new TransactionQuery(this);
    		this.amount = transactionQuery.costByTransaction(this.transaction); 
    	}
    }
    
    private void initList() {
    	this.userListAdapter = new UserListAdapter(this, this);
    	
    	// Create sectionizer to seperate guests from inhabitants
    	SimpleSectionAdapter<User> sectionAdapter = new SimpleSectionAdapter<User>(
			this, this.userListAdapter, R.layout.listview_row_header, R.id.header, new Sectionizer<User>() {
			@Override
			public String getSectionTitleForItem(User instance) {
				if (instance.getType() == User.INHABITANT) {
					return "Bewoners";
				} else if (instance.getType() == User.GUEST) {
					return "Gasten";
				}
				
				// Not good
				return null;
			}		
		});
    	
	    // Set the adapter and display the data
    	this.userListView.setAdapter(sectionAdapter);
    	
    	// Add context menu to list
    	this.registerForContextMenu(this.userListView);
    }

    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
	    User user = (User) this.userListView.getAdapter().getItem(info.position);
        
        switch (user.getType()) {
        	case User.INHABITANT:
        		inflater.inflate(R.menu.menu_context_home_inhabitant, menu);
        		break;
        	case User.GUEST:
        		inflater.inflate(R.menu.menu_context_home_guest, menu);
        		break;
        }
        
        super.onCreateContextMenu(menu, v, menuInfo);
    }

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		final UserRowView view = (UserRowView) info.targetView;
		final User user = view.getUser();
		
    	switch (item.getItemId()) {
    		case R.id.menu_context_add_guest:
    			{
	    			final ArrayAdapter<User> adapter = new ArrayAdapter<User>(
						this, 
						android.R.layout.simple_list_item_single_choice, 
						new UserQuery(this).inactiveGuests()
	    			);
	    			
	    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    		    builder.setTitle("Kies een gast");
	    		    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final User guest = adapter.getItem(which);
							final List<User> users = HomeActivity.this.inhabitants;
							
							final String[] names = new String[users.size()];
							final boolean[] states = new boolean[users.size()];
							
							for (int i = 0; i < users.size(); i++) {
								names[i] = users.get(i).getName();
								states[i] = users.get(i) == user;
							}
							
							AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
							builder.setTitle("Kies de host(s)");
							builder.setMultiChoiceItems(
								names, 
								states,
								new DialogInterface.OnMultiChoiceClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which, boolean isChecked) {
										states[which] = isChecked;
									}
								}
							);
							builder.setPositiveButton("Toevoegen", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									List<User> hosts = Lists.newArrayList();
									
									for (int i = 0; i < states.length; i++) {
										if (states[i])
											hosts.add(users.get(i));
									}
	
									if (hosts.size() > 0) {
										HostQuery hostQuery = new HostQuery(HomeActivity.this);
										hostQuery.create(guest, hosts);
										
										// Reload data
										HomeActivity.this.refreshList();
										
										// Done
										Toast.makeText(HomeActivity.this, guest.getName() + " als gast toegevoegd.", Toast.LENGTH_LONG).show();
									} else {
										// Done
										Toast.makeText(HomeActivity.this, "Gast niet toegevoegd omdat er geen hosts geselecteerd zijn.", Toast.LENGTH_LONG).show();
									}
								}
							});
							builder.setNegativeButton("Annuleren", null);
							builder.show();
						}
	    		    });
	    		    
	    		    builder.show();
	    			
	    			return true;
    			}
    		case R.id.menu_context_remove_guest:
    			{
	    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    		    builder.setMessage("Weet je zeker dat je " + user.getName() + " wilt verwijderen?");
	    		    builder.setNegativeButton(android.R.string.no, null);
	    		    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Cancel transactions for this user
							HomeActivity.this.cancelTransaction(user);
							
							HostQuery hostQuery = new HostQuery(HomeActivity.this);
							hostQuery.delete(user);
							
							// Reload data
							HomeActivity.this.refreshList();
							HomeActivity.this.refreshMenu();
							
							// Done
							Toast.makeText(HomeActivity.this, user.getName() + " is verwijderd.", Toast.LENGTH_LONG).show();
						}
					});
	    		    
	    			builder.show();
	    		    
	    			return true;
    			}
    		case R.id.menu_context_clear_transaction_items:
    		    if (this.transaction != null) {
					// Set each product info to 0
					HomeActivity.this.cancelTransaction(user);
					
					// Refresh view
					view.refreshProducts();
					this.refreshMenu();
					
					// Done
					Toast.makeText(HomeActivity.this, "Transactie-items voor " + user.getName() + " gewist.", Toast.LENGTH_LONG).show();
    		    }
    		    
    			return true;
    		default:
    			return super.onContextItemSelected(item);
    	}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        
        // Create normal menu handler
        OnMenuItemClickListener generalHandler = new OnMenuItemClickListener() {
        	@Override
			public boolean onMenuItemClick(MenuItem item) {
        		Intent intent;
        		
				switch (item.getItemId()) {
					case R.id.menu_show_guests:
						// Switch to guests activity
						intent = new Intent(HomeActivity.this, GuestsActivity.class);
						startActivity(intent); 
						
						return true;
					case R.id.menu_show_transactions:
						// Switch to guests activity
						intent = new Intent(HomeActivity.this, TransactionActivity.class);
						startActivity(intent); 
						
						return true;
					case R.id.menu_show_stats:
						// Switch to guests activity
						intent = new Intent(HomeActivity.this, StatsActivity.class);
						startActivity(intent); 
						
						return true;
					case R.id.menu_refresh:
						// Refresh data
						if (HomeActivity.this.transaction != null) {
							AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
							builder.setMessage("Er is een huidige transactie gaande die gewist zal worden. Wil je doorgaan?");
							builder.setNegativeButton(android.R.string.no, null);
							builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									new LoadDataTask().execute();
								}
							});
							
							builder.show();
						} else {
							new LoadDataTask().execute();
						}
						
						return true;
					default:
						// Nothing to do
						return false;
				}
			}
		};
        
        // Create purchase menu handler
        OnMenuItemClickListener purchaseHandler = new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
					case R.id.menu_purchase_confirm:
						if (HomeActivity.this.transaction != null) {
							new SaveTransactionTask().execute();
						}
						
						return true;
					case R.id.menu_purchase_show:
						/*AlertDialog alertDialog;
						alertDialog = new AlertDialog.Builder(HomeActivity.this).create();
						alertDialog.setTitle("Huidige transactie");
						
						if (HomeActivity.this.transaction != null) {
							Multimap<User, TransactionItem> grouped = HomeActivity.this.transaction.groupByUser();
							StringBuilder message = new StringBuilder();
							
							// Walk through each transaction and display count
							for (User user : grouped.keySet()) {
								message.append(user.getFullName() + "\t\t" + grouped.get(user).size() + "x\n");
							}
							
							// Set the message
							alertDialog.setMessage(message.toString());
						}
						
						// Show dialog and done
						alertDialog.show();*/
						return true;
					case R.id.menu_purchase_cancel:
						// Cancel the transaction if there is one
						if (HomeActivity.this.transaction != null) {
							TransactionQuery transactionQuery = new TransactionQuery(HomeActivity.this);
							
							transactionQuery.delete(HomeActivity.this.transaction);
							HomeActivity.this.transaction = null;
						}
						
						for (User user : HomeActivity.this.inhabitants) {
							HomeActivity.this.cancelTransaction(user);
						}
						
						// Refresh visible rows
						HomeActivity.this.applyToVisibleRows(new Function<UserRowView, Boolean>() {
							@Override
							public Boolean apply(UserRowView view) {
								view.refreshProducts();
								return true;
							}
						});
						
						// Refresh global
						HomeActivity.this.amount = 0;
						HomeActivity.this.refreshMenu();
						
						// Display toast message
						Toast.makeText(HomeActivity.this, "Transactie gewist", Toast.LENGTH_LONG).show();
						
						return true;
					default:
						// Nothing to do
						return false;
				}
			}
		};
		
		// Save reference to purchase menu to hide/update it
		this.purchaseMenu = menu.findItem(R.id.menu_purchase);
		
		// Add handler to general menu
		menu.findItem(R.id.menu_show_guests).setOnMenuItemClickListener(generalHandler);
		menu.findItem(R.id.menu_show_stats).setOnMenuItemClickListener(generalHandler);
		menu.findItem(R.id.menu_show_transactions).setOnMenuItemClickListener(generalHandler);
		menu.findItem(R.id.menu_refresh).setOnMenuItemClickListener(generalHandler);
		
		// Add handler to purchase menu
		menu.findItem(R.id.menu_purchase_confirm).setOnMenuItemClickListener(purchaseHandler);
		menu.findItem(R.id.menu_purchase_show).setOnMenuItemClickListener(purchaseHandler);
		menu.findItem(R.id.menu_purchase_cancel).setOnMenuItemClickListener(purchaseHandler);
        
		// Set initial text
		this.refreshMenu();
		
		// Done		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Save all userRowItems to the bundle
		outState.putIntArray("userRandomAvatars", this.userRandomAvatars);
		
		// Save an on-going transaction, if any
		if (this.transaction != null) {
			outState.putInt("transactionId", this.transaction.getId());
		}
	}
	
	private void cancelTransaction(User user) {
		for (ProductInfo productInfo : user.getProducts().values()) {
			this.amount = this.amount - productInfo.getChange(); 
			productInfo.setChange(0);
		}
	}
	
	public void applyToVisibleRows(Function<UserRowView, Boolean> method) {
		int first = this.userListView.getFirstVisiblePosition();
		int last = this.userListView.getLastVisiblePosition();
		
		for (int i = first, j = last; i <= j; i++) {
			View view = this.userListView.getChildAt(i - first);
			
			if (view instanceof UserRowView) {
				if (method.apply((UserRowView) view) == false) {
					break;
				}
			}
		}
	}
	
	public void refreshMenu() {
		// Save or hide purchase menu based on amount.
		if (this.amount == 0) {
			this.purchaseMenu.setVisible(false);
		} else {
			this.purchaseMenu.setTitle((this.amount * -1) + " consumpties");
			this.purchaseMenu.setVisible(true);
		}
	}
	
	private void refreshList() {
    	TransactionQuery transactionQuery = new TransactionQuery(this);
		UserQuery userQuery = new UserQuery(this);
		ProductQuery productQuery = new ProductQuery(this);
		
		this.inhabitants = userQuery.inhabitants();
		this.guests = userQuery.activeGuests();
		this.products = productQuery.all();
		
		// Remove all current items
		this.userListAdapter.clear();
		
		// For each user, set the product info
		for (User user : Iterables.concat(this.inhabitants, this.guests)) {
    		Builder<Product, ProductInfo> builder = ImmutableMap.<Product, ProductInfo>builder();
    		
    		for (Product product : products) {
    			UserInfo userInfo = userQuery.userProductInfo(user, product); 
    			
    			if (this.transaction != null) {
    				int change = transactionQuery.costByUserAndProduct(
						this.transaction,
						user,
						product
    				);
    				
    				builder.put(product, new ProductInfo(userInfo.getCount(), change));
    			} else {
    				builder.put(product, new ProductInfo(userInfo.getCount(), 0));
    			}
    		}
    		
    		user.setProducts(builder.build());
    		this.userListAdapter.add(user);
    	}
		
		// Notify change
		((SimpleSectionAdapter) this.userListView.getAdapter()).notifyDataSetChanged();
    }
	
	@Override
	public void onProductClicked(UserRowView view, User user, Product product) {
		// Start a new transaction if needed
		if (HomeActivity.this.transaction == null) {
			TransactionQuery transactionQuery = new TransactionQuery(this); 
			this.transaction = transactionQuery.create("Verkoop vanaf Tablet");
		}
		
		// Create a transaction
		TransactionItem transactionItem = new TransactionItem();
		
		transactionItem.setCount(-1);
		transactionItem.setPayer(user);
		transactionItem.setUser(user);
		transactionItem.setProduct(product);
		transactionItem.setTransaction(this.transaction);
		
		// Add to the list
		this.transaction.getTransactionItems().add(transactionItem);
		
		// Refresh view
		this.amount--;
		this.refreshMenu();
		
		ProductInfo productInfo = user.getProducts().get(product);
		productInfo.setChange(productInfo.getChange() - 1);
		
		// Refresh the row
		view.refreshProducts();
	}

	private class LoadDataTask extends AsyncTask<Void, Void, Integer> {
		private ProgressDialog dialog;
		
		public LoadDataTask() {
	        this.dialog = new ProgressDialog(HomeActivity.this);
	        this.dialog.setMessage("Gegevens herladen");
	    }
		
		@Override
		protected void onPreExecute() {
	        this.dialog.show();
	        
	        // Stop ongoing transaction
 			if (HomeActivity.this.transaction != null) {
 				new TransactionQuery(HomeActivity.this).delete(HomeActivity.this.transaction);
 				HomeActivity.this.transaction = null;
 			}
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			// Refresh the data
			try {
				HomeActivity.this.apiConnector.loadProducts();
				HomeActivity.this.apiConnector.loadUsers();
				HomeActivity.this.apiConnector.loadTransactions();
				HomeActivity.this.apiConnector.loadUserInfo();
			} catch (IOException e) {
				Log.e(this.getClass().getName(), e.getMessage());
				return 1;
			} catch (SQLException e) {
				Log.e(this.getClass().getName(), e.getMessage());
				return 2;
			}
			
			// Done
	        return 0;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			checkNotNull(result);
			
			// Hide popup dialog
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			
			switch (result) {
			
			}
			
			// Update view
			HomeActivity.this.refreshList();
			HomeActivity.this.refreshMenu();
		}
	}
	
	private class SaveTransactionTask extends AsyncTask<Void, Void, Integer> {
	    private ProgressDialog dialog;
		
		public SaveTransactionTask() {
			this.dialog = new ProgressDialog(HomeActivity.this);
			this.dialog.setMessage("Transactie versturen");
	    }
		
		@Override
		protected void onPreExecute() {
	        this.dialog.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			checkNotNull(HomeActivity.this.transaction);
			
			try {
				return HomeActivity.this.apiConnector.saveTransaction(HomeActivity.this.transaction) ? 0 : 1;
			} catch (IOException e) {
				Log.e("HOME", e.getMessage());
				return 2;
			} catch (SQLException e) {
				Log.e("HOME", e.getMessage());
				return 2;
			}
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			checkNotNull(result);
			
			// Hide popup dialog
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			
			switch (result) {
				case 0: // OK
					HomeActivity.this.transaction = null;
					HomeActivity.this.refreshMenu();
					
					// Set each product change back to 0
					for (User user : HomeActivity.this.inhabitants) {
						for (ProductInfo productInfo : user.getProducts().values()) {
							productInfo.setChange(0);
						}
					}
					
					// Refresh visible rows
					HomeActivity.this.applyToVisibleRows(new Function<UserRowView, Boolean>() {
						@Override
						public Boolean apply(UserRowView view) {
							view.refreshProducts();
							view.refreshUser();
							
							return true;
						}
					});
					
					// Done
					Toast.makeText(HomeActivity.this, "Transactie succesvol verzonden!", Toast.LENGTH_LONG).show();
					
					break;
				case 1: // POST error
					new AlertDialog.Builder(HomeActivity.this)
						.setMessage("Opslaan van transactie is mislukt. Probeer het nogmaals.")
						.setTitle("Transactiefout")
						.create()
						.show();
					break;
				case 2: // Exception
					new AlertDialog.Builder(HomeActivity.this)
						.setMessage("Geen internetverbinding. Probeer het nogmaals.")
						.setTitle("Connectiefout")
						.create()
						.show();
					
					break;
					
			}
		}

	}

}
