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

public class UserListAdapter extends ArrayAdapter<User> {
	private List<UserRowState> userRowStates;
	private BierAppApplication application;
	private ProductClickedCallback callback;
	
    public UserListAdapter(Activity context, List<UserRowState> rowItems, ProductClickedCallback callback){  
        super(context, R.layout.listview_row_user);
        
        // Set properties
        this.userRowStates = rowItems;
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
        row.setRow(user, this.userRowStates.get(pos), this.callback, this.application);
        
        // Then instruct view to set data
        row.initView();
        
        // Done
        return row;
    }
}
