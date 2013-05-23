package com.warmwit.bierapp.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.warmwit.bierapp.R;

public abstract class ProgressAsyncTask<X, Y, T> extends AsyncTask<X, Y, T> {
	private ProgressDialog dialog;
	
	public ProgressAsyncTask(Context context) {
		this.dialog = new ProgressDialog(context);
	}
	
	public ProgressDialog getDialog() {
		return this.dialog;
	}

	@Override
	protected void onPostExecute(T result) {
		// Hide popup dialog
		if (this.dialog.isShowing()) {
			this.dialog.dismiss();
		}
	}

	@Override
	protected void onPreExecute() {
		this.dialog.show();
	}
	
	public void setMessage(String message) {
		this.dialog.setMessage(message);
	}
}
