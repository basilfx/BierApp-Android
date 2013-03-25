package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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

import com.google.common.collect.Multimap;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.ProductClickedCallback;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.data.adapters.UserListAdapter;
import com.warmwit.bierapp.data.adapters.UserRowView;
import com.warmwit.bierapp.data.models.Guest;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;

public class HomeActivity extends Activity {
	private ApiConnector apiConnector;
	
	private int[] userRandomAvatars;
	private UserListAdapter userListAdapter; 
	private ListView userListView;
	private MenuItem purchaseMenu;
	
	/**
	 * @var Reference to an on-going transaction
	 */
	private Transaction transaction;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content
        this.setContentView(R.layout.activity_home);
        
        // Bind controls
        this.apiConnector = ((BierAppApplication) this.getApplication()).getApiConnector();
        this.userListView = (ListView) this.findViewById(R.id.user_list);
        
        // Restore state data
 		if (savedInstanceState != null) {
 			this.userRandomAvatars = savedInstanceState.getIntArray("userRandomAvatars");
 		} else {
 			// In advance, declare size of array
 			int size = this.apiConnector.getUsers().size() + this.apiConnector.getGuests().size();
 			this.userRandomAvatars = new int[size];
 		}
        
 		// Bind data
 		this.bindData(savedInstanceState);
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
    	switch (item.getItemId()) {
    		case R.id.menu_context_add_guest:
    			ArrayAdapter<Guest> adapter = new ArrayAdapter<Guest>(this, android.R.layout.simple_list_item_single_choice, this.apiConnector.getGuests());
    			
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		    builder.setTitle("Kies een gast");
    		    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
    		    	
    		    });
    		    
    		    builder.show();
    			
    			return true;
    		case R.id.menu_context_clear_transaction_items:
    			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    		    User user = (User) this.userListView.getAdapter().getItem(info.position);
    		    
    		    if (this.transaction != null) {
    		    	this.transaction.clear(user);
    		    	
    		    	// Display toast message
					Toast.makeText(HomeActivity.this, "Transactie-items voor " + user.getName() + " gewist", Toast.LENGTH_LONG).show();
    		    	
					// Update view
					this.refreshGlobal();
					this.refreshUser(info.position);
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
					default:
						// Nothing to do
						return false;
				}
			}
		};
        
        // Create purchase menu handler
        OnMenuItemClickListener purchaseHandler = new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.menu_purchase_confirm:
						if (HomeActivity.this.transaction != null) {
							new SaveTransactionTask(HomeActivity.this).execute();
						}
						
						return true;
					case R.id.menu_purchase_show:
						AlertDialog alertDialog;
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
						alertDialog.show();
						return true;
					case R.id.menu_purchase_cancel:
						// Cancel the transaction if there is one
						if (HomeActivity.this.transaction != null) {
							HomeActivity.this.transaction = null;
							HomeActivity.this.refreshAll();
						}
						
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
		
		// Add handler to purchase menu
		menu.findItem(R.id.menu_purchase_confirm).setOnMenuItemClickListener(purchaseHandler);
		menu.findItem(R.id.menu_purchase_show).setOnMenuItemClickListener(purchaseHandler);
		menu.findItem(R.id.menu_purchase_cancel).setOnMenuItemClickListener(purchaseHandler);
        
		// Done
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Save all userRowItems to the bundle
		outState.putIntArray("userRandomAvatars", this.userRandomAvatars);
	}

	private void bindData(Bundle savedInstance) {
		ProductClickedCallback callback = new ProductClickedCallback() {
			@Override
			public void onProductClicked(UserRowView userRowView, Product product) {
				// Create new transaction if needed
				if (HomeActivity.this.transaction == null) {
					HomeActivity.this.transaction = new Transaction();
					HomeActivity.this.transaction.setDescription("Normale verkoop");
				}
				
				// Create a transaction
				TransactionItem transactionItem = new TransactionItem();
				User user = userRowView.getUser();
				
				transactionItem.setAmount(1);
				transactionItem.setPayer(user);
				transactionItem.setUser(user);
				transactionItem.setProduct(product);
				
				// Add to the list
				HomeActivity.this.transaction.add(transactionItem);
				
				// Refresh view
				HomeActivity.this.refreshGlobal();
			}
		};
		
    	this.userListAdapter = new UserListAdapter(this, callback);
    	this.userListAdapter.addAll(this.apiConnector.getUsers());
    	this.userListAdapter.addAll(this.apiConnector.getGuests());
    	
    	// Create sectionizer to seperate guests from inhabitants
    	SimpleSectionAdapter<User> sectionAdapter = new SimpleSectionAdapter<User>(
			this, userListAdapter, R.layout.listview_row_header, R.id.header, new Sectionizer<User>() {
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
	
	public void refreshAll() {
		this.refreshGlobal();
		this.refreshUsers();
	}
	
	public void refreshGlobal() {
		// Retrieve total amount of products
		int amount = this.transaction == null ? 0 : this.transaction.getTotalAmount();
		
		// Save or hide purchase menu based on amount.
		if (amount == 0) {
			this.purchaseMenu.setVisible(false);
		} else {
			this.purchaseMenu.setTitle(amount + " consumpties");
			this.purchaseMenu.setVisible(true);
		}
	}
	
	public void refreshUsers() {
		// Update rows currently visible -- note that getChildAt() corresponds to first visible item!
		int firstIndex = this.userListView.getFirstVisiblePosition();
		int lastIndex = this.userListView.getLastVisiblePosition();
		
		for (int i = 0; i <= lastIndex - firstIndex; i++) {
			this.refreshUser(i);
		}
	}
	
	public void refreshUser(int index) {
		View view = this.userListView.getChildAt(index);
		
		// Skip section headers
		if (view instanceof UserRowView) {
			//((UserRowView) view).refreshView();
		}
	}

	private class SaveTransactionTask extends AsyncTask<Void, Void, Integer> {
	    private ProgressDialog dialog;
		
		public SaveTransactionTask(Context context) {
	        this.dialog = new ProgressDialog(context);
	    }
		
		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Transactie versturen");
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
					HomeActivity.this.refreshAll();
					
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
