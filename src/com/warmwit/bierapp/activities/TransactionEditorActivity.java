package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.http.auth.AuthenticationException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.actions.Action;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.data.adapters.TransactionItemEditorListAdapter;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.TransactionItemQuery;
import com.warmwit.bierapp.database.TransactionQuery;
import com.warmwit.bierapp.exceptions.UnexpectedData;
import com.warmwit.bierapp.exceptions.UnexpectedStatusCode;
import com.warmwit.bierapp.utils.LogUtils;
import com.warmwit.bierapp.utils.ProgressAsyncTask;
import com.warmwit.bierapp.views.TransactionItemView;

public class TransactionEditorActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnMenuItemClickListener {
	
	public static final String LOG_TAG = "TransactionEditorActivity";
	
	public static final String ACTION_UNDO_TRANSACTION = "com.warmwit.bierapp.ACTION_UNDO_TRANSACTION";
	public static final String ACTION_ADD_TEMPLATE_TRANSACTION = "com.warmwit.bierapp.ACTION_ADD_TEMPLATE_TRANSACTION";
	public static final String ACTION_ADD_NORMAL_TRANSACTION = "com.warmwit.bierapp.ACTION_ADD_NORMAL_TRANSACTION";
	
	private LinearLayout focusDummy;
	private EditText description;
	private Button addTransactionItem;
	private ListView transactionItemList;
	
	private List<TransactionItem> transactionItems;
	
	private Transaction transaction;
	
	private ApiConnector apiConnector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// Connect to API
		this.apiConnector = new ApiConnector(BierAppApplication.getRemoteClient(), this.getHelper());
		
		// Initialize the layout
		this.setContentView(R.layout.activity_transaction_editor);
		this.setResult(-1);
		
		this.focusDummy = (LinearLayout) this.findViewById(R.id.focus_dummy);
		this.description = (EditText) this.findViewById(R.id.description);
		this.addTransactionItem = (Button) this.findViewById(R.id.add_transactionitem);
		this.transactionItemList = (ListView) this.findViewById(R.id.transaction_items);
		
		this.transactionItems = Lists.newArrayList();
		
		// Decide what to do based on intent action
		Intent intent = this.getIntent();
		String action = Strings.nullToEmpty(intent.getAction());
		
		if (action.equals("") || action.equals(ACTION_ADD_NORMAL_TRANSACTION)) {
			
		} else if (action.equals(ACTION_ADD_TEMPLATE_TRANSACTION)) {
			
		} else if (action.equals(ACTION_UNDO_TRANSACTION)) {
			int id = intent.getIntExtra("transaction", -1);
			
			if (id != -1) {
				TransactionQuery transactionQuery = new TransactionQuery(this);
				TransactionItemQuery transactionItemQuery = new TransactionItemQuery(this);
				Transaction transaction = transactionQuery.byId(id);
				
				if (transaction != null && transaction.isSynced() == true) {
					this.description.setText("Tegentransactie van #" + id);
					
					List<TransactionItem> oldTransactionItems = transactionItemQuery.byTransaction(transaction);
					
					for (TransactionItem oldTransactionItem : oldTransactionItems) {
						TransactionItem newTransactionItem = new TransactionItem();
						
						newTransactionItem.setCount(oldTransactionItem.getCount() * -1);
						newTransactionItem.setPayer(oldTransactionItem.getPayer());
						newTransactionItem.setUser(oldTransactionItem.getUser());
						newTransactionItem.setProduct(oldTransactionItem.getProduct());
						
						this.transactionItems.add(newTransactionItem);
					}
				} else {
					Log.w(LOG_TAG, "Transaction with ID " + id + " not found");
				}
			} else {
				Log.w(LOG_TAG, "Received action to undo transaction, but no transaction ID given");
			}
		} else {
			Log.e(LOG_TAG, "Unknown intent action: " + action);
		}
		
