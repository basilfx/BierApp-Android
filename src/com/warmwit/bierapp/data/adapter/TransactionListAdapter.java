package com.warmwit.bierapp.data.adapter;

import android.app.Activity;
import android.widget.ArrayAdapter;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.model.Transaction;

public class TransactionListAdapter extends ArrayAdapter<Transaction> {
	
	public TransactionListAdapter(Activity context) {  
        super(context, R.layout.listview_row_user);
	}
}
