package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.OnProductClickListener;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.data.adapters.UserListAdapter;
import com.warmwit.bierapp.data.models.HostMapping;
import com.warmwit.bierapp.data.models.Hosting;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.data.models.UserInfo;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.HostQuery;
import com.warmwit.bierapp.database.ProductQuery;
import com.warmwit.bierapp.database.TransactionItemQuery;
import com.warmwit.bierapp.database.TransactionQuery;
import com.warmwit.bierapp.database.UserQuery;
import com.warmwit.bierapp.utils.LogUtils;
import com.warmwit.bierapp.utils.ProductInfo;
import com.warmwit.bierapp.utils.ProgressAsyncTask;
import com.warmwit.bierapp.views.ProductView;
import com.warmwit.bierapp.views.UserRowView;

/**
 *
 * 
 * @author Bas Stottelaar
 */
public class HomeActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnProductClickListener, OnMenuItemClickListener {
	public static final String LOG_TAG = "HomeActivity";
	
	private ApiConnector apiConnector;
	
	private UserListAdapter userListAdapter; 
	private ListView userListView;
	private MenuItem purchaseMenu;
	
	private List<User> inhabitants;
	private List<User> guests;
	private List<Product> products;
	
	/**
	 * @var Reference to an on-going transaction.
	 */
	private Transaction transaction;
	
	/**
	 * @var Cached total count of products in transaction.
	 */
	private int amount;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Connect to API
		this.apiConnector = new ApiConnector(BierAppApplication.remoteClient, this.getHelper());
        
        // Set content
        this.setContentView(R.layout.activity_home);
        
        // Bind controls
        this.userListView = (ListView) this.findViewById(R.id.list_users);
        
        // Restore state data
 		if (savedInstanceState != null) {
 			this.transaction = new TransactionQuery(this).resumeById(savedInstanceState.getInt("transactionId"));
 		} else {
 			// TODO: Ask user if it wants to continue a old transaction
 			this.transaction = new TransactionQuery(this).resumeLatest();
 		}
 		
 		if (this.transaction != null) {
    		TransactionQuery transactionQuery = new TransactionQuery(this);
    		this.amount = transactionQuery.costByTransaction(this.transaction); 
    	}
 		
