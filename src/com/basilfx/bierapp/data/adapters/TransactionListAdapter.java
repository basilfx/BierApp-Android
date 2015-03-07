package com.basilfx.bierapp.data.adapters;

import java.text.DateFormat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.basilfx.bierapp.R;
import com.basilfx.bierapp.data.models.Transaction;

public abstract class TransactionListAdapter extends BaseAdapter {
	
	private static class ViewHolder {
		private TextView products;
		private TextView time;
	}
	
	private Context context;
	
    public TransactionListAdapter(Context context) {
    	this.context = context;
    }
	
	@Override
    public View getView(int pos, View view, ViewGroup parent) {
		ViewHolder holder;
		
    	// Inflate or recycle view
        if (view == null) {
        	LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
        	view = inflater.inflate(R.layout.listview_row_transaction, parent, false);
        	holder = new ViewHolder();
            
            holder.products = (TextView) view.findViewById(R.id.textView1);
            holder.time = (TextView) view.findViewById(R.id.textView2);
            
            view.setTag(holder);
        } else {
        	holder = (ViewHolder) view.getTag();
        }
        
        // Bind data
        Transaction transaction = (Transaction) this.getItem(pos);
        holder.products.setText(transaction.getDescription());
        
        holder.time.setText(DateFormat
	    		.getTimeInstance(DateFormat.SHORT)
	    		.format(transaction.getDateCreated()) + " uur"
        );
        
        // Done
        return view;
    }
}