		// Bind actions
		this.transactionItemList.setAdapter(new TransactionItemEditorListAdapter(this) {
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				return TransactionEditorActivity.this.transactionItems.get(position);
			}
			
			@Override
			public int getCount() {
				return TransactionEditorActivity.this.transactionItems.size();
			}
		});
		
		this.addTransactionItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Create dialog contents
				final TransactionItemView view = new TransactionItemView(TransactionEditorActivity.this, TransactionEditorActivity.this.getHelper());
				
				// Create the dialog self
				new AlertDialog.Builder(TransactionEditorActivity.this)
					.setTitle("Transactie-item toevoegen")
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (view.isValid()) {
								TransactionItem transactionItem = new TransactionItem();
								
								transactionItem.setCount(view.getCount());
								transactionItem.setPayer(view.getPayer());
								transactionItem.setUser(view.getUser());
								transactionItem.setProduct(view.getProduct());
								
								TransactionEditorActivity.this.transactionItems.add(transactionItem);
								((TransactionItemEditorListAdapter) TransactionEditorActivity.this.transactionItemList.getAdapter()).notifyDataSetChanged();
							}
						}
					})
					.setView(view)
					.show();
			}
		});
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        //MenuInflater inflater = getMenuInflater(); 
	    //inflater.inflate(R.menu.menu_context_transaction, menu);
        
        super.onCreateContextMenu(menu, v, menuInfo);
    }

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save:
				TransactionQuery transactionQuery = new TransactionQuery(this);
				
				this.transaction = transactionQuery.create(this.description.getText().toString());
				
				for (TransactionItem transactionItem : this.transactionItems) {
					transactionQuery.addTransactionItem(this.transaction, transactionItem);
				}
				
				new SaveTransactionTask().execute();
				return true;
			case R.id.menu_cancel:
				this.finish();
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_transaction_editor, menu);
		
		// Add handler to general menu
		menu.findItem(R.id.menu_save).setOnMenuItemClickListener(this);
		menu.findItem(R.id.menu_cancel).setOnMenuItemClickListener(this);
		
		// Done		
		return super.onCreateOptionsMenu(menu);
	}

	private class SaveTransactionTask extends ProgressAsyncTask<Void, Void, Integer> {
		private Transaction result;
		
		public SaveTransactionTask() {
			super(TransactionEditorActivity.this);
			this.setMessage(TransactionEditorActivity.this.getResources().getString(R.string.transactie_versturen));
	    }

		@Override
		protected Integer doInBackground(Void... params) {
			checkNotNull(TransactionEditorActivity.this.transaction);
			
			// Send transaction to the server
			try {
				this.result = TransactionEditorActivity.this.apiConnector.saveTransaction(TransactionEditorActivity.this.transaction);
				
				if (this.result != null) {
					// Reload new user data
					TransactionEditorActivity.this.apiConnector.loadUserInfo();
					
					return Action.RESULT_OK;
				} else {
					return Action.RESULT_ERROR_INTERNAL;
				}
			} catch (IOException e) {
				return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_CONNECTION);
			} catch (SQLException e) {
				return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SQL);
			} catch (AuthenticationException e) {
				return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_AUTHENTICATION);
			} catch (UnexpectedStatusCode e) {
				return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
			} catch (UnexpectedData e) {
				return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
			}
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			checkNotNull(result);
			
			switch (result) {
				case Action.RESULT_OK: // Ok
					// Done
					Toast.makeText(TransactionEditorActivity.this, R.string.transactie_succesvol_verzonden, Toast.LENGTH_LONG).show();
					TransactionEditorActivity.this.setResult(0);
					TransactionEditorActivity.this.finish();
					
					break;
				case Action.RESULT_ERROR_INTERNAL:
				case Action.RESULT_ERROR_SERVER:
					new AlertDialog.Builder(TransactionEditorActivity.this)
						.setMessage(R.string.opslaan_van_transactie_is_mislukt_probeer_het_nogmaals)
						.setTitle(R.string.transactiefout)
						.create()
						.show();
					
					break;
				case Action.RESULT_ERROR_CONNECTION: // Exception
					new AlertDialog.Builder(TransactionEditorActivity.this)
						.setMessage(R.string.geen_internetverbinding_probeer_het_nogmaals)
						.setTitle(R.string.connectiefout)
						.create()
						.show();
					
					break;
				case Action.RESULT_ERROR_AUTHENTICATION:
					new AlertDialog.Builder(TransactionEditorActivity.this)
						.setMessage(R.string.authenticatie_met_de_server_is_mislukt_de_applicatie_moet_opnieuw_gekoppeld_worden)
						.setTitle(R.string.authenticatiefout)
						.create()
						.show();
					
					break;
				default:
					throw new IllegalStateException("Code: " + result);
			}
			
			super.onPostExecute(result);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		this.description.clearFocus();
	    this.focusDummy.requestFocus();
	}
}
