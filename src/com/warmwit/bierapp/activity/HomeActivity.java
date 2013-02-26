package com.warmwit.bierapp.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.warmwit.bierapp.data.adapter.UserListAdapter;
import com.warmwit.bierapp.data.adapter.UserRowState;
import com.warmwit.bierapp.data.adapter.UserRowView;
import com.warmwit.bierapp.data.model.Guest;
import com.warmwit.bierapp.data.model.Product;
import com.warmwit.bierapp.data.model.Transaction;
import com.warmwit.bierapp.data.model.TransactionItem;
import com.warmwit.bierapp.data.model.User;

public class HomeActivity extends Activity {
	private ApiConnector apiConnector;
	private Transaction transaction;
	
	private UserListAdapter userListAdapter; 
	private ListView userListView;
	private ArrayList<UserRowState> userRowItems;
	private MenuItem purchaseMenu;
	
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
 			this.userRowItems = savedInstanceState.getParcelableArrayList("userRowItems");
 		} else {
 			// In advance, declare size of array
 			int size = this.apiConnector.getUsers().size() + this.apiConnector.getGuests().size();
 			this.userRowItems = new ArrayList<UserRowState>(size);
 			
 			// Users
 			for (User user : this.apiConnector.getUsers()) {
 				this.userRowItems.add(new UserRowState(user));
 			}
 			
 			// Guests
 			for (Guest guest : this.apiConnector.getGuests()) {
 				this.userRowItems.add(new UserRowState(guest));
 			}
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
    		    	this.refreshView();
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
							HomeActivity.this.refreshView();
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
		outState.putParcelableArrayList("userRowItems", this.userRowItems);
	}

	private void bindData(Bundle savedInstance) {
		ProductClickedCallback callback = new ProductClickedCallback() {
			@Override
			public void onProductClicked(User user, Product product) {
				// Create new transaction if needed
				if (HomeActivity.this.transaction == null) {
					HomeActivity.this.transaction = new Transaction();
				}
				
				// Add item
				//HomeActivity.this.transaction.add(user, product);
				
				// Refresh view
				HomeActivity.this.refreshView();
			}
		};
		
    	this.userListAdapter = new UserListAdapter(this, this.userRowItems, callback);
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
	
	public void refreshView() {
		// Retrieve total amount of products
		int amount = this.transaction == null ? 0 : this.transaction.getTotalAmount();
		
		// Save or hide purchase menu based on amount.
		if (amount == 0) {
			this.purchaseMenu.setVisible(false);
		} else {
			this.purchaseMenu.setTitle(amount + " consumpties");
			this.purchaseMenu.setVisible(true);
		}
		
		// Update changes items
		for (int i = 0; i < this.userRowItems.size(); i++) {
			// Get references
			User user = this.userListAdapter.getItem(i);
			UserRowState row = this.userRowItems.get(i);
			
			// Update row
			row.setChange(amount != 0 ? this.transaction.getAmount(user) : 0);
		}
		
		// Update rows currently visible -- note that getChildAt() corresponds to first visible item!
		int firstIndex = this.userListView.getFirstVisiblePosition();
		int lastIndex = this.userListView.getLastVisiblePosition();
		
		for (int i = 0; i <= lastIndex - firstIndex; i++) {
			View view = this.userListView.getChildAt(i);
			
			// Skip section headers
			if (view instanceof UserRowView) {
				((UserRowView) view).refreshView();
			}
		}
	}
}
