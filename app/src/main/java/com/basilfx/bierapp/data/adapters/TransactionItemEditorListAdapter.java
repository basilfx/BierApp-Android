package com.basilfx.bierapp.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.basilfx.bierapp.R;
import com.basilfx.bierapp.data.models.TransactionItem;

public abstract class TransactionItemEditorListAdapter extends BaseAdapter {

	private static class ViewHolder {
		private TextView users;
		private TextView count;
		private TextView product;
	}
	
	private Context context;
	
    public TransactionItemEditorListAdapter(Context context) {
    	this.context = context;
    }

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;
		
    	// Inflate or recycle view
        if (view == null) {
        	LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
        	view = inflater.inflate(R.layout.listview_row_transactionitem_editor, parent, false);
        	holder = new ViewHolder();
            
            holder.users = (TextView) view.findViewById(R.id.users);
            holder.count = (TextView) view.findViewById(R.id.count);
            holder.product = (TextView) view.findViewById(R.id.product);
            
            view.setTag(holder);
        } else {
        	holder = (ViewHolder) view.getTag();
        }
        
        // Set data
        TransactionItem transactionItem = (TransactionItem) this.getItem(position);
        
        holder.count.setText(transactionItem.getCount() + "");
        holder.product.setText(transactionItem.getProduct().getTitle());
        
        if (transactionItem.getUser().equals(transactionItem.getPayer())) {
        	holder.users.setText(transactionItem.getPayer().getName());
        } else {
        	holder.users.setText(transactionItem.getUser().getName() + " (" + transactionItem.getPayer().getName() + " betaalt)");
        }
        
        return view;
	}

}
