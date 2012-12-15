package com.warmwit.bierapp.data.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.ProductClickedCallback;
import com.warmwit.bierapp.data.model.Product;
import com.warmwit.bierapp.data.model.User;

public class UserAdapter extends ArrayAdapter<User> {
	private List<UserRowItem> rowItems;
	private BierAppApplication application;
	private ProductClickedCallback callback;
	
    public UserAdapter(Activity context, List<UserRowItem> rowItems, ProductClickedCallback callback){  
        super(context, R.layout.listview_row_user);
        
        // Set properties
        this.rowItems = rowItems;
        this.application = (BierAppApplication) context.getApplication();
        this.callback = callback;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent){
    	// Inflate or reuse view
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_row_user, null);
        }
        
        // Set properties
        TextView name = (TextView) view.findViewById(R.id.name);
        ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
        LinearLayout products = (LinearLayout) view.findViewById(R.id.products);
        
        // Set properties
        final User user = this.getItem(pos);
        UserRowItem rowItem = this.rowItems.get(pos);
        rowItem.setRow(user, name, avatar);

        // Add products
        int drawn = 0;
        
        for (final Product product : this.application.products) {
        	// Stop after three drawn icons
        	if (drawn > 3) break;
        	
        	// 
        	ImageView button = new ImageView(this.getContext());
        	button.setImageResource(R.drawable.product_beer_brand);
        	
        	// Notify the receiving end of the onProductClicked callback
        	button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					UserAdapter.this.callback.onProductClicked(user, product);
				}
			});
        	
        	// Prevent long clicks to be detected as single clicks
        	button.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					return false;
				}
			});
        	
        	// Add to products view
        	products.addView(button);
        	
        	// Increment counter
        	drawn = drawn + 1;
        }
        
        // Show 'more' button when there are products left
        if (drawn < this.application.products.size()) {
        	//
        }
        
        // Done
        return view;
    }
}
