package com.warmwit.bierapp.tasks;

import static com.google.common.base.Preconditions.checkNotNull;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.actions.SyncAction;
import com.warmwit.bierapp.callbacks.OnRefreshActionListener;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.database.DatabaseHelper;

public class RefreshDataTask extends DialogFragment {
	public static final String LOG_TAG = "RefreshDataTask";
	
	public static final String FRAGMENT_TAG = "RefreshDataTask";
	
	private OnRefreshActionListener listener;
	
	private InnerTask task;
	
	private DatabaseHelper databaseHelper;
	
	private ApiConnector apiConnector;
	
	public static RefreshDataTask newInstance() {
		return new RefreshDataTask();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final ProgressDialog dialog = new ProgressDialog(this.getActivity());

		// Set title and message
		dialog.setMessage(this.getResources().getString(R.string.gegevens_herladen));
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		// Disable the back button
		dialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				return keyCode == KeyEvent.KEYCODE_BACK;
			}
		});

		// Done
		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.listener = (OnRefreshActionListener) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.listener = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Retain this fragment across configuration changes.
	    setRetainInstance(true);
		
	    // Create an API connector
	    this.apiConnector = new ApiConnector(BierAppApplication.getRemoteClient(), this.getHelper());
	    
	    // Create and start task
		this.task = new InnerTask();
		this.task.execute();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (this.databaseHelper != null) {
            OpenHelperManager.releaseHelper();
        }
	}
	
	@Override
	public void onDestroyView() {
		if (this.getDialog() != null && this.getRetainInstance()) {
			this.getDialog().setDismissMessage(null);
		}
		
		super.onDestroyView();
	}
	
	protected DatabaseHelper getHelper() {
        if (this.databaseHelper == null) {
            this.databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        
        return this.databaseHelper;
    }
	
	private class InnerTask extends AsyncTask<Void, Void, Integer> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.d(LOG_TAG, "Task started");
		}

		@Override
		protected Integer doInBackground(Void... params) {
		    return new SyncAction(RefreshDataTask.this.apiConnector).basicSync();
		}
		
		@Override
        protected void onPostExecute(Integer result) {
            checkNotNull(result);
            Log.d(LOG_TAG, "Task finished with result " + result);
            
            // Inform listener
            if (RefreshDataTask.this.listener != null) {
            	RefreshDataTask.this.listener.onRefreshActionResult(result);
            } else {
            	Log.d(LOG_TAG, "Not attached to an activity");
            }
            
            // Hide dialog
            Dialog dialog = RefreshDataTask.this.getDialog();
            
            if (dialog != null) {
            	dialog.dismiss();
            }
		}
	}
}