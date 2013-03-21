package com.warmwit.bierapp.data.adapters;

import java.text.DateFormat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.models.Transaction;

public class TransactionListAdapter extends ArrayAdapter<Transaction> {
	
	public TransactionListAdapter(Activity context) {  
        super(context, R.layout.listview_row_user);
	}
	
	 @Override
    public View getView(int pos, View view, ViewGroup parent) {
    	// Inflate or reuse view
        if (view == null) {
        	LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_row_transaction, parent, false);
        }
        
        // Bind views
        TextView products = (TextView) view.findViewById(R.id.textView1);
        TextView time = (TextView) view.findViewById(R.id.textView2);
        
        // Bind data
        Transaction transaction = this.getItem(pos);
        products.setText(transaction.getDescription());
        
        time.setText(DateFormat.getTimeInstance(DateFormat.SHORT)
        					   .format(transaction.getDateCreated()));
        
        // Done
        return view;
    }
}
