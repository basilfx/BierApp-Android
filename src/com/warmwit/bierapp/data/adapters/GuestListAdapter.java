package com.warmwit.bierapp.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.models.User;

public class GuestListAdapter extends ArrayAdapter<User> {
	
	public GuestListAdapter(Activity context) {  
        super(context, R.layout.listview_row_guest);
	}
	
	 @Override
    public View getView(int pos, View view, ViewGroup parent) {
    	// Inflate or reuse view
        if (view == null) {
        	LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_row_guest, parent, false);
        }
        
        // Bind views
        TextView username = (TextView) view.findViewById(R.id.name);
        ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
        
        // Bind data
        User user = this.getItem(pos);
        username.setText(user.getName());
        
        // Done
        return view;
    }
}
