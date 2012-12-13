package com.warmwit.bierapp.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.warmwit.bierapp.R;
import com.warmwit.bierapp.activity.fragments.StatsFragment;
import com.warmwit.bierapp.activity.fragments.StreepFragment;
import com.warmwit.bierapp.util.CustomTabListener;

public class HomeActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content
        setContentView(R.layout.activity_home);
        
        // Initialize action bar
        this.bindActionBar();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context_user, menu);
        Log.i("TEST", "Menu created");
        
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        
		return super.onCreateOptionsMenu(menu);
	}

    private void bindActionBar() {
		ActionBar bar = this.getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Create tabs
		ActionBar.Tab tabStrepen = bar.newTab().setText("Strepen");
		ActionBar.Tab tabStats = bar.newTab().setText("Stats");
		
		// Add listeners to tabs
		tabStrepen.setTabListener(new CustomTabListener<StreepFragment>(this, android.R.id.content, "strepen", StreepFragment.class));
		tabStats.setTabListener(new CustomTabListener<StatsFragment>(this, android.R.id.content, "stats", StatsFragment.class));
		
		// Add tabs
		bar.addTab(tabStrepen);
		bar.addTab(tabStats);
    }
}
