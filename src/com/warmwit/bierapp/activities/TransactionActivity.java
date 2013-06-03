package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.DateFormat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.TransactionItemQuery;
import com.warmwit.bierapp.database.TransactionQuery;

public class TransactionActivity extends OrmLiteBaseActivity<DatabaseHelper> {
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
		this.transactionListAdapter = new TransactionListAdapter(this);
		this.transactionListAdapter.addAll(new TransactionQuery(this).bySynced(true));
    	
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
				TransactionActivity.this.showTransactionSummary(transaction, true);
			}
    		
		});
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
}
