package com.warmwit.bierapp.activities;

import com.warmwit.bierapp.R;

import android.app.Activity;
import android.os.Bundle;

public class StatsActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content
        this.setContentView(R.layout.activity_stats);
        this.setTitle("Statistieken");
	}
}
