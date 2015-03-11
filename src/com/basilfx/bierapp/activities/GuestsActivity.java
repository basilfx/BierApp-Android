package com.basilfx.bierapp.activities;

import java.util.List;

import android.os.Bundle;
import android.widget.ListView;

import com.google.common.collect.Lists;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.basilfx.bierapp.R;
import com.basilfx.bierapp.data.adapters.GuestListAdapter;
import com.basilfx.bierapp.data.models.User;
import com.basilfx.bierapp.database.DatabaseHelper;
import com.basilfx.bierapp.database.HostingHelper;
import com.basilfx.bierapp.database.UserHelper;

public class GuestsActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private ListView guestListView;
	private GuestListAdapter guestListAdapter;
	
	private UserHelper userHelper;
	private HostingHelper hostingHelper;
	
	private List<User> guests;
	private List<Integer> userIds;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show back arrow
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Set content
        this.setContentView(R.layout.activity_guests);
        this.setTitle("Gasten");
        
        // Bind controls
        this.guestListView = (ListView) this.findViewById(R.id.list_guests);
        
        // Open model helpers
        this.userHelper = new UserHelper(this.getHelper());
        this.hostingHelper = new HostingHelper(this.getHelper());
        
        // Retrieve data
        this.guests = Lists.newArrayList();
        
        // Bind data
        this.bindData(savedInstanceState);
	}
	
	private void bindData(Bundle savedInstanceState) {
		this.guestListAdapter = new GuestListAdapter(this) {
			@Override
			public int getCount() {
				return GuestsActivity.this.guests.size();
			}

			@Override
			public Object getItem(int position) {
				return GuestsActivity.this.guests.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}
		};
    	
    	// Create sectionizer to seperate transactions by date
    	SimpleSectionAdapter<User> sectionAdapter = new SimpleSectionAdapter<User>(
			this, this.guestListAdapter, R.layout.listview_row_header, R.id.header, new Sectionizer<User>() {
			@Override
			public String getSectionTitleForItem(User instance) {
				// TODO
				if (GuestsActivity.this.userIds.contains(instance.getId())) {
					return "Actief";
				} else {
					return "Inactief";
				}
			}		
		});
    	
	    // Set the adapter and display the data
    	this.guestListView.setAdapter(sectionAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Retrieve all guests
		this.guests = this.userHelper.select()
	    	.whereRoleEq(User.GUEST)
	    	.all();
		this.userIds = this.hostingHelper.select()
			.selectUserIds()
			.whereActiveEq(true)
			.asIntList();
		
		// Refresh UI
		((SimpleSectionAdapter) this.guestListView.getAdapter()).notifyDataSetChanged();
	}
}
