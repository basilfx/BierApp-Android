package com.warmwit.bierapp.activity.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.activity.HomeActivity;
import com.warmwit.bierapp.data.adapter.UserAdapter;
import com.warmwit.bierapp.data.model.User;

public class StreepFragment extends Fragment {
	private ListView userListView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_strepen, container, false);
		
		// Bind controls
		this.userListView = (ListView) view.findViewById(R.id.userListView);
		
		// Bind data
		this.bindData();
		
		// Done
		return view;
	}
	
    private void bindData() {
    	BierAppApplication application = (BierAppApplication) this.getActivity().getApplication();
    	UserAdapter userAdapter = new UserAdapter(this.getActivity());
    	userAdapter.addAll(application.users);
    	
    	SimpleSectionAdapter<User> sectionAdapter = new SimpleSectionAdapter<User>(
    			this.getActivity(), userAdapter, R.layout.listview_row_header, R.id.header, new Sectionizer<User>() {
				@Override
				public String getSectionTitleForItem(User instance) {
					if (instance.getType() == User.INHABITANT) {
						return "Bewoners";
					} else if (instance.getType() == User.GUEST) {
						return "Gasten";
					}
					
					// Not good
					return null;
				}		
    		}
    	);
    	
	    	// Set the adapter and display the data
    	this.userListView.setAdapter(sectionAdapter);
    	
    	// Add context menu to list
    	this.registerForContextMenu(this.userListView);
    }
}
