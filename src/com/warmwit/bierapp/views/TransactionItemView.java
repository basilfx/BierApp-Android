package com.warmwit.bierapp.views;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.database.DatabaseHelper;
import com.warmwit.bierapp.database.ProductQuery;
import com.warmwit.bierapp.database.UserQuery;

public class TransactionItemView extends LinearLayout {
	
	private Spinner products;
	private EditText count;
	private Spinner users;
	private Spinner payers;
	private CheckBox userIsPayer;
	
	private List<Product> productList;
	private List<User> guests;
	private List<User> inhabitants;
	private List<User> userList;
	
	public TransactionItemView(Context context, DatabaseHelper helper) {
		super(context);
		
		// Inflate layout
		LayoutInflater.from(context).inflate(R.layout.dialog_add_transactionitem, this);
		
		// Bind controls
		this.products = (Spinner) this.findViewById(R.id.products);
		this.count = (EditText) this.findViewById(R.id.count);
		this.users = (Spinner) this.findViewById(R.id.users);
		this.payers = (Spinner) this.findViewById(R.id.payers);
		this.userIsPayer = (CheckBox) this.findViewById(R.id.user_is_payer);
		
		// Initialize data
		this.productList = new ProductQuery(helper).all();
		this.guests = new UserQuery(helper).guests();
		this.inhabitants = new UserQuery(helper).inhabitants();
		this.userList = Lists.newArrayList(Iterables.concat(this.inhabitants, this.guests));
		
		this.products.setAdapter(new ArrayAdapter<Product>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, this.productList));
		this.users.setAdapter(new ArrayAdapter<User>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, this.userList));
		this.payers.setAdapter(new ArrayAdapter<User>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, this.inhabitants));
		
		this.userIsPayer.setChecked(true);
		this.users.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				User user = TransactionItemView.this.userList.get(position);
				
				if (user.getType() == User.INHABITANT) {
					if (TransactionItemView.this.userIsPayer.isChecked()) {
						TransactionItemView.this.userIsPayer.setVisibility(View.VISIBLE);
						TransactionItemView.this.payers.setVisibility(View.GONE);
					} else {
						TransactionItemView.this.userIsPayer.setVisibility(View.GONE);
						TransactionItemView.this.payers.setVisibility(View.VISIBLE);
						
						// Set the payer equal to the user
						int index = TransactionItemView.this.inhabitants.indexOf(user); 
						
						if (index > -1) {
							TransactionItemView.this.payers.setSelection(index);
						}
					}
				} else {
					TransactionItemView.this.userIsPayer.setVisibility(View.GONE);
					TransactionItemView.this.payers.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				return;
			}
		});
		
		this.userIsPayer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					TransactionItemView.this.payers.setVisibility(View.VISIBLE);
					TransactionItemView.this.userIsPayer.setVisibility(View.GONE);
				}
			}
		});
	}

	public boolean isValid() {
		return !this.count.getText().toString().isEmpty();
	}
	
	public int getCount() {
		return Integer.valueOf(this.count.getText().toString());
	}
	
	public Product getProduct() {
		return (Product) this.products.getSelectedItem();
	}
	
	public User getUser() {
		return (User) this.users.getSelectedItem();
	}
	
	public User getPayer() {
		User user = (User) this.users.getSelectedItem();
		
		if (this.userIsPayer.isChecked() && user.getType() == User.INHABITANT) {
			return user;
		} else {
			return (User) this.payers.getSelectedItem();
		}
	}
}
