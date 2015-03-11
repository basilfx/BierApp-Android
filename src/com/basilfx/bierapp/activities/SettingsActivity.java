package com.basilfx.bierapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.basilfx.bierapp.R;

public class SettingsActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show back arrow
        this.getActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        this.getFragmentManager()
        	.beginTransaction()
        	.replace(android.R.id.content, new GeneralFragment())
        	.commit();
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class GeneralFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preference_general, false);

            // Load the preferences from an XML resource
            this.addPreferencesFromResource(R.xml.preference_general);
        }
    }


}
