package com.warmwit.bierapp.data.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.warmwit.bierapp.callbacks.OnProductClickListener;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.views.UserRowView;

public abstract class UserListAdapter extends BaseAdapter {
	
	private Context context;
	
    public UserListAdapter(Context context) {
    	this.context = context;
    }
    
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
    	UserRowView view;
    	
    	// Inflate or reuse view
        if (convertView == null) {
        	view = new UserRowView(this.context);
        } else {
        	view = (UserRowView) convertView;
        }
        
        // Bind data
    	view.setUser((User) this.getItem(pos));
    	view.setCallback(this.getOnProductClickListener());
        
    	// Display data
    	view.refreshAll();
        
        // Done
        return view;
    }

	public abstract OnProductClickListener getOnProductClickListener();
}
