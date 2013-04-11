package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLException;
import java.text.DateFormat;

import android.os.Bundle;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.data.RemoteClient;
import com.warmwit.bierapp.data.adapters.TransactionListAdapter;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.database.DatabaseHelper;
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
	}
}
