package com.basilfx.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.DateFormat;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.common.collect.Lists;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.basilfx.bierapp.R;
import com.basilfx.bierapp.data.adapters.TransactionItemListAdapter;
import com.basilfx.bierapp.data.adapters.TransactionListAdapter;
import com.basilfx.bierapp.data.models.Transaction;
import com.basilfx.bierapp.data.models.TransactionItem;
import com.basilfx.bierapp.database.DatabaseHelper;
import com.basilfx.bierapp.database.TransactionHelper;
import com.basilfx.bierapp.database.TransactionItemHelper;

public class TransactionsActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnMenuItemClickListener {
	private List<Transaction> transactions;
	private ListView transactionListView;
	private TransactionListAdapter transactionListAdapter;
	
	private TransactionHelper transactionHelper;
	private TransactionItemHelper transactionItemHelper;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show back arrow
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Set content
        this.setContentView(R.layout.activity_transactions);
        this.setTitle("Historie");
        
        // Construct model helpers
        this.transactionHelper = new TransactionHelper(this.getHelper());
        this.transactionItemHelper = new TransactionItemHelper(this.getHelper());
        
        // Bind controls
        this.transactionListView = (ListView) this.findViewById(R.id.list_transactions);
        this.transactions = Lists.newArrayList(); 
        
        // Bind data
        this.bindData(savedInstanceState);
	}
	
	
	
	private void bindData(Bundle savedInstanceState) {
		this.transactionListAdapter = new TransactionListAdapter(this) {
			@Override
			public int getCount() {
				return TransactionsActivity.this.transactions.size();
			}

			@Override
			public Object getItem(int position) {
				return TransactionsActivity.this.transactions.get(position);
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
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater(); 
	    inflater.inflate(R.menu.menu_context_transaction, menu);
        
        super.onCreateContextMenu(menu, v, menuInfo);
    }

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_add_transaction:
				Intent intent = new Intent(this, TransactionEditorActivity.class);
				
				intent.setAction(TransactionEditorActivity.ACTION_ADD_NORMAL_TRANSACTION);
				
				this.startActivity(intent);
				
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId()) {
			case R.id.menu_add_transaction:
				return true;
			case R.id.menu_context_undo_transaction:
				Intent intent = new Intent(this, TransactionEditorActivity.class);
				
				intent.setAction(TransactionEditorActivity.ACTION_UNDO_TRANSACTION);
				intent.putExtra("transactionId", ((Transaction) this.transactionListView.getItemAtPosition(info.position)).getId());
				
				this.startActivity(intent);
				
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case android.R.id.home:
	    		this.finish();

	        	return true;
	    	default: 
	    		return super.onOptionsItemSelected(item);  
	    }
	}

	@Override
	protected void onResume() {
		// (Re)load data
		this.transactions = this.transactionHelper.select()
			.whereRemoteIdNeq(null)
			.orderByDateCreated(false)
			.all();
		
		((SimpleSectionAdapter) this.transactionListView.getAdapter()).notifyDataSetChanged();
		
		// Done
		super.onResume();
	}
}