 		// Bind data
 		this.initList();
    	this.refreshList();
    }
    
    private void initList() {
    	this.userListAdapter = new UserListAdapter(this, this);
    	
    	// Create sectionizer to separate guests from inhabitants
    	SimpleSectionAdapter<User> sectionAdapter = new SimpleSectionAdapter<User>(
			this, this.userListAdapter, R.layout.listview_row_header, R.id.header, new Sectionizer<User>() {
			@Override
			public String getSectionTitleForItem(User instance) {
				switch (instance.getType()) {
					case User.INHABITANT:
						return "Bewoners";
					case User.GUEST:
						return "Gasten";
					default:
						Log.e(LOG_TAG, "Requested user type " + instance.getType());
						throw new IllegalStateException();
				}
			}		
		});
    	
	    // Display data and register context menu
    	this.userListView.setAdapter(sectionAdapter);
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
        	default:
				Log.e(LOG_TAG, "Requested user type " + user.getType());
				throw new IllegalStateException();
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
				checkArgument(user.getType() == User.INHABITANT);
				
				// Create list of inactive guests
    			final ArrayAdapter<User> adapter = new ArrayAdapter<User>(
					this, 
					android.R.layout.simple_list_item_single_choice, 
					new UserQuery(this).inactiveGuests()
    			);
    			
    			// Create list of hosts
				final List<User> users = this.inhabitants;
				final String[] names = new String[users.size()];
				final boolean[] states = new boolean[users.size()];
				
				for (int i = 0; i < users.size(); i++) {
					names[i] = users.get(i).getName();
					states[i] = users.get(i) == user;
				}
    			
				// Create the first dialog
    			new AlertDialog.Builder(this)
    		    	.setTitle("Kies een gast")
    		    	.setAdapter(adapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final User guest = adapter.getItem(which);
							
							// Create the second dialog
							new AlertDialog.Builder(HomeActivity.this)
								.setTitle("Kies de host(s)")
								.setMultiChoiceItems(
									names, 
									states,
									new DialogInterface.OnMultiChoiceClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which, boolean isChecked) {
											states[which] = isChecked;
										}
									}
								)
								.setPositiveButton("Toevoegen", new DialogInterface.OnClickListener() {
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
								})
								.setNegativeButton("Annuleren", null)
								.show();
						}
	    		    })
	    		    .show();
    			
    			return true;
    		case R.id.menu_context_remove_guest:
				checkArgument(user.getType() == User.GUEST);
				
				// Create a dialog
    			new AlertDialog.Builder(this)
    		    	.setMessage("Weet je zeker dat je " + user.getName() + " wilt verwijderen?")
    		    	.setNegativeButton(android.R.string.no, null)
    		    	.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Cancel transactions for this user
							HomeActivity.this.cancelTransaction(user);
							
							new TransactionItemQuery(HomeActivity.this).deleteByTransactionAndUser(HomeActivity.this.transaction, user);
							new HostQuery(HomeActivity.this).delete(user);
							
							// Reload data
							HomeActivity.this.refreshList();
							HomeActivity.this.refreshMenu();
							
							// Done
							Toast.makeText(HomeActivity.this, user.getName() + " is verwijderd.", Toast.LENGTH_LONG).show();
						}
					})
	    			.show();
    		    
    			return true;
    		case R.id.menu_context_show_hosts:
				checkArgument(user.getType() == User.GUEST);
				checkNotNull(user.getHosting());
				
				// Build a list of hosts
				List<String> message = Lists.newArrayList();
				
				for (HostMapping host : user.getHosting().getHosts()) {
					message.add(host.getHost().getFullName());
				}
				
				// Create a dialog
				new AlertDialog.Builder(this)
    		    	.setMessage(Joiner.on("\n").join(message))
    		    	.setPositiveButton(android.R.string.ok, null)
    				.show();
    			
    			// Done
    			return true;
    		case R.id.menu_context_clear_transaction_items:
    			checkNotNull(this.transaction);
    			
				// Set each product info to 0
    			new TransactionItemQuery(HomeActivity.this).deleteByTransactionAndUser(HomeActivity.this.transaction, user);
				this.cancelTransaction(user);
				
				// Refresh view
				view.refreshProducts();
				this.refreshMenu();
				
				// Done
				Toast.makeText(HomeActivity.this, "Transactie-items voor " + user.getName() + " gewist.", Toast.LENGTH_LONG).show();
    		    
    			return true;
    		default:
    			return super.onContextItemSelected(item);
    	}
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_show_guests:
				// Switch to guests activity
				startActivity(new Intent(this, GuestsActivity.class)); 
				return true;
			case R.id.menu_show_transactions:
				// Switch to guests activity
				startActivity(new Intent(this, TransactionActivity.class)); 
				return true;
			case R.id.menu_show_stats:
				// Switch to guests activity
				startActivity(new Intent(this, StatsActivity.class)); 
				return true;
			case R.id.menu_refresh:
				// Refresh data
				if (this.transaction != null) {
					new AlertDialog.Builder(this)
						.setTitle("Vernieuwen")
						.setMessage("Er is een huidige transactie gaande die gewist zal worden. Wil je doorgaan?")
						.setNegativeButton(android.R.string.no, null)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								new LoadDataTask().execute();
							}
						})
						.show();
				} else {
					new LoadDataTask().execute();
				}
				
				return true;
			case R.id.menu_purchase_confirm:
				checkNotNull(this.transaction);
				new SaveTransactionTask().execute();
				
				return true;
			case R.id.menu_purchase_show:
				checkNotNull(this.transaction);
				
				TransactionItemQuery transactionItemQuery = new TransactionItemQuery(this);
				List<TransactionItem> transactionItems = transactionItemQuery.byTransaction(this.transaction);
				List<String> message = Lists.newArrayList();
				
				for (TransactionItem transactionItem : transactionItems) {
					message.add(transactionItem.getUser().getFullName() + "\t\t" + transactionItem.getCount() + "x " + transactionItem.getProduct());
				}
				
				// Set the message
				new AlertDialog.Builder(this)
					.setTitle("Huidige transactie")
					.setPositiveButton("Sluiten", null)
					.setMessage(Joiner.on("\n").join(message))
					.show();
				
				return true;
			case R.id.menu_purchase_cancel:
				checkNotNull(this.transaction);

				// Delete query
				TransactionQuery transactionQuery = new TransactionQuery(this);
				transactionQuery.delete(this.transaction);
				this.transaction = null;
				
				for (User user : Iterables.concat(this.inhabitants, this.guests)) {
					this.cancelTransaction(user);
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
				this.amount = 0;
				this.refreshMenu();
				
				// Display toast message
				Toast.makeText(HomeActivity.this, "Transactie gewist", Toast.LENGTH_LONG).show();
				
				return true;
			default:
				// Nothing to do
				return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
		
		// Save reference to purchase menu to hide/update it
		this.purchaseMenu = menu.findItem(R.id.menu_purchase);
		
		// Add handler to general menu
		menu.findItem(R.id.menu_show_guests).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_show_stats).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_show_transactions).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_refresh).setOnMenuItemClickListener(this);
		
		// Add handler to purchase menu
		menu.findItem(R.id.menu_purchase_confirm).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_purchase_show).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_purchase_cancel).setOnMenuItemClickListener(this);
        
		// Set initial text
		this.refreshMenu();
		
		// Done		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
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
    			
    			// If userInfo is null, no information was found (e.g. no transactions)
    			if (userInfo == null) {
    				userInfo = new UserInfo();
    			}
    			
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
	public void onProductClickListener(UserRowView userView, ProductView productView, User user, boolean inDialog, Product product, int count) {
		// Don't do anything in case of zero
		if (count == 0) {
			return;
		}
		
		TransactionQuery transactionQuery = new TransactionQuery(this);
		
		// Start a new transaction if needed
		if (this.transaction == null) {
			this.transaction = transactionQuery.create("Verkoop vanaf Tablet");
		}
		
		// Create (or update an existing) transaction item
		transactionQuery.addTransactionItem(this.transaction, product, user, user, -1 * count);
		
		// Update counts
		ProductInfo productInfo = user.getProducts().get(product);
		productInfo.setChange(productInfo.getChange() - count);
		this.amount = this.amount - count;
		
		// Dialog items are not in the list, so invoke a custom refresh
		if (inDialog) {
			userView.refreshProduct(productView, productInfo);
		}
		
		// Refresh UI
		userView.refreshProducts();
		this.refreshMenu();
	}

	
	private class LoadDataTask extends ProgressAsyncTask<Void, Void, Integer> {
		public LoadDataTask() {
	        super(HomeActivity.this);
	        this.setMessage("Gegevens herladen");
	    }
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
	        
	        // Stop an ongoing transaction
 			if (HomeActivity.this.transaction != null) {
 				new TransactionQuery(HomeActivity.this).delete(HomeActivity.this.transaction);
 				HomeActivity.this.transaction = null;
 				HomeActivity.this.amount = 0;
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
				return LogUtils.logException(LOG_TAG, e, 1);
			} catch (SQLException e) {
				return LogUtils.logException(LOG_TAG, e, 2);
			}
			
			// Done
	        return 0;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			checkNotNull(result);
			
			switch (result) {
				case 0:
					// Update view
					HomeActivity.this.refreshList();
					HomeActivity.this.refreshMenu();
					
					// Inform user
					Toast.makeText(HomeActivity.this, "Data vernieuwd", Toast.LENGTH_LONG).show();
					
					break;
				case 1:
					break;
				case 2:
					break;
				default:
					throw new IllegalStateException();
			}
			
			super.onPostExecute(result);
		}
	}
	
	private class SaveTransactionTask extends ProgressAsyncTask<Void, Void, Integer> {
		public SaveTransactionTask() {
			super(HomeActivity.this);
			this.setMessage("Transactie versturen");
	    }

		@Override
		protected Integer doInBackground(Void... params) {
			checkNotNull(HomeActivity.this.transaction);
			
			// For each guest transaction, set a payer
			TransactionItemQuery transactionItemQuery = new TransactionItemQuery(HomeActivity.this);
			List<TransactionItem> transactionItems = transactionItemQuery.byTransaction(HomeActivity.this.transaction);
			
			for (TransactionItem transactionItem : transactionItems) {
				if (transactionItem.getPayer().getType() == User.GUEST) {
					Hosting hosting = transactionItem.getPayer().getHosting();
					
					// Determine the least times paid
					int min = Integer.MAX_VALUE;
					
					try {
						hosting.getHosts().refreshCollection();
					} catch (Exception e) {
						
					}
					
					for (HostMapping host : hosting.getHosts()) {
						if (host.getTimesPaid() < min) {
							min = host.getTimesPaid();
						}
					}
					
					// Gather candidate payers
					List<HostMapping> payers = Lists.newArrayList();
					
					for (HostMapping host : hosting.getHosts()) {
						if (host.getTimesPaid() <= min) {
							payers.add(host);
						}
					}
					
					// Select random payer
					HostMapping payer = payers.get(new Random().nextInt(payers.size()));
					transactionItem.setPayer(payer.getHost());
					payer.setTimesPaid(payer.getTimesPaid() + 1);
					
					try {
						HomeActivity.this.getHelper().getTransactionItemDao().update(transactionItem);
						HomeActivity.this.getHelper().getHostMappingDao().update(payer);
					} catch (SQLException e) {
						
					}
				}
			}
			
			// Send transaction to the server
			try {
				return HomeActivity.this.apiConnector.saveTransaction(HomeActivity.this.transaction) ? 0 : 1;
			} catch (IOException e) {
				return LogUtils.logException(LOG_TAG, e, 1);
			} catch (SQLException e) {
				return LogUtils.logException(LOG_TAG, e, 2);
			}
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			checkNotNull(result);
			
			switch (result) {
				case 0: // OK
					HomeActivity.this.transaction = null;
					HomeActivity.this.amount = 0;
					HomeActivity.this.refreshMenu();
					HomeActivity.this.refreshList();
					
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
				default:
					throw new IllegalStateException();
			}
			
			super.onPostExecute(result);
		}
	}
}
