package com.warmwit.bierapp.activities;

import android.os.Bundle;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.adapters.GuestListAdapter;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.UserQuery;

public class GuestsActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private ListView guestListView;
	private GuestListAdapter guestListAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content
        this.setContentView(R.layout.activity_guests);
        this.setTitle("Gasten");
        
        // Bind controls
        this.guestListView = (ListView) this.findViewById(R.id.list_guests);
        
        // Bind data
        this.bindData(savedInstanceState);
	}
	
	private void bindData(Bundle savedInstanceState) {
		this.guestListAdapter = new GuestListAdapter(this);
		this.guestListAdapter.addAll(new UserQuery(this).guests());
    	
    	// Create sectionizer to seperate transactions by date
    	SimpleSectionAdapter<User> sectionAdapter = new SimpleSectionAdapter<User>(
			this, this.guestListAdapter, R.layout.listview_row_header, R.id.header, new Sectionizer<User>() {
			@Override
			public String getSectionTitleForItem(User instance) {
				if (instance.getHosting() != null) {
					return "Actief";
				} else {
					return "Inactief";
				}
			}		
		});
    	
	    // Set the adapter and display the data
    	this.guestListView.setAdapter(sectionAdapter);
	}
}
