package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.DateFormat;
import java.util.List;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.adapters.TransactionItemListAdapter;
import com.warmwit.bierapp.data.adapters.TransactionListAdapter;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.TransactionItemQuery;
import com.warmwit.bierapp.database.TransactionQuery;
import com.warmwit.bierapp.views.UserRowView;

public class TransactionsActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnMenuItemClickListener {
	private ListView transactionListView;
	private TransactionListAdapter transactionListAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content
        this.setContentView(R.layout.activity_transactions);
        this.setTitle("Historie");
        
        // Bind controls
        this.transactionListView = (ListView) this.findViewById(R.id.list_transactions);
        
        // Bind data
        this.bindData(savedInstanceState);
	}
	
	private void bindData(Bundle savedInstanceState) {
		final List<Transaction> transactions = new TransactionQuery(this).bySynced(true);
		this.transactionListAdapter = new TransactionListAdapter(this) {
			@Override
			public int getCount() {
				return transactions.size();
			}

			@Override
			public Object getItem(int position) {
				return transactions.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}
		};
    	
    	// Create sectionizer to seperate transactions by date
    	SimpleSectionAdapter<Transaction> sectionAdapter = new SimpleSectionAdapter<Transaction>(
			this, this.transactionListAdapter, R.layout.listview_row_header, R.id.header, new Sectionizer<Transaction>() {
			@Override
			public String getSectionTitleForItem(Transaction instance) {
				checkNotNull(instance.getDateCreated());
				
				return DateFormat.getDateInstance(DateFormat.LONG)
								 .format(instance.getDateCreated());
			}		
		});
    	
	    // Set the adapter and display the data
    	this.transactionListView.setAdapter(sectionAdapter);
    	
    	// Add click handler
    	this.transactionListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Transaction transaction = (Transaction) parent.getItemAtPosition(position);
				TransactionsActivity.this.showTransactionSummary(transaction, true);
			}
    		
		});
    	
    	this.registerForContextMenu(this.transactionListView);
	}
	
	private void showTransactionSummary(Transaction transaction, final boolean byPayer) {
		TransactionItemQuery transactionItemQuery = new TransactionItemQuery(this);
		ListView listView = new ListView(this);
		
		// Create inner adapter
		TransactionItemListAdapter adapter = new TransactionItemListAdapter(this);
		adapter.addAll(transactionItemQuery.byTransaction(transaction, byPayer ? "payer_id" : "user_id"));
		
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
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater(); 
	    inflater.inflate(R.menu.menu_context_transaction, menu);
        
        super.onCreateContextMenu(menu, v, menuInfo);
    }

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId()) {
			case R.id.menu_add_transaction:
				return true;
			case R.id.menu_context_undo_transaction:
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_transactions, menu);
		
		// Add handler to general menu
		menu.findItem(R.id.menu_add_transaction).setOnMenuItemClickListener(this);
		
		// Done		
		return super.onCreateOptionsMenu(menu);
	}
}
