package com.warmwit.bierapp.data.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.ProductClickedCallback;
import com.warmwit.bierapp.data.models.User;

public class UserListAdapter extends ArrayAdapter<User> {
	private BierAppApplication application;
	private ProductClickedCallback callback;
	
    public UserListAdapter(Activity context, ProductClickedCallback callback){  
        super(context, R.layout.listview_row_user);
        
        // Set properties
        this.application = (BierAppApplication) context.getApplication();
        this.callback = callback;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {
    	UserRowView row;
    	
    	// Inflate or reuse view
        if (view == null) {
            row = new UserRowView(this.getContext());
        } else {
        	row = (UserRowView) view;
        }
        
        // Make sure the correct data is available
        User user = this.getItem(pos);
        row.setRow(user, this.callback, this.application);
        
        // Then instruct view to set data
        row.initView();
        
        // Done
        return row;
    }
}
