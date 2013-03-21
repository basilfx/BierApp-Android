package com.warmwit.bierapp.data.adapters;

import java.util.Random;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.utils.ImageDownloader;

public class UserRowState implements Parcelable {
	private int randomAvatar;
	private int change;

	public UserRowState(User user) {
		// Pick random avatar
		int[] avatars = { R.drawable.avatar_1, R.drawable.avatar_2,
				R.drawable.avatar_3, R.drawable.avatar_4, R.drawable.avatar_5,
				R.drawable.avatar_6, R.drawable.avatar_7, R.drawable.avatar_8,
				R.drawable.avatar_9, R.drawable.avatar_10 };

		// Select random avatar from list
		this.randomAvatar = avatars[(new Random().nextInt(avatars.length))];
	}

	public void setChange(int change) {
		this.change = change;
	}
	
	public int getRandomAvatar() {
		return this.randomAvatar;
	}
	
	public int getChange() {
		return this.change;
	}

	public UserRowState(Parcel in) {
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

	public static final Parcelable.Creator<UserRowState> CREATOR = new Parcelable.Creator<UserRowState>() {
		public UserRowState createFromParcel(Parcel in) {
			return new UserRowState(in);
		}

		public UserRowState[] newArray(int size) {
			return new UserRowState[size];
		}
	};
}