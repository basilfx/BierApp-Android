package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.auth.AuthenticationException;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
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
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.actions.Action;
import com.warmwit.bierapp.actions.SyncAction;
import com.warmwit.bierapp.callbacks.OnProductClickListener;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.data.adapters.TransactionItemListAdapter;
import com.warmwit.bierapp.data.adapters.UserListAdapter;
import com.warmwit.bierapp.data.models.HostMapping;
import com.warmwit.bierapp.data.models.Hosting;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.data.models.UserInfo;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.HostMappingHelper;
import com.warmwit.bierapp.database.HostingHelper;
import com.warmwit.bierapp.database.ProductHelper;
import com.warmwit.bierapp.database.TransactionHelper;
import com.warmwit.bierapp.database.TransactionItemHelper;
import com.warmwit.bierapp.database.UserHelper;
import com.warmwit.bierapp.database.UserInfoHelper;
import com.warmwit.bierapp.exceptions.UnexpectedData;
import com.warmwit.bierapp.exceptions.UnexpectedStatusCode;
import com.warmwit.bierapp.service.SyncService;
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
	
	private static final String TRANSACTION_TAG = LOG_TAG;
	
	private ApiConnector apiConnector;
	
	private TransactionHelper transactionHelper;
	private TransactionItemHelper transactionItemHelper;
	private ProductHelper productHelper;
	private UserHelper userHelper;
	private UserInfoHelper userInfoHelper;
	private HostingHelper hostingHelper;
	private HostMappingHelper hostMappingHelper;
	
	private UserListAdapter userListAdapter; 
	private ListView userListView;
	private MenuItem purchaseMenu;
	
	private List<User> inhabitants;
	private List<User> guests;
	private List<Product> products;
	
	private BroadcastReceiver broadcastReceiver;
	
	/**
	 * @var Reference to an on-going transaction.
	 */
	private Transaction transaction;
	
	/**
	 * @var Cached total count of products in current transaction. Only 
	 * 		applicable if this.transaction is not null.
	 */
	private int amount;
	
	/**
	 * @var Cached number of items in current transaction. Only applicable if
	 * 		this.transaction is not null.
	 */
	private int items;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Connect to API
		this.apiConnector = new ApiConnector(BierAppApplication.getRemoteClient(), this.getHelper());
		
        // Initialize UI
        this.setContentView(R.layout.activity_home);
        
        // Bind controls
        this.userListView = (ListView) this.findViewById(R.id.list_users);

        // Create helpers for transactions
        this.transactionHelper = new TransactionHelper(this.getHelper());
        this.transactionItemHelper = new TransactionItemHelper(this.getHelper());
        this.productHelper = new ProductHelper(this.getHelper());
        this.userHelper = new UserHelper(this.getHelper());
        this.userInfoHelper = new UserInfoHelper(this.getHelper());
        this.hostingHelper = new HostingHelper(this.getHelper());
        this.hostMappingHelper = new HostMappingHelper(this.getHelper());
        
        // Try to continue an ongoing transaction. Should only be one!
        TransactionHelper.Select builder = transactionHelper.select();
        builder.whereTagEq(TRANSACTION_TAG).whereRemoteIdEq(null).orderByDateCreated(true);
        
 		if (savedInstanceState != null) {
 			int transactionId = savedInstanceState.getInt("transactionId");
 			
 			if (transactionId > 0) {
 				builder.whereIdEq(transactionId);
 			}
 		}
 		
 		this.transaction = builder.first();
 		this.amount = 0;
 		this.items = 0;
 		
 		// If an active transaction was found, sum it's transaction items
 		if (this.transaction != null) {
    		this.amount = this.transactionItemHelper.select()
    			.sumCount()
    			.whereTransactionIdEq(this.transaction.getId())
    			.firstInt();
    		
    		this.items = this.transactionItemHelper.select()
    			.count()
    			.whereTransactionIdEq(this.transaction.getId())
    			.firstInt();
    	}
 		
 		// Verify transaction state
 		this.checkTransaction();
 		
 		// Bind data
 		this.initList();
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		// Create broadcast receiver
		this.broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				HomeActivity.this.onHandleIntent(intent);
			}
		};
		
		// Register for messages
		this.registerReceiver(this.broadcastReceiver, new IntentFilter(SyncService.SYNC_COMPLETE));
		
		// Warn user if he is resuming an existing transaction
 		if (this.transaction != null && this.transaction.getDateCreated() != null) {
 			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 			
 			if (preferences.getBoolean("transaction_warn_resume", false)) {
 				int seconds = Integer.parseInt(preferences.getString("transaction_warn_resume_timeout", "3600"));
 				
 				Calendar timeout = Calendar.getInstance();
 				timeout.setTime(this.transaction.getDateCreated());
 				timeout.add(Calendar.SECOND, seconds);
 				
 				if (Calendar.getInstance().after(timeout)) {
	 				String startDate = DateFormat
	 					.getDateInstance(DateFormat.LONG)
	 					.format(this.transaction.getDateCreated());
	 				
	 				String startTime = DateFormat
	 					.getTimeInstance(DateFormat.SHORT)
	 					.format(this.transaction.getDateCreated());
	 				
	 				new AlertDialog.Builder(this)
	 					.setTitle(R.string.transactie_resumeren)
	 					.setMessage(this.getResources().getString(R.string.let_op_er_is_op_dit_moment_een_transactie_gestart_die_aangemaakt_is_op_s_om_s_uur, startDate, startTime))
	 					.setPositiveButton(R.string.sluiten, null)
	 					.show();
 				}
 			}
 		}
		
		// Refresh list
 		this.refreshList();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		// Unregister
		this.unregisterReceiver(this.broadcastReceiver);
	}
	
	protected void onHandleIntent(Intent intent) {
		String action = Strings.nullToEmpty(intent.getAction());
		Log.d(LOG_TAG, "Received intent: " + action);
		
		if (action.equals(SyncService.SYNC_COMPLETE)) {
			if (intent.getLongExtra("result", Action.RESULT_ERROR_UNKNOWN) == Action.RESULT_OK) {
				HomeActivity.this.refreshMenu();
				HomeActivity.this.refreshList();
			}
		}
	}
    
    private void initList() {
    	this.userListAdapter = new UserListAdapter(this) {
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				int inhabitantsCount = HomeActivity.this.inhabitants.size();
				
				if (position >= inhabitantsCount) {
					position = position - inhabitantsCount;
					return HomeActivity.this.guests.get(position);
				} else {
					return HomeActivity.this.inhabitants.get(position);
				}
			}
			
			@Override
			public int getCount() {
				if (HomeActivity.this.inhabitants == null || HomeActivity.this.guests == null) {
					return 0;
				}
				
				return HomeActivity.this.inhabitants.size() + HomeActivity.this.guests.size();
			}
			
			@Override
			public OnProductClickListener getOnProductClickListener() {
				return HomeActivity.this;
			}
		};
    	
    	
    	// Create sectionizer to separate guests from inhabitants
    	this.userListView.setAdapter(new SimpleSectionAdapter<User>(
			this, this.userListAdapter, R.layout.listview_row_header, R.id.header, new Sectionizer<User>() {
			@Override
			public String getSectionTitleForItem(User instance) {
				switch (instance.getType()) {
					case User.INHABITANT:
						return "Bewoners";
					case User.GUEST:
						return "Gasten";
					default:
						throw new IllegalStateException("type " + instance.getType());
				}
			}		
		}));
    	
	    // Register context menu
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
        
        // Hide clear button if applicable
        menu.findItem(R.id.menu_context_clear_transaction_items).setVisible(this.transaction != null);
        
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
				final List<User> guests = this.userHelper.select() 
					.whereTypeEq(User.GUEST)
					.whereIdNotIn(this.hostingHelper.select().selectUserIds().whereActiveEq(true).getBuilder())
					.all();
				
    			final ArrayAdapter<User> adapter = new ArrayAdapter<User>(
					this, 
					android.R.layout.simple_list_item_single_choice, 
					guests
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
    		    	.setTitle(R.string.kies_een_gast)
    		    	.setAdapter(adapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final User guest = adapter.getItem(which);
							
							// Create the second dialog
							new AlertDialog.Builder(HomeActivity.this)
								.setTitle(R.string.kies_de_host_s_)
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
								.setPositiveButton(R.string.toevoegen, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										List<User> hosts = Lists.newArrayList();
										
										for (int i = 0; i < states.length; i++) {
											if (states[i]) {
												hosts.add(users.get(i));
											}
										}
		
										if (hosts.size() > 0) {
											// Create a hosting
											Hosting hosting = new Hosting();
											
											hosting.setUser(guest);
											hosting.setActive(true);
											hosting.setDateChanged(new Date());

											HomeActivity.this.hostingHelper.create(hosting);
											
											// Create mappings for each host
											for (User user : hosts) {
												HostMapping mapping = new HostMapping();
												
												mapping.setHost(user);
												mapping.setHosting(hosting);
												
												HomeActivity.this.hostMappingHelper.create(mapping);
											}
											
											// Reload data
											HomeActivity.this.refreshList();
											
											// Done
											Toast.makeText(HomeActivity.this, HomeActivity.this.getResources().getString(R.string.s_als_gast_toegevoegd, guest.getName()), Toast.LENGTH_LONG).show();
										} else {
											// Done
											Toast.makeText(HomeActivity.this, R.string.gast_niet_toegevoegd_omdat_er_geen_hosts_geselecteerd_zijn, Toast.LENGTH_LONG).show();
										}
									}
								})
								.setNegativeButton(R.string.annuleren, null)
								.show();
						}
	    		    })
	    		    .show();
    			
    			return true;
    		case R.id.menu_context_remove_guest:
				checkArgument(user.getType() == User.GUEST);
				
				// Create a dialog
    			new AlertDialog.Builder(this)
    		    	.setMessage(HomeActivity.this.getResources().getString(R.string.weet_je_zeker_dat_je_s_wilt_verwijderen, user.getName()))
    		    	.setNegativeButton(android.R.string.no, null)
    		    	.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (HomeActivity.this.transaction != null) {
								// Cancel transactions for this user
								HomeActivity.this.cancelTransaction(user);
								
								// Remove transaction items from database
								int lines = HomeActivity.this.transactionItemHelper.delete()
				    				.whereRemoteIdEq(null)
				    				.whereUserIdEq(user.getId())
				    				.whereTransactionIdEq(HomeActivity.this.transaction.getId())
				    				.execute();
								
								// Update the number of transaction items
								HomeActivity.this.items = HomeActivity.this.items - lines;
								
								// Check transaction state
								HomeActivity.this.checkTransaction();
							}
							
							// Mark hosting as inactive
							HomeActivity.this.hostingHelper.update()
								.whereUserIdEq(user.getId())
								.setActive(false)
								.execute();
								
							// Reload data
							HomeActivity.this.refreshList();
							HomeActivity.this.refreshMenu();
							
							// Done
							String message = HomeActivity.this.getResources().getString(R.string.s_is_verwijderd, user.getName());
							Toast.makeText(HomeActivity.this, message, Toast.LENGTH_LONG).show();
						}
					})
	    			.show();
    		    
    			return true;
    		case R.id.menu_context_show_hosts:
				checkArgument(user.getType() == User.GUEST);
				
				// Initialize data
				List<String> message = Lists.newArrayList();
				List<HostMapping> mappings = this.hostMappingHelper.select()
					.whereHostingIdIn(this.hostingHelper.select()
						.selectIds()
						.whereActiveEq(true)
						.whereUserIdEq(user.getId())
						.getBuilder())
					.all();
				
				// Build message
				for (HostMapping mapping : mappings) {
					message.add(mapping.getHost().getFullName() + " (" + mapping.getTimesPaid() + "x betaald)");
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
    			this.cancelTransaction(user);
    			
    			// Remove transaction items
    			int lines = this.transactionItemHelper.delete()
    				.whereRemoteIdEq(null)
    				.whereUserIdEq(user.getId())
    				.whereTransactionIdEq(this.transaction.getId())
    				.execute();
    			
    			this.items = this.items - lines;
    			
    			// Check transaction state
    			this.checkTransaction();
				
				// Refresh view
				view.refreshProducts();
				this.refreshMenu();
				
				// Done
				Toast.makeText(HomeActivity.this, this.getResources().getString(R.string.transactie_items_voor_s_gewist, user.getName()), Toast.LENGTH_LONG).show();
    		    
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
			case R.id.menu_transactions:
				// Switch to guests activity
				startActivity(new Intent(this, TransactionsActivity.class)); 
				return true;
			case R.id.menu_refresh:
				// Refresh data
				if (this.transaction != null) {
					new AlertDialog.Builder(this)
						.setTitle(R.string.vernieuwen)
						.setMessage(R.string.er_is_een_huidige_transactie_gaande_die_gewist_zal_worden_wil_je_doorgaan)
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
			case R.id.menu_settings:
				// Switch to guests activity
				startActivity(new Intent(this, SettingsActivity.class)); 
				return true;
			case R.id.menu_purchase_confirm:
				checkNotNull(this.transaction);
				new SaveTransactionTask().execute();
				
				return true;
			case R.id.menu_purchase_show:
				this.showTransactionSummary(checkNotNull(this.transaction), false);
				
				return true;
			case R.id.menu_purchase_cancel:
				checkNotNull(this.transaction);

				// Delete query
				this.transactionHelper.delete(this.transaction);
				
				for (User user : Iterables.concat(this.inhabitants, this.guests)) {
					this.cancelTransaction(user);
				}
				
				// Refresh global
				this.amount = 0;
				this.items = 0;
				this.transaction = null;
				
				// Update UI
				this.refreshMenu();
				this.applyToVisibleRows(new Function<UserRowView, Boolean>() {
					@Override
					public Boolean apply(UserRowView view) {
						view.refreshProducts();
						return true;
					}
				});
				
				// Display toast message
				Toast.makeText(HomeActivity.this, R.string.transactie_gewist, Toast.LENGTH_LONG).show();
				
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
		menu.findItem(R.id.menu_transactions).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_refresh).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_settings).setOnMenuItemClickListener(this);
		
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
		if (this.items == 0) {
			this.purchaseMenu.setVisible(false);
		} else {
			this.purchaseMenu.setTitle(this.getResources().getString(R.string.d_consumpties, -1 * this.amount));
			this.purchaseMenu.setVisible(true);
		}
	}
	
	private void refreshList() {
		this.inhabitants = this.userHelper.select()
			.whereTypeEq(User.INHABITANT)
			.all();
		this.guests = this.userHelper.select()
			.whereTypeEq(User.GUEST)
			.whereIdIn(this.hostingHelper.select().selectUserIds().whereActiveEq(true).getBuilder())
			.all();
		this.products = this.productHelper.select()
			.all();
		
		// Generate lists of IDs
		List<Integer> userIds = Lists.newArrayList();
		List<Integer> productIds = Lists.newArrayList();
		
		for (User user : Iterables.concat(this.inhabitants, this.guests)) {
			userIds.add(user.getId());
		}
		
		for (Product product : products) {
			productIds.add(product.getId());
		}
		
		// For each user, set the product info
		Map<Pair<Integer, Integer>, UserInfo> userInfos = userInfoHelper.select()
			.whereUserIdIn(userIds)
			.whereProductIdIn(productIds)
			.asUserProductMap();
		
		for (User user : Iterables.concat(this.inhabitants, this.guests)) {
    		Builder<Product, ProductInfo> builder = ImmutableMap.<Product, ProductInfo>builder();
    		
    		for (Product product : products) {
    			Pair<Integer, Integer> key = Pair.create(user.getId(), product.getId());
    			UserInfo userInfo;
    			
    			// If userInfo is null, no information was found (e.g. no transactions)
    			if (userInfos.containsKey(key)) {
    				userInfo = userInfos.get(key);
    			} else {
    				userInfo = new UserInfo();
    			}
    			
    			if (this.transaction != null) {
    				int change = this.transactionItemHelper.select()
		    			.sumCount()
		    			.whereTransactionIdEq(this.transaction.getId())
		    			.whereUserIdEq(user.getId())
		    			.whereProductIdEq(product.getId())
		    			.firstInt();
    				
    				builder.put(product, new ProductInfo(userInfo.getEstimatedCount(), change));
    			} else {
    				builder.put(product, new ProductInfo(userInfo.getEstimatedCount(), 0));
    			}
    		}
    		
    		user.setProducts(builder.build());
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
		
		// Start a new transaction if needed
		boolean created = false;
		
		if (this.transaction == null) {
			this.transaction = new Transaction();
			this.amount = 0;
			this.items = 0;
			
			this.transaction.setDateCreated(new Date());
			this.transaction.setDescription(getString(R.string.verkoop_vanaf_s, android.os.Build.MODEL));
			this.transaction.setTag(TRANSACTION_TAG);
			
			transactionHelper.create(this.transaction);
			created = true;
		}
		
		// Try to update a similar transaction item
		int id = 0;
		int lines = created ? 0 : this.transactionItemHelper.update()
			.wherePayerIdEq(user.getId())
			.whereRemoteIdEq(null)
			.whereProductIdEq(product.getId())
			.whereTransactionIdEq(this.transaction.getId())
			.addCount(-1 * count)
			.execute();
		
		// Or create it if needed.
		if (lines == 0) {
			TransactionItem transactionItem = new TransactionItem();
			
			transactionItem.setProduct(product);
			transactionItem.setCount(-1 * count);
			transactionItem.setUser(user);
			transactionItem.setPayer(user);
			transactionItem.setTransaction(this.transaction);
			
			id = this.transactionItemHelper.create(transactionItem);
		}
		
		// Update transaction state
		ProductInfo productInfo = user.getProducts().get(product);
		productInfo.setChange(productInfo.getChange() - count);
		this.amount = this.amount - count;
		this.items = this.items + (id > 0 ? 1 : 0);
		
		// Dialog items are not in the list, so invoke a custom refresh
		if (inDialog) {
			userView.refreshProduct(productView, productInfo);
		}
		
		// Check transaction state
		this.checkTransaction();
		
		// Refresh UI
		userView.refreshProducts();
		this.refreshMenu();
	}
	
	private void showTransactionSummary(Transaction transaction, final boolean byPayer) {
		ListView listView = new ListView(this);
		
		// Create inner adapter
		TransactionItemListAdapter adapter = new TransactionItemListAdapter(this);
		TransactionItemHelper.Select builder = this.transactionItemHelper.select()
			.whereTransactionIdEq(transaction.getId());
		
		if (byPayer) {
			builder.orderByPayer(true);
		} else {
			builder.orderByUser(true);
		}
		
		adapter.addAll(builder.all());
		
		// Create outer adapter
		listView.setAdapter(new SimpleSectionAdapter<TransactionItem>(
			this, adapter, R.layout.listview_row_header, R.id.header, new Sectionizer<TransactionItem>() {
				@Override
				public String getSectionTitleForItem(TransactionItem instance) {
					return byPayer ? instance.getPayer().getName() : instance.getUser().getName();
				}		
		}));
		
		// Display dialog
		new AlertDialog.Builder(this)
			.setTitle(R.string.transactiesamenvatting)
			.setPositiveButton(R.string.sluiten, null)
			.setView(listView)
			.show();
	}
	
	private void checkTransaction() {
		// Check state
 		checkArgument(this.items >= 0, "Number of items cannot be negative");
 		checkArgument(this.amount != 0 || this.items == 0, "Amount not zero while items count is zero");
 		checkArgument(this.transaction != null || (this.amount == 0 && this.items == 0), "Amount and items not zero while no transaction");
 		
 		// Remove transaction if empty
 		if (this.transaction != null && this.items == 0) {
 			this.transactionHelper.delete(this.transaction);
 			
 			this.transaction = null;
 			this.amount = 0;
 			this.items = 0;
 		}
	}
	
	private class LoadDataTask extends ProgressAsyncTask<Void, Void, Integer> {
		public LoadDataTask() {
	        super(HomeActivity.this);
	        this.setMessage(HomeActivity.this.getResources().getString(R.string.gegevens_herladen));
	    }
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
	        
	        // Stop an ongoing transaction
 			if (HomeActivity.this.transaction != null) {
 				new TransactionHelper(getHelper()).delete(transaction);
 				
 				// Reset transaction
 				HomeActivity.this.transaction = null;
 				HomeActivity.this.amount = 0;
 				HomeActivity.this.items = 0;
 			}
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			return new SyncAction(HomeActivity.this.apiConnector).basicSync();
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
					Toast.makeText(HomeActivity.this, R.string.data_vernieuwd, Toast.LENGTH_LONG).show();
					
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
		private Transaction result;
		
		public SaveTransactionTask() {
			super(HomeActivity.this);
			this.setMessage(HomeActivity.this.getResources().getString(R.string.transactie_versturen));
	    }

		@Override
		protected Integer doInBackground(Void... params) {
			checkNotNull(HomeActivity.this.transaction);
			
			// For each guest transaction, set a payer
			List<TransactionItem> transactionItems = HomeActivity.this.transactionItemHelper.select()
				.whereTransactionIdEq(HomeActivity.this.transaction.getId())
				.all();
			
			for (TransactionItem transactionItem : transactionItems) {
				if (transactionItem.getPayer().getType() == User.GUEST) {
					List<HostMapping> mappings = HomeActivity.this.hostMappingHelper.select()
						.whereHostingIdIn(HomeActivity.this.hostingHelper.select()
							.selectIds()
							.whereActiveEq(true)
							.whereUserIdEq(transactionItem.getPayer().getId())
							.getBuilder())
						.all();
					
					// Determine the least times paid
					int min = Integer.MAX_VALUE;
					
					for (HostMapping host : mappings) {
						if (host.getTimesPaid() < min) {
							min = host.getTimesPaid();
						}
					}
					
					// Gather candidate payers
					List<HostMapping> payers = Lists.newArrayList();
					
					for (HostMapping host : mappings) {
						if (host.getTimesPaid() <= min) {
							payers.add(host);
						}
					}
					
					// Select random payer
					HostMapping mapping = payers.get(new Random().nextInt(payers.size()));
					transactionItem.setPayer(mapping.getHost());
					mapping.setTimesPaid(mapping.getTimesPaid() + 1);

					HomeActivity.this.transactionItemHelper.update(transactionItem);
					HomeActivity.this.hostMappingHelper.update(mapping);
				}
			}
			
			// Send transaction to the server
			try {
				this.result = HomeActivity.this.apiConnector.saveTransaction(HomeActivity.this.transaction);
				
				if (this.result != null) {
					// Reload new user data
					HomeActivity.this.apiConnector.loadUserInfo();
					
					return Action.RESULT_OK;
				} else {
					return Action.RESULT_ERROR_INTERNAL;
				}
			} catch (IOException e) {
				return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_CONNECTION);
			} catch (SQLException e) {
				return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SQL);
			} catch (AuthenticationException e) {
				return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_AUTHENTICATION);
			} catch (UnexpectedStatusCode e) {
				return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
			} catch (UnexpectedData e) {
				return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
			}
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			checkNotNull(result);
			
			switch (result) {
				case Action.RESULT_OK: // OK
					// Reset transaction
					HomeActivity.this.transaction = null;
					HomeActivity.this.amount = 0;
					HomeActivity.this.items = 0;
					
					HomeActivity.this.refreshMenu();
					HomeActivity.this.refreshList();
					
					// Display summary
					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
					
					if (preferences.getBoolean("transaction_post_save_summary", true)) {
						HomeActivity.this.showTransactionSummary(this.result, true);
					}
					
					// Done
					Toast.makeText(HomeActivity.this, R.string.transactie_succesvol_verzonden, Toast.LENGTH_LONG).show();
					
					break;
				case Action.RESULT_ERROR_INTERNAL:
				case Action.RESULT_ERROR_SERVER:
					new AlertDialog.Builder(HomeActivity.this)
						.setMessage(R.string.opslaan_van_transactie_is_mislukt_probeer_het_nogmaals)
						.setTitle(R.string.transactiefout)
						.create()
						.show();
					
					break;
				case Action.RESULT_ERROR_CONNECTION:
					new AlertDialog.Builder(HomeActivity.this)
						.setMessage(R.string.geen_internetverbinding_probeer_het_nogmaals)
						.setTitle(R.string.connectiefout)
						.create()
						.show();
					
					break;
				case Action.RESULT_ERROR_AUTHENTICATION:
					new AlertDialog.Builder(HomeActivity.this)
						.setMessage(R.string.authenticatie_met_de_server_is_mislukt_de_applicatie_moet_opnieuw_gekoppeld_worden)
						.setTitle(R.string.authenticatiefout)
						.create()
						.show();
					
					break;
				default:
					throw new IllegalStateException("Code: " + result);
			}
			
			super.onPostExecute(result);
		}
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
}
