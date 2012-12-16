package com.warmwit.bierapp.data.adapter;

import java.util.Random;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.model.User;
import com.warmwit.bierapp.util.ImageDownloader;

public class UserRowItem implements Parcelable {
	private int randomAvatar;
	private int change;
	
	public UserRowItem(User user) {
		// Pick random avatar
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
    	this.randomAvatar = avatars[(new Random().nextInt(avatars.length))];
	}
	
	public void setChange(int change) {
		this.change = change;
	}
	
	public void setRow(User user, TextView text, TextView change, ImageView view) {
		// Set the avatar
		if (user.getAvatarUrl() != null) {
			ImageDownloader downloader = new ImageDownloader();
			downloader.download(user.getAvatarUrl(), view);
		} else {
			view.setImageResource(this.randomAvatar);
		}
		
		// Set the name
		text.setText(user.getFullName());
		
		// Set the change
		if (this.change != 0) {
			if (this.change < 0) {
				change.setTextColor(Color.GREEN);
				change.setText("+" + this.change);
			} else {
				change.setTextColor(Color.RED);
				change.setText("-" + this.change);
			}
			change.setVisibility(View.VISIBLE);
		} else {
			change.setVisibility(View.GONE);
		}
	}

	public UserRowItem(Parcel in) {
		this.randomAvatar = in.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.randomAvatar);
	}
	
	public static final Parcelable.Creator<UserRowItem> CREATOR = new Parcelable.Creator<UserRowItem>() {
		public UserRowItem createFromParcel(Parcel in) {
		    return new UserRowItem(in);
		}
		
		public UserRowItem[] newArray(int size) {
		    return new UserRowItem[size];
		}
	};
}