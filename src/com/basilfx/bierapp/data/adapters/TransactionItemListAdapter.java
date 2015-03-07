package com.basilfx.bierapp.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.basilfx.bierapp.R;
import com.basilfx.bierapp.data.models.TransactionItem;

public class TransactionItemListAdapter extends ArrayAdapter<TransactionItem> {
	public TransactionItemListAdapter(Activity context) {  
        super(context, R.layout.listview_row_transactionitem);
	}
	
	 @Override
    public View getView(int pos, View view, ViewGroup parent) {
    	// Inflate or reuse view
        if (view == null) {
        	LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_row_transactionitem, parent, false);
        }
        
        // Bind views
        TextView product = (TextView) view.findViewById(R.id.product);
        TextView count = (TextView) view.findViewById(R.id.count);
        
        // Bind data
        TransactionItem transactionItem = this.getItem(pos);
        product.setText(transactionItem.getProduct().getTitle());
        count.setText("" + transactionItem.getCount());
        
        // Done
        return view;
    }
}
