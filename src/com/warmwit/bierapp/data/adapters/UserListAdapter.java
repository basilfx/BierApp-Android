package com.warmwit.bierapp.data.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.OnProductClickListener;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.views.UserRowView;

public class UserListAdapter extends ArrayAdapter<User> {
	private OnProductClickListener callback;
	
    public UserListAdapter(Activity context, OnProductClickListener callback){  
        super(context, R.layout.listview_row_user);
        
        this.callback = callback;
    }
    
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
    	UserRowView view;
    	
    	// Inflate or reuse view
        if (convertView == null) {
        	view = new UserRowView(this.getContext());
        } else {
        	view = (UserRowView) convertView;
        }
        
        // Bind data
        view.setUser(this.getItem(pos));
        view.setCallback(this.callback);
        
        // Display data
        view.refreshAll();
        
        // Done
        return view;
    }
}
