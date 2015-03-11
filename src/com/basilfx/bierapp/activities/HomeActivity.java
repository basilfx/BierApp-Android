package com.basilfx.bierapp.activities;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import android.widget.TextView;
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
import com.basilfx.bierapp.R;
import com.basilfx.bierapp.actions.Action;
import com.basilfx.bierapp.callbacks.OnProductClickListener;
import com.basilfx.bierapp.callbacks.OnRefreshActionListener;
import com.basilfx.bierapp.callbacks.OnSaveActionListener;
import com.basilfx.bierapp.data.adapters.TransactionItemListAdapter;
import com.basilfx.bierapp.data.adapters.UserListAdapter;
import com.basilfx.bierapp.data.models.HostMapping;
import com.basilfx.bierapp.data.models.Hosting;
import com.basilfx.bierapp.data.models.Product;
import com.basilfx.bierapp.data.models.Balance;
import com.basilfx.bierapp.data.models.Transaction;
import com.basilfx.bierapp.data.models.TransactionItem;
import com.basilfx.bierapp.data.models.User;
import com.basilfx.bierapp.database.DatabaseHelper;
import com.basilfx.bierapp.database.HostMappingHelper;
import com.basilfx.bierapp.database.HostingHelper;
import com.basilfx.bierapp.database.ProductBalanceHelper;
import com.basilfx.bierapp.database.ProductHelper;
import com.basilfx.bierapp.database.TransactionHelper;
import com.basilfx.bierapp.database.TransactionItemHelper;
import com.basilfx.bierapp.database.UserHelper;
import com.basilfx.bierapp.service.SyncService;
import com.basilfx.bierapp.tasks.RefreshDataTask;
import com.basilfx.bierapp.tasks.SaveTransactionTask;
import com.basilfx.bierapp.utils.ProductInfo;
import com.basilfx.bierapp.views.ProductView;
import com.basilfx.bierapp.views.UserRowView;

/**
 *
 * 
 * @author Bas Stottelaar
 */
public class HomeActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnProductClickListener, OnMenuItemClickListener, OnRefreshActionListener, OnSaveActionListener {
	public static final String LOG_TAG = "HomeActivity";
	
	private static final String TRANSACTION_TAG = LOG_TAG;
	
	private TransactionHelper transactionHelper;
	private TransactionItemHelper transactionItemHelper;
	private ProductHelper productHelper;
	private UserHelper userHelper;
	private ProductBalanceHelper productBalanceHelper;
	private HostingHelper hostingHelper;
	private HostMappingHelper hostMappingHelper;
	
	private UserListAdapter userListAdapter; 
	private ListView userListView;
	private TextView status;
	private MenuItem purchaseMenu;
	private MenuItem undoMenu;
	
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
        
        // Initialize UI
        this.setContentView(R.layout.activity_home);
        
        // Bind controls
        this.userListView = (ListView) this.findViewById(R.id.list_users);
        this.status = (TextView) this.findViewById(R.id.text_status);
        
        this.status.setText("Total 0 transactions");

        // Create helpers for transactions
        this.transactionHelper = new TransactionHelper(this.getHelper());
        this.transactionItemHelper = new TransactionItemHelper(this.getHelper());
        this.productHelper = new ProductHelper(this.getHelper());
        this.userHelper = new UserHelper(this.getHelper());
        this.productBalanceHelper = new ProductBalanceHelper(this.getHelper());
        this.hostingHelper = new HostingHelper(this.getHelper());
        this.hostMappingHelper = new HostMappingHelper(this.getHelper());
        
