package com.warmwit.bierapp.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.warmwit.bierapp.data.ApiAccessToken;

public class TokenInfo {
	
	private String accessToken;
	
	private String refreshToken;
	
	private Calendar expires;
	
	public TokenInfo(String accessToken, String refreshToken, Calendar expires) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.expires = expires;
	}
	
	public boolean isValid() {
		return this.accessToken != null && this.refreshToken != null && this.expires != null;
	}
	
	public boolean isExpired() {
		return Calendar.getInstance().after(checkNotNull(this.expires));
	}
	
	public void deleteInPreferences(Context context) {
		// Clear information
		this.accessToken = null;
		this.refreshToken = null;
		
		// Overwrite with invalid data
		this.saveToPreferences(context);
	}
	
	public void saveToPreferences(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		
		// Store as keys
		editor.putString("access_token", this.accessToken);
		editor.putString("refresh_token", this.refreshToken);
		editor.putLong("expires", this.expires.getTimeInMillis());
		
		// Commit and done
		editor.commit();
	}
	
	public static TokenInfo createFromApi(ApiAccessToken apiAccessToken) {
		Calendar calendar = Calendar.getInstance();
    	calendar.add(Calendar.SECOND, apiAccessToken.expires_in);
    	
    	// Create object
    	return new TokenInfo(apiAccessToken.access_token, apiAccessToken.refresh_token, calendar);
	}
	
	public static TokenInfo createFromPreferences(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		// Retrieve from keys
		String accessToken = preferences.getString("access_token", null);
    	String refreshToken = preferences.getString("refresh_token", null);
    	long expires = preferences.getLong("expires", 0);
    	
    	// Convert timestamp to calendar object
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(expires);
    	
    	// Create object
    	return new TokenInfo(accessToken, refreshToken, calendar);
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public Calendar getExpires() {
		return expires;
	}
}
