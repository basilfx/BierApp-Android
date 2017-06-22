package com.basilfx.bierapp.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.basilfx.bierapp.R;

public class Convert {
	
	private Context context;
	
	public Convert(Context context) {
		this.context = context;
	}
	
	public int toPx(int dp) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return (int) (dp * metrics.density);
	}
	
	public int toResource(int attr) {
		return this.context.getTheme().obtainStyledAttributes(R.style.AppTheme, new int[] { attr }).getResourceId(0, 0);
	}
}
