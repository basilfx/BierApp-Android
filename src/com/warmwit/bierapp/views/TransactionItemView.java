package com.warmwit.bierapp.views;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.Transaction;
import com.warmwit.bierapp.data.models.TransactionItem;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.ProductHelper;
import com.warmwit.bierapp.database.TransactionHelper;
import com.warmwit.bierapp.database.TransactionItemHelper;
import com.warmwit.bierapp.database.UserHelper;

public class TransactionItemView extends DialogFragment implements OnCheckedChangeListener, OnItemSelectedListener, OnClickListener {
	
	private static final String LOG_TAG = "TransactionItemView";
	
	public static final int ACTION_CREATE = 1;
	public static final int ACTION_UPDATE = 2;
	
	public static interface OnTransactionItemListener {
		public void onTransactionItemCreated(TransactionItem transactionItem);
		
		public void onTransactionItemUpdated(TransactionItem transactionItem);
	}
	
	private int action;
	private int transactionId;
	private int transactionItemId;
	
	private Spinner products;
	private EditText count;
	private Spinner users;
	private Spinner payers;
	private CheckBox userIsPayer;
	
	private List<Product> productList;
	private List<User> guests;
	private List<User> inhabitants;
	private List<User> userList;
	
	private DatabaseHelper databaseHelper;
	private TransactionHelper transactionHelper;
	private TransactionItemHelper transactionItemHelper;
	private ProductHelper productHelper;
	private UserHelper userHelper;
	
	private OnTransactionItemListener callback;

	public static TransactionItemView createInstance(Transaction transaction) {
		TransactionItemView view = new TransactionItemView();
		Bundle arguments = new Bundle();
		
		arguments.putInt("action", ACTION_CREATE);
		arguments.putInt("transactionId", transaction.getId());
		arguments.putInt("transactionItemId", -1);
		
		view.setArguments(arguments);
		
		return view;
	}
	
	public static TransactionItemView createInstance(TransactionItem transactionItem) {
		TransactionItemView view = new TransactionItemView();
		Bundle arguments = new Bundle();
		
		arguments.putInt("action", ACTION_UPDATE);
		arguments.putInt("transactionId", -1);
		arguments.putInt("transactionItemId", transactionItem.getId());
		
		view.setArguments(arguments);
		
		return view;
	}
	
    protected DatabaseHelper getHelper() {
        if (this.databaseHelper == null) {
            this.databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        
        return this.databaseHelper;
    }
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.transactionHelper = new TransactionHelper(this.getHelper());
		this.transactionItemHelper = new TransactionItemHelper(this.getHelper());
		this.productHelper = new ProductHelper(this.getHelper());
		this.userHelper = new UserHelper(this.getHelper());
		
		this.inhabitants = this.userHelper.select()
			.whereTypeEq(User.INHABITANT)
			.all();
		this.guests = this.userHelper.select()
			.whereTypeEq(User.GUEST)
			.all();
		this.productList = this.productHelper.select()
			.all();
		this.userList = Lists.newArrayList(Iterables.concat(this.inhabitants, this.guests));
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        
        if (this.databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            
            this.databaseHelper = null;
            this.transactionHelper = null;
            this.transactionItemHelper = null;
            this.productHelper = null;
            this.userHelper = null;
        }
    }
    
