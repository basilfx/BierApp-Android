package com.warmwit.bierapp.data.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.ProductClickedCallback;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.utils.ImageDownloader;

public class UserRowView extends LinearLayout {

	private BierAppApplication application;
	private ProductClickedCallback callback;
	private User user;
	private int randomAvatar;
	
	private TextView name;
	private TextView score;
	private ImageView avatar;
	private ImageView more;
	
	private ProductView[] products; 
	
	public UserRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		// Inflate layout
		View.inflate(context, R.layout.listview_row_user, this);
		
		this.randomAvatar = R.drawable.avatar_1;
		
		// Find fields
		this.name = (TextView) this.findViewById(R.id.name);
		this.score = (TextView) this.findViewById(R.id.score);
		this.avatar = (ImageView) this.findViewById(R.id.avatar);
		this.more = (ImageView) this.findViewById(R.id.product_placeholder_more);
		
		// Find products
		this.products = new ProductView[] {
			(ProductView) this.findViewById(R.id.product_1),
			(ProductView) this.findViewById(R.id.product_2),
			(ProductView) this.findViewById(R.id.product_3)
		};
	}
	
	public UserRowView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public UserRowView(Context context) {
		this(context, null, 0);
	}
	
	public void setRow(User user, ProductClickedCallback callback, BierAppApplication application) {
		this.user = user;
		this.callback = callback;
		this.application = application;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public void initView() {
		// Show avatar
		if (Strings.isNullOrEmpty(this.user.getAvatarUrl())) {
			this.avatar.setImageResource(this.randomAvatar);
		} else {
			ImageDownloader downloader = new ImageDownloader();
			downloader.download(this.user.getAvatarUrl(), this.avatar);
		}
		
        // Hide or show balance/score
        switch (this.user.getType()) {
	        case User.INHABITANT:
	        	this.score.setVisibility(View.VISIBLE);
	        	break;
	        case User.GUEST:
	        	this.score.setVisibility(View.INVISIBLE);
	        	break;
        }

        // Add products to the place holders
        for (int i = 0; i < products.length; i++) {
        	if (i < this.application.getApiConnector().getProducts().size()) {
	        	// Retrieve corresponding product
	        	final Product product = this.application.getApiConnector().getProducts().get(i);
	        	final int productIndex = i;
	        	
	        	this.products[i].setupView(this.user.getType() == User.GUEST, 0, 0);
	        	
	        	// Notify the receiving end of the onProductClicked callback
	        	this.products[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						UserRowView.this.callback.onProductClicked(UserRowView.this, product);
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
        		this.products[i].setVisibility(View.GONE);
        	}
        }
        
        // Show the more button if applicable
        if (this.application.getApiConnector().getProducts().size() > products.length) {
        	this.more.setVisibility(View.VISIBLE);
        } else {
        	this.more.setVisibility(View.GONE);
        }
        
        // Set the name, score and XP
 		this.name.setText(this.user.getName());
 		this.score.setText(this.user.getScore() + " XP");
	}
}
