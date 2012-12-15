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
        ImageView moreView = (ImageView) view.findViewById(R.id.product_placeholder_more);
        
        // Get placeholders
        ImageView[] productView = { 
    		(ImageView) view.findViewById(R.id.product_placeholder_1),
    		(ImageView) view.findViewById(R.id.product_placeholder_2),
    		(ImageView) view.findViewById(R.id.product_placeholder_3),
        };
        
        // Set properties
        final User user = this.getItem(pos);
        UserRowItem rowItem = this.rowItems.get(pos);
        rowItem.setRow(user, name, avatar);

        // Add products to the placeholders
        for (int i = 0; i < productView.length; i++) {
        	if (i < this.application.products.size()) {
        		// Make sure the view is visible
        		productView[i].setVisibility(View.VISIBLE);
        		
	        	// Retrieve corresponding product
	        	final Product product = this.application.products.get(i);
	        	
	        	// Set image
	        	productView[i].setImageResource(product.getBuiltinLogo());
	        	
	        	// Notify the receiving end of the onProductClicked callback
	        	productView[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						UserAdapter.this.callback.onProductClicked(user, product);
					}
				});
	        	
	        	// Prevent long clicks to be detected as single clicks
	        	productView[i].setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						return false;
					}
				});
        	} else {
        		// Hide view
        		productView[i].setVisibility(View.INVISIBLE);
        	}
        }
        
        // Show the more button if applicable
        if (this.application.products.size() > productView.length) {
        	moreView.setVisibility(View.VISIBLE);
        } else {
        	moreView.setVisibility(View.INVISIBLE);
        }
        
        // Done
        return view;
    }
}
