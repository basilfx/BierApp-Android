package com.warmwit.bierapp.data.adapter;

import java.util.Random;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;
import android.widget.TextView;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.model.User;
import com.warmwit.bierapp.util.ImageDownloader;

public class UserRowItem implements Parcelable {
	private int randomAvatar;
	
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
	
	public void setRow(User user, TextView text, ImageView view) {
		// Set the avatar
		if (user.getAvatarUrl() != null) {
			ImageDownloader downloader = new ImageDownloader();
			downloader.download(user.getAvatarUrl(), view);
		} else {
			view.setImageResource(this.randomAvatar);
		}
		
		// Set the name
		text.setText(user.getFullName());
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