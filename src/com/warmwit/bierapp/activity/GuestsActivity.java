package com.warmwit.bierapp.activity;

import com.warmwit.bierapp.R;

import android.app.Activity;
import android.os.Bundle;

public class GuestsActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content
        this.setContentView(R.layout.activity_guests);
        this.setTitle("Gasten");
	}
}
