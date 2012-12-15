package com.warmwit.bierapp.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.ProductClickedCallback;
import com.warmwit.bierapp.data.adapter.UserAdapter;
import com.warmwit.bierapp.data.adapter.UserRowItem;
import com.warmwit.bierapp.data.model.Product;
import com.warmwit.bierapp.data.model.Transaction;
import com.warmwit.bierapp.data.model.TransactionItem;
import com.warmwit.bierapp.data.model.User;

public class HomeActivity extends Activity {
	private ListView userListView;
	private ArrayList<UserRowItem> userRowItems;
	private BierAppApplication application;
	
	private Transaction transaction;
	
	private MenuItem purchaseMenu;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content
        setContentView(R.layout.activity_home);
        
        // Bind controls
        this.application = (BierAppApplication) this.getApplication();
        this.userListView = (ListView) this.findViewById(R.id.userListView);
     	
        // Restore state data
 		if (savedInstanceState != null) {
 			this.userRowItems = savedInstanceState.getParcelableArrayList("userRowItems");
 		} else {
 			this.userRowItems = new ArrayList<UserRowItem>(application.users.size());
 			
 			for (User user : application.users) {
 				this.userRowItems.add(new UserRowItem(user));
 			}
 		}
        
 		// Bind data
 		this.bindData(savedInstanceState);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context_user, menu);
        
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_context_add_guest:
    			return true;
    		case R.id.menu_context_clear_transaction_items:
    			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    		    User user = (User) this.userListView.getAdapter().getItem(info.position);
    		    
    		    if (this.transaction != null) {
    		    	this.transaction.clear(user);
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
        
        OnMenuItemClickListener handler = new OnMenuItemClickListener() {
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
							
							for (User user : grouped.keySet()) {
								message.append(user.getFullName() + "\t\t" + grouped.get(user).size() + "x\n");
							}
							
							alertDialog.setMessage(message.toString());
						}
						
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
		
		// Add handler to submenu items
		menu.findItem(R.id.menu_purchase_confirm).setOnMenuItemClickListener(handler);
		menu.findItem(R.id.menu_purchase_show).setOnMenuItemClickListener(handler);
		menu.findItem(R.id.menu_purchase_cancel).setOnMenuItemClickListener(handler);
        
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
				HomeActivity.this.transaction.add(user, product);
				
				// Refresh view
				HomeActivity.this.refreshView();
			}
		};
		
    	UserAdapter userAdapter = new UserAdapter(this, this.userRowItems, callback);
    	userAdapter.addAll(this.application.users);
    	
    	// Create sectionizer to seperate guests from inhabitants
    	SimpleSectionAdapter<User> sectionAdapter = new SimpleSectionAdapter<User>(
			this, userAdapter, R.layout.listview_row_header, R.id.header, new Sectionizer<User>() {
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
		if (this.transaction == null) {
			this.purchaseMenu.setVisible(false);
		} else {
			this.purchaseMenu.setTitle(this.transaction.getTotalAmount() + " producten");
			this.purchaseMenu.setVisible(true);
		}
	}
}