    @Override
	public void onStart() {
		super.onStart();
		
		// Wrapper for the positive button to allow manual dismiss
		final AlertDialog dialog = (AlertDialog) this.getDialog();
		
	    if (dialog != null) {
	        Button button = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
	        button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransactionItemView.this.onClick(dialog, Dialog.BUTTON_POSITIVE);
                }
            });
	    }
	}
    
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("products", this.products.getSelectedItemPosition());
		outState.putString("count", this.count.getText().toString());
		outState.putInt("users", this.users.getSelectedItemPosition());
		outState.putInt("payers", this.payers.getSelectedItemPosition());
		outState.putBoolean("userIsPayer", this.userIsPayer.isChecked());
		
		outState.putInt("action", this.action);
		outState.putInt("transactionId", this.transactionId);
		outState.putInt("transactionItemId", this.transactionItemId);
		
		super.onSaveInstanceState(outState);
	}
    
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_transactionitem, null);
		
		// Bind controls
		this.products = (Spinner) view.findViewById(R.id.products);
		this.count = (EditText) view.findViewById(R.id.count);
		this.users = (Spinner) view.findViewById(R.id.users);
		this.payers = (Spinner) view.findViewById(R.id.payers);
		this.userIsPayer = (CheckBox) view.findViewById(R.id.user_is_payer);
		
		// Initialize data		
		this.products.setAdapter(new ArrayAdapter<Product>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, this.productList));
		this.users.setAdapter(new ArrayAdapter<User>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, this.userList));
		this.payers.setAdapter(new ArrayAdapter<User>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, this.inhabitants));
		
		// Restore view if applicable
		if (savedInstanceState != null) {
			this.products.setSelection(savedInstanceState.getInt("products"));
			this.count.setText(savedInstanceState.getString("count"));
			this.users.setSelection(savedInstanceState.getInt("users"));
			this.payers.setSelection(savedInstanceState.getInt("payers"));
			this.userIsPayer.setChecked(savedInstanceState.getBoolean("userIsPayer"));
			
			this.action = savedInstanceState.getInt("action");
			this.transactionId = savedInstanceState.getInt("transactionId");
			this.transactionItemId = savedInstanceState.getInt("transactionItemId");
		} else if (this.getArguments() != null) {
			Bundle arguments = this.getArguments();
			
			this.action = arguments.getInt("action");
			this.transactionId = arguments.getInt("transactionId");
			this.transactionItemId = arguments.getInt("transactionItemId");
			
			if (this.action == ACTION_UPDATE) {
				TransactionItem transactionItem = this.transactionItemHelper.select()
					.whereIdEq(this.transactionItemId)
					.first();
				
				int userIndex = this.findUser(this.userList, transactionItem.getUser().getId());
				int payerIndex = this.findUser(this.inhabitants, transactionItem.getPayer().getId());
				int productIndex = this.findProduct(this.productList, transactionItem.getProduct().getId());
				
				User user = this.userList.get(userIndex);
				User payer = this.inhabitants.get(payerIndex);
				
				this.products.setSelection(productIndex);
				this.count.setText(transactionItem.getCount() + "");
				this.users.setSelection(userIndex);
				this.payers.setSelection(payerIndex);
				this.userIsPayer.setChecked(user.getType() == User.INHABITANT && user.equals(payer));
			}
		} else {
			throw new IllegalStateException("Dialog started without arguments bundle");
		}
		
		// Add event listeners via post since onItemSelected would be triggered too early
		view.post(new Runnable() {
		    public void run() {
		        TransactionItemView.this.users.setOnItemSelectedListener(TransactionItemView.this);
		        TransactionItemView.this.userIsPayer.setOnCheckedChangeListener(TransactionItemView.this);
		    }
		});
		
		// Trigger UI update
		this.onItemSelected(null, null, this.users.getSelectedItemPosition(), 0);
		
		// Build dialog
		AlertDialog dialog = new AlertDialog.Builder(this.getActivity())
			.setTitle("Transactie-item toevoegen")
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(this.action == ACTION_CREATE ? R.string.add : R.string.edit, null)
			.setView(view)
			.create();
		
		return dialog;
	}

	public boolean isValid() {	
		// Check for valid count
		if (this.count.getText().toString().isEmpty()) {
			new AlertDialog.Builder(this.getActivity())
				.setTitle("Invoerfout")
				.setMessage("Er is geen aantal ingevuld!")
				.show();
			
			return false;
		}
		
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		User user = this.userList.get(position);
		
		if (user.getType() == User.INHABITANT) {
			if (this.userIsPayer.isChecked()) {
				this.userIsPayer.setVisibility(View.VISIBLE);
				this.payers.setVisibility(View.GONE);
			} else {
				this.userIsPayer.setVisibility(View.GONE);
				this.payers.setVisibility(View.VISIBLE);
				
				// Set the payer equal to the user
				int index = this.inhabitants.indexOf(user); 
				
				if (index > -1) {
					this.payers.setSelection(index);
				}
			}
		} else {
			this.userIsPayer.setVisibility(View.GONE);
			this.payers.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		return;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (!isChecked) {
			this.payers.setVisibility(View.VISIBLE);
			this.userIsPayer.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (!this.isValid()) {
			return;
		}
		
		TransactionItem transactionItem;
		
		if (this.action == ACTION_CREATE) {
			Transaction transaction = this.transactionHelper.select()
				.whereIdEq(this.transactionId)
				.first();
			
			transactionItem = new TransactionItem();
			transactionItem.setTransaction(transaction);
		} else {
			transactionItem = this.transactionItemHelper.select()
				.whereIdEq(this.transactionItemId)
				.first();
		}
		
		// Set the (new) information
		transactionItem.setProduct((Product) this.products.getSelectedItem());
		transactionItem.setCount(Integer.valueOf(this.count.getText().toString()));
		
		User user = (User) this.users.getSelectedItem();
		
		if (this.userIsPayer.isChecked() && user.getType() == User.INHABITANT) {
			transactionItem.setUser(user);
			transactionItem.setPayer(user);
		} else {
			transactionItem.setUser(user);
			transactionItem.setPayer((User) this.payers.getSelectedItem());
		}
		
		// Save or update
		if (this.action == ACTION_CREATE) {
			this.transactionItemHelper.create(transactionItem);
		} else {
			this.transactionItemHelper.update(transactionItem);
		}
		
		// Notify parent view
		if (callback != null) {
			if (this.action == ACTION_CREATE) {
				callback.onTransactionItemCreated(transactionItem);
			} else {
				callback.onTransactionItemUpdated(transactionItem);
			}
		}
		
		// We can now dismiss it the dialog
		dialog.dismiss();
	}

	@Override
	public void onAttach(Activity activity) {
		if (activity instanceof OnTransactionItemListener) {
			this.callback = (OnTransactionItemListener) activity;
		} else {
			this.callback = null;
		}
		
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		this.callback = null;
		super.onDetach();
	}
	
	private int findUser(List<User> list, int id) {
		int size = list.size();
		
		for (int i = 0; i < size; i++) {
			if (list.get(i).getId() == id) {
				return i;
			}
		}
		
		return -1;
	}
	
	private int findProduct(List<Product> list, int id) {
		int size = list.size();
		
		for (int i = 0; i < size; i++) {
			if (list.get(i).getId() == id) {
				return i;
			}
		}
		
		return -1;
	}
}
