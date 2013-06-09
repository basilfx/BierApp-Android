package com.warmwit.bierapp.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.http.auth.AuthenticationException;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import com.j256.ormlite.dao.CloseableIterator;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.actions.Action;
import com.warmwit.bierapp.data.ApiConnector;
import com.warmwit.bierapp.data.adapters.TransactionItemEditorListAdapter;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database2.TransactionHelper;
import com.warmwit.bierapp.database2.TransactionItemHelper;
import com.warmwit.bierapp.exceptions.UnexpectedData;
import com.warmwit.bierapp.exceptions.UnexpectedStatusCode;
import com.warmwit.bierapp.utils.LogUtils;
import com.warmwit.bierapp.utils.ProgressAsyncTask;
import com.warmwit.bierapp.views.TransactionItemView;
import com.warmwit.bierapp.views.TransactionItemView.OnTransactionItemListener;

public class TransactionEditorActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnMenuItemClickListener, OnTransactionItemListener {
	
	public static final String LOG_TAG = "TransactionEditorActivity";
	
	private static final String TRANSACTION_TAG = LOG_TAG;
	
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
	private TransactionHelper transactionHelper;
	private TransactionItemHelper transactionItemHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// Connect to API
		this.apiConnector = new ApiConnector(BierAppApplication.getRemoteClient(), this.getHelper());
		
		// Create helpers for transactions
        this.transactionHelper = new TransactionHelper(this.getHelper());
        this.transactionItemHelper = new TransactionItemHelper(this.getHelper());
		
		// Initialize the layout
		this.setContentView(R.layout.activity_transaction_editor);
		this.setResult(-1);
		
		this.focusDummy = (LinearLayout) this.findViewById(R.id.focus_dummy);
		this.description = (EditText) this.findViewById(R.id.description);
		this.addTransactionItem = (Button) this.findViewById(R.id.add_transactionitem);
		this.transactionItemList = (ListView) this.findViewById(R.id.transaction_items);
		
		this.transactionItems = Lists.newArrayList();
		
		if (savedInstanceState == null) {
			// Decide what to do based on intent action
			Intent intent = this.getIntent();
			String action = Strings.nullToEmpty(intent.getAction());
			
			if (action.equals("") || action.equals(ACTION_ADD_NORMAL_TRANSACTION)) {
				this.description.setText("Handmatige transactie vanaf Tablet");
			} else if (action.equals(ACTION_ADD_TEMPLATE_TRANSACTION)) {
				
			} else if (action.equals(ACTION_UNDO_TRANSACTION)) {
				int id = intent.getIntExtra("transactionId", -1);
				
				if (id != -1) { 
					Transaction oldTransaction = this.transactionHelper.select()
						.whereIdEq(id)
						.whereRemoteIdNeq(null)
						.first();
					
					if (transaction != null) {
						this.description.setText("Tegentransactie #" + oldTransaction.getRemoteId() + " vanaf Tablet");
						
						CloseableIterator<TransactionItem> iterator = oldTransaction.getTransactionItems().closeableIterator();
						
						try {
							TransactionItem oldTransactionItem = iterator.next();
							TransactionItem newTransactionItem = new TransactionItem();
							
							newTransactionItem.setCount(oldTransactionItem.getCount() * -1);
							newTransactionItem.setPayer(oldTransactionItem.getPayer());
							newTransactionItem.setUser(oldTransactionItem.getUser());
							newTransactionItem.setProduct(oldTransactionItem.getProduct());
							
							this.transactionItems.add(newTransactionItem);
						} finally {
							try {
								iterator.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
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
		} else {
			int id = savedInstanceState.getInt("transactionId", -1);
			
			this.transaction = this.transactionHelper.select()
				.whereTagEq(TRANSACTION_TAG)
				.whereRemoteIdNeq(null)
				.whereIdEq(id)
				.first(); 
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
				FragmentTransaction transaction = TransactionEditorActivity.this.getFragmentManager().beginTransaction();
				Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			    if (prev != null) {
			        transaction.remove(prev);
			    }
			    transaction.addToBackStack(null);

			    // Create and show the dialog.
			    TransactionItemView fragment = TransactionItemView.createInstance(TransactionEditorActivity.this.transaction);
			    fragment.show(transaction, "dialog");
			}
		});
		
		// Add context menu to list
		this.registerForContextMenu(this.transactionItemList);
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater(); 
	    inflater.inflate(R.menu.menu_context_transaction_editor, menu);
        
        super.onCreateContextMenu(menu, v, menuInfo);
    }

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save:
				checkNotNull(this.transaction);
				
				if (this.description.getText().toString().isEmpty()) {
					new AlertDialog.Builder(this)
						.setTitle("Invoerfout")
						.setMessage("Omschrijving mag niet leeg zijn.")
						.setPositiveButton(android.R.string.ok, null)
						.show();
						
					return true;
				}
				
				this.transaction.setDescription(this.description.getText().toString());
				this.transaction.setDateCreated(new Date());
				
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
		
		switch (item.getItemId()) {
			case R.id.menu_context_edit_transaction_item:
				TransactionItem transactionItem = this.transactionItems.get(info.position);
				
				FragmentTransaction transaction = TransactionEditorActivity.this.getFragmentManager().beginTransaction();
				Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			    if (prev != null) {
			        transaction.remove(prev);
			    }
			    transaction.addToBackStack(null);

			    // Create and show the dialog.
			    TransactionItemView fragment = TransactionItemView.createInstance(this.transaction);
			    fragment.show(transaction, "dialog");
				
				return true;
			case R.id.menu_context_delete_transaction_item:
				this.transactionItems.remove(info.position);
				((TransactionItemEditorListAdapter) this.transactionItemList.getAdapter()).notifyDataSetChanged();
				
				return true;
			default:
    			return super.onContextItemSelected(item);
			
		}
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

	@Override
	public void onTransactionItemCreated(TransactionItem transactionItem) {		
		this.transactionItems.add(transactionItem);
		((TransactionItemEditorListAdapter) this.transactionItemList.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onTransactionItemUpdated(TransactionItem transactionItem) {
		((TransactionItemEditorListAdapter) this.transactionItemList.getAdapter()).notifyDataSetChanged();
	}
}
