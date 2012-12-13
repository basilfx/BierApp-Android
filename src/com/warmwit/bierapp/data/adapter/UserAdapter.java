package com.warmwit.bierapp.data.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.model.User;
import com.warmwit.bierapp.util.ImageDownloader;

public class UserAdapter extends ArrayAdapter<User> {
	private View view;
	
    public UserAdapter(Context context){  
        super(context, R.layout.listview_row_user);
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent){
        this.view = convertView;
        
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_row_user, null);
        }
        
        // Set properties
        TextView name = (TextView) this.view.findViewById(R.id.name);
        ImageView avatar = (ImageView) this.view.findViewById(R.id.avatar);
        
        // Set properties
        name.setText(this.getItem(pos).getFullName());
        ImageDownloader downloader = new ImageDownloader();
        
        if (this.getItem(pos).getAvatarUrl() != null) { 
        	downloader.download(this.getItem(pos).getAvatarUrl(), avatar);
        } else {
        	// Set random avatar
        	int[] avatars = {
        		R.drawable.avatar_1,
        		R.drawable.avatar_2,
        		R.drawable.avatar_3,
        		R.drawable.avatar_4,
        		R.drawable.avatar_5,
        		R.drawable.avatar_6,
        		R.drawable.avatar_7,
        		R.drawable.avatar_8,
        		R.drawable.avatar_9,
        		R.drawable.avatar_10
        	};
        	
        	// Select random avatar from list
        	int randomAvatar = avatars[(new Random().nextInt(avatars.length))];
        	avatar.setImageResource(randomAvatar);
        }

        // Done
        return view;
    }
}
