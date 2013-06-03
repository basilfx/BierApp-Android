package com.warmwit.bierapp.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.models.User;

public class GuestListAdapter extends ArrayAdapter<User> {
	
	private static class ViewHolder { 
		private TextView username;
        private ImageView avatar;
	}
	
	public GuestListAdapter(Activity context) {  
        super(context, R.layout.listview_row_guest);
	}
	
	 @Override
    public View getView(int pos, View view, ViewGroup parent) {
		ViewHolder holder;
		
    	// Inflate or reuse view
        if (view == null) {
        	LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_row_guest, parent, false);

            // Bind views
            holder = new ViewHolder();

            holder.username = (TextView) view.findViewById(R.id.name);
            holder.avatar = (ImageView) view.findViewById(R.id.avatar);
            
            // Save holder
            view.setTag(holder);
        } else {
        	holder = (ViewHolder) view.getTag();
        }
        
        // Retrieve the user corresponding to this row
        User user = this.getItem(pos);
        
        // Bind data 
        holder.username.setText(user.getName());
        
        String url = Strings.isNullOrEmpty(user.getAvatarUrl()) ? "drawable://" + R.drawable.avatar_none : user.getAvatarUrl();
 		ImageLoader.getInstance().displayImage(url, holder.avatar);
        
        // Done
        return view;
    }
}