        // Try to continue an ongoing transaction. Should only be one!
        TransactionHelper.Select builder = transactionHelper.select();
        builder.whereTagEq(TRANSACTION_TAG).whereRemoteIdEq(null).orderByCreated(true);
        
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
 		if (this.transaction != null && this.transaction.getCreated() != null) {
 			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 			
 			if (preferences.getBoolean("transaction_warn_resume", false)) {
 				int seconds = Integer.parseInt(preferences.getString("transaction_warn_resume_timeout", "3600"));
 				
 				Calendar timeout = Calendar.getInstance();
 				timeout.setTime(this.transaction.getModified());
 				timeout.add(Calendar.SECOND, seconds);
 				
 				if (Calendar.getInstance().after(timeout)) {
	 				String startDate = DateFormat
	 					.getDateInstance(DateFormat.LONG)
	 					.format(this.transaction.getCreated());
	 				
	 				String startTime = DateFormat
	 					.getTimeInstance(DateFormat.SHORT)
	 					.format(this.transaction.getCreated());
	 				
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
				switch (instance.getRole()) {
					case User.ADMIN:
					case User.MEMBER:
						return "Bewoners";
					case User.GUEST:
						return "Gasten";
					default:
						throw new IllegalStateException("Unknown role " + instance.getRole());
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
        
        switch (user.getRole()) {
	        case User.ADMIN:
			case User.MEMBER:
        		inflater.inflate(R.menu.menu_context_home_inhabitant, menu);
        		break;
        	case User.GUEST:
        		inflater.inflate(R.menu.menu_context_home_guest, menu);
        		break;
        	default:
        		throw new IllegalStateException("Unknown role " + user.getRole());
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
				checkArgument(user.getRole() == User.ADMIN || user.getRole() == User.MEMBER);

				// Create list of inactive guests
				final List<User> guests = this.userHelper.select() 
					.whereRoleEq(User.GUEST)
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
	    		    .setNegativeButton(R.string.annuleren, null)
	    		    .show();
    			
    			return true;
    		case R.id.menu_context_remove_guest:
				checkArgument(user.getRole() == User.GUEST);
				
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
				checkArgument(user.getRole() == User.GUEST);
				
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
				this.startActivity(new Intent(this, GuestsActivity.class)); 
				return true;
			case R.id.menu_transactions:
				// Switch to guests activity
				this.startActivity(new Intent(this, TransactionsActivity.class)); 
				return true;
			case R.id.menu_refresh:
				// Refresh data. If transaction exists, ask the user to cancel it.
				// Otherwise, we just make sure it doesn't exist.
				if (this.items > 0) {
					new AlertDialog.Builder(this)
						.setTitle(R.string.vernieuwen)
						.setMessage(R.string.er_is_een_huidige_transactie_gaande_die_gewist_zal_worden_wil_je_doorgaan)
						.setNegativeButton(android.R.string.no, null)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								HomeActivity.this.refreshData();
							}
						})
						.show();
				} else {
					this.refreshData();
				}
				
				return true;
			case R.id.menu_settings:
				// Switch to guests activity
				this.startActivity(new Intent(this, SettingsActivity.class)); 
				return true;
			case R.id.menu_purchase_confirm:
				checkNotNull(this.transaction);
				this.saveTransaction();
				
				return true;
			case R.id.menu_purchase_show:
				this.showTransactionSummary(checkNotNull(this.transaction), false);
				
				return true;
			case R.id.menu_purchase_cancel:
				checkNotNull(this.transaction);

				// Delete query
				this.cancelTransaction();
				
				for (User user : Iterables.concat(this.inhabitants, this.guests)) {
					this.cancelTransaction(user);
				}
				
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
				Toast.makeText(this, R.string.transactie_gewist, Toast.LENGTH_LONG).show();
				
				return true;
			case R.id.menu_undo:
				checkNotNull(this.transaction);

				// Undo last transaction item, possibly canceling the whole transaction.
				this.undoTransactionItem();
				
				// Update UI
				this.refreshMenu();
				this.refreshList();
				
				// Display toast message
				Toast.makeText(this, "Last transaction item removed.", Toast.LENGTH_LONG).show();
				
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
		
		// Save reference to purchase and undo menu to hide/update it
		this.purchaseMenu = menu.findItem(R.id.menu_purchase);
		this.undoMenu = menu.findItem(R.id.menu_undo);
		
		// Add handler to general menu
		menu.findItem(R.id.menu_show_guests).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_transactions).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_refresh).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_settings).setOnMenuItemClickListener(this);
		
		// Add handler to purchase menu
		menu.findItem(R.id.menu_purchase_confirm).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_purchase_show).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_purchase_cancel).setOnMenuItemClickListener(this);
		
		// Add handler to undo menu
		menu.findItem(R.id.menu_undo).setOnMenuItemClickListener(this);
        
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
	
	private void saveTransaction() {
		// For each guest transaction, set a payer
        List<TransactionItem> transactionItems = this.transactionItemHelper.select()
            .whereTransactionIdEq(this.transaction.getId())
            .all();
		
		for (TransactionItem transactionItem : transactionItems) {
            if (transactionItem.getPayer().getRole() == User.GUEST) {
                List<HostMapping> mappings = this.hostMappingHelper.select()
                    .whereHostingIdIn(this.hostingHelper.select()
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

                this.transactionItemHelper.update(transactionItem);
                this.hostMappingHelper.update(mapping);
            }
        }
		
		// Display fragment which will load new data
		SaveTransactionTask task = SaveTransactionTask.newInstance(this.transaction);
		task.show(this.getFragmentManager(), SaveTransactionTask.FRAGMENT_TAG);
	}
	
	private void refreshData() {
		// Make sure current transaction is stopped
		this.cancelTransaction();
		
		// Display fragment which will load new data
		RefreshDataTask task = RefreshDataTask.newInstance();
		task.show(this.getFragmentManager(), RefreshDataTask.FRAGMENT_TAG);
	}
	
	private void cancelTransaction(User user) {
		for (ProductInfo productInfo : user.getProducts().values()) {
			this.amount = this.amount - productInfo.getChange(); 
			productInfo.setChange(0);
		}
	}
	
	private void undoTransactionItem() {
		TransactionItem transactionItem = this.transactionItemHelper.select()
			.whereRemoteIdEq(null)
			.whereTransactionIdEq(this.transaction.getId())
			.orderById(false)
			.first();
		
		// There should be an ID of a transaction item
		checkNotNull(transactionItem, "Transaction item ID expected");
		
		// Delete it
		int count = transactionItem.getCount();
		int lines = this.transactionItemHelper.delete()
			.whereIdEq(transactionItem.getId())
			.execute();
		
		// Update the number of transaction items
		this.items = this.items - lines;
		this.amount = this.amount - count;
		
		// Check transaction state
		this.checkTransaction();
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
			this.undoMenu.setVisible(false);
		} else {
			this.purchaseMenu.setTitle(this.getResources().getString(R.string.d_producten, -1 * this.amount));
			this.purchaseMenu.setVisible(true);
			this.undoMenu.setVisible(true);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void refreshList() {
		Pair<Integer, Integer> key;

		this.inhabitants = this.userHelper.select()
			.whereRoleIn(Lists.newArrayList(User.ADMIN, User.MEMBER))
			.all();
		this.guests = this.userHelper.select()
			.whereRoleEq(User.GUEST)
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
		
		// For each user, get the product info
		Map<Pair<Integer, Integer>, Balance> productBalances = productBalanceHelper.select()
			.whereUserIdIn(userIds)
			.whereProductIdIn(productIds)
			.asUserProductMap();
		
		// For each user, get the 
		
		// Fetch count per user per project
		Map<Pair<Integer, Integer>, Integer> userProductCount = null; 
		
		if (this.transaction != null) {
			userProductCount = this.transactionItemHelper.select()
				.whereTransactionIdEq(this.transaction.getId())
				.whereUserIdIn(userIds)
				.whereProductIdIn(productIds)
				.asUserProductCountMap();
		}

		for (User user : Iterables.concat(this.inhabitants, this.guests)) {
    		Builder<Product, ProductInfo> builder = ImmutableMap.<Product, ProductInfo>builder();
    		
    		for (Product product : products) {
    			Balance productBalance;
    			
    			// If productBalance is null, no information was found (e.g. no transactions)
    			key = Pair.create(user.getId(), product.getId());

    			if (productBalances.containsKey(key)) {
    				productBalance = productBalances.get(key);
    			} else {
    				productBalance = new Balance();
    			}
    			
    			// Fetch the transaction item change
    			key = Pair.create(user.getId(), product.getId());
    			
    			if (userProductCount != null && userProductCount.containsKey(key)) {    				
    				builder.put(product, new ProductInfo(productBalance.getEstimatedCount(), userProductCount.get(key)));
    			} else {
    				builder.put(product, new ProductInfo(productBalance.getEstimatedCount(), 0));
    			}
    		}
    		
    		user.setProducts(builder.build());
    	}
		
		// Notify change
		((SimpleSectionAdapter<User>) this.userListView.getAdapter()).notifyDataSetChanged();
    }
	
	@Override
	public void onProductClickListener(UserRowView userView, ProductView productView, User user, boolean inDialog, Product product, int count) {
		// Don't do anything in case of zero
		if (count == 0) {
			return;
		}
		
		if (this.transaction == null) {
			this.transaction = new Transaction();
			this.amount = 0;
			this.items = 0;
			
			this.transaction.setCreated(new Date());
			this.transaction.setModified(new Date());
			this.transaction.setDescription(getString(R.string.verkoop_vanaf_s, android.os.Build.MODEL));
			this.transaction.setTag(TRANSACTION_TAG);
			
			transactionHelper.create(this.transaction);
		}
		
		// Add new transaction item
		TransactionItem transactionItem = new TransactionItem();
		
		transactionItem.setProduct(product);
		transactionItem.setCount(-1 * count);
		transactionItem.setUser(user);
		transactionItem.setPayer(user);
		transactionItem.setTransaction(this.transaction);
		
		this.transactionItemHelper.create(transactionItem);
		
		// Update transaction state
		ProductInfo productInfo = user.getProducts().get(product);
		productInfo.setChange(productInfo.getChange() - count);
		this.amount = this.amount - count;
		this.items = this.items + 1;
		
		// Check transaction state
		this.checkTransaction();
		
		// Dialog items are not in the list, so invoke a custom refresh
		if (inDialog) {
			userView.refreshProduct(productView, productInfo);
		}
		
		// Refresh User view
		if (userView != null) {
			userView.refreshProducts();
		}
		
		// Refresh menu
		this.refreshMenu();
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
	private void cancelTransaction() {
		// Stop an ongoing transaction
		if (this.transaction != null) {
			this.transactionHelper.delete(this.transaction);
		}
			
		// Reset transaction
		this.transaction = null;
		this.amount = 0;
		this.items = 0;
	}
	
	private void showTransactionSummary(Transaction transaction, final boolean byPayer) {
		ListView listView = new ListView(this);
		
		// Create inner adapter
		TransactionItemListAdapter adapter = new TransactionItemListAdapter(this);
		TransactionItemHelper.Select builder = this.transactionItemHelper.select()
			.whereTransactionIdEq(transaction.getId())
			.groupByUserAndProduct();
		
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
 		checkArgument(this.transaction != null || (this.amount == 0 && this.items == 0), "Amount and items not zero while no transaction");
 		
 		// Remove transaction if empty
 		if (this.transaction != null && this.items == 0) {
 			this.transactionHelper.delete(this.transaction);
 			
 			this.transaction = null;
 			this.amount = 0;
 			this.items = 0;
 		}
	}

	@Override
	public void onRefreshActionResult(int result) {
		switch (result) {
	        case Action.RESULT_OK:
	            // Update view
	            this.refreshList();
	            this.refreshMenu();
	            
	            // Inform user
	            Toast.makeText(HomeActivity.this, R.string.data_vernieuwd, Toast.LENGTH_LONG).show();
	            
	            break;
	        case 1:
	            break;
	        case 2:
	            break;
	        default:
	        	throw new IllegalStateException("Code: " + result);
	    }
	}

	@Override
	public void onSaveActionResult(int result, int transactionId) {
        switch (result) {
	        case Action.RESULT_OK:
	            // Reset state
	            this.transaction = null;
	            this.amount = 0;
	            this.items = 0;
	            
	            // Update UI
	            this.refreshMenu();
	            this.refreshList();
	            
	            // Display summary
	            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
	            
	            if (preferences.getBoolean("transaction_post_save_summary", true)) {
	            	Transaction transaction = this.transactionHelper.select()
	            		.whereIdEq(transactionId)
	            		.whereRemoteIdNeq(null)
	            		.first();
	            	
	                this.showTransactionSummary(transaction, true);
	            }
	            
	            // Done
	            Toast.makeText(this, R.string.transactie_succesvol_verzonden, Toast.LENGTH_LONG).show();
	            
	            break;
	        case Action.RESULT_ERROR_INTERNAL:
	        case Action.RESULT_ERROR_SERVER:
	            new AlertDialog.Builder(this)
	                .setMessage(R.string.opslaan_van_transactie_is_mislukt_probeer_het_nogmaals)
	                .setTitle(R.string.transactiefout)
	                .create()
	                .show();
	            
	            break;
	        case Action.RESULT_ERROR_CONNECTION:
	            new AlertDialog.Builder(this)
	                .setMessage(R.string.geen_internetverbinding_probeer_het_nogmaals)
	                .setTitle(R.string.connectiefout)
	                .create()
	                .show();
	            
	            break;
	        case Action.RESULT_ERROR_AUTHENTICATION:
	            new AlertDialog.Builder(this)
	                .setMessage(R.string.authenticatie_met_de_server_is_mislukt_de_applicatie_moet_opnieuw_gekoppeld_worden)
	                .setTitle(R.string.authenticatiefout)
	                .create()
	                .show();
	            
	            break;
	        default:
	            throw new IllegalStateException("Code: " + result);
	    }
	}
}
