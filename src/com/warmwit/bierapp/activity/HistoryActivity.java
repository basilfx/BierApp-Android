package com.warmwit.bierapp.activity;

import com.warmwit.bierapp.R;

import android.app.Activity;
import android.os.Bundle;

public class HistoryActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content
        this.setContentView(R.layout.activity_history);
        this.setTitle("Historie");
	}
}
