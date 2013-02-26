package com.warmwit.bierapp.data.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.ProductClickedCallback;
import com.warmwit.bierapp.data.model.Product;
import com.warmwit.bierapp.data.model.User;
import com.warmwit.bierapp.util.ImageDownloader;

public class UserRowView extends LinearLayout {

	private BierAppApplication application;
	private ProductClickedCallback callback;
	private User user;
	private UserRowState userRowState;
	
	private TextView name;
	private TextView balance;
	private TextView score;
	private TextView change;
	
	private ImageView avatar;
	private ImageView more;
	private ImageView[] products;
	
	public UserRowView(Context context) {
		super(context);
		
		// Inflate layout
		View.inflate(context, R.layout.listview_row_user, this);
		
		// Find fields
		this.name = (TextView) this.findViewById(R.id.name);
		this.balance = (TextView) this.findViewById(R.id.balance);
		this.score = (TextView) this.findViewById(R.id.score);
		this.change = (TextView) this.findViewById(R.id.change);
		this.avatar = (ImageView) this.findViewById(R.id.avatar);
		this.more = (ImageView) this.findViewById(R.id.product_placeholder_more);
		
		// Find products
		this.products = new ImageView[] { 
    		(ImageView) this.findViewById(R.id.product_placeholder_1),
    		(ImageView) this.findViewById(R.id.product_placeholder_2),
    		(ImageView) this.findViewById(R.id.product_placeholder_3),
        };
	}
	
	public void setRow(User user, UserRowState userRowState, ProductClickedCallback callback, BierAppApplication application) {
		this.user = user;
		this.userRowState = userRowState;
		this.callback = callback;
		this.application = application;
	}
	
	public void initView() {
		// Show avatar
		if (Strings.isNullOrEmpty(this.user.getAvatarUrl())) {
			this.avatar.setImageResource(this.userRowState.getRandomAvatar());
		} else {
			ImageDownloader downloader = new ImageDownloader();
			downloader.download(this.user.getAvatarUrl(), this.avatar);
		}
		
        // Hide or show balance/score
        switch (this.user.getType()) {
	        case User.INHABITANT:
	        	this.balance.setVisibility(View.VISIBLE);
	        	this.score.setVisibility(View.VISIBLE);
	        	break;
	        case User.GUEST:
	        	this.balance.setVisibility(View.GONE);
	        	this.score.setVisibility(View.GONE);
	        	break;
        }

        // Add products to the place holders
        for (int i = 0; i < products.length; i++) {
        	if (i < this.application.getApiConnector().getProducts().size()) {
        		// Make sure the view is visible
        		this.products[i].setVisibility(View.VISIBLE);
        		
	        	// Retrieve corresponding product
	        	final Product product = this.application.getApiConnector().getProducts().get(i);
	        	
	        	// Set image
	        	this.products[i].setImageResource(product.getBuiltinLogo());
	        	
	        	// Notify the receiving end of the onProductClicked callback
	        	this.products[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						UserRowView.this.callback.onProductClicked(UserRowView.this.user, product);
					}
				});
	        	
	        	// Prevent long clicks to be detected as single clicks
	        	this.products[i].setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						return false;
					}
				});
        	} else {
        		// Hide view
        		this.products[i].setVisibility(View.INVISIBLE);
        	}
        }
        
        // Show the more button if applicable
        if (this.application.getApiConnector().getProducts().size() > products.length) {
        	this.more.setVisibility(View.VISIBLE);
        } else {
        	this.more.setVisibility(View.INVISIBLE);
        }
        
        // Small UI things are in refreshView()
        this.refreshView();
	}
	
	public void refreshView() {
		int changeValue = this.userRowState.getChange();
		
		// Set the name, score and XP
		this.name.setText(this.user.getName());
		this.score.setText(this.user.getScore() + " XP");
		this.balance.setText(0 + "");

		// Set the change if applicable
		if (changeValue != 0) {
			if (changeValue < 0) {
				change.setTextColor(Color.GREEN);
				change.setText("+" + changeValue);
			} else {
				change.setTextColor(Color.RED);
				change.setText("-" + changeValue);
			}
			
			change.setVisibility(View.VISIBLE);
		} else {
			change.setVisibility(View.GONE);
		}
	}
}
