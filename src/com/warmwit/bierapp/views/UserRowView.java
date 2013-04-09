package com.warmwit.bierapp.views;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.ProductClickedCallback;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.utils.ImageDownloader;
import com.warmwit.bierapp.utils.ProductInfo;

public class UserRowView extends LinearLayout {
	private static ImageDownloader downloader = new ImageDownloader();
	
	private static class ViewHolder { 
		private TextView name;
		private TextView score;
		private ImageView avatar;
		
		private ProductView[] products;
		private ProductView more;
	}
	
	private User user;
	private ProductClickedCallback callback;
	
	public UserRowView(Context context) {
		super(context);
		
		LayoutInflater.from(context).inflate(R.layout.listview_row_user, this);
		ViewHolder holder = new ViewHolder();
    	
    	// Find fields
    	holder.name = (TextView) this.findViewById(R.id.name);
		holder.score = (TextView) this.findViewById(R.id.score);
		holder.avatar = (ImageView) this.findViewById(R.id.avatar);
		holder.more = (ProductView) this.findViewById(R.id.product_more);
		
		holder.products = new ProductView[] {
			(ProductView) this.findViewById(R.id.product_1),
			(ProductView) this.findViewById(R.id.product_2),
			(ProductView) this.findViewById(R.id.product_3)
		};
    	
		// Set Tag
    	this.setTag(holder);
	}
	
	public User getUser() {
		return this.user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public void setCallback(ProductClickedCallback callback) {
		this.callback = callback;
	}
	
	public void refreshAll() {
		this.refreshAvatar();
        this.refreshUser();
        this.refreshProducts();
	}
	
	public void refreshAvatar() {
		ViewHolder holder = (ViewHolder) this.getTag();
		
		// Set at first
		int[] avatars = new int[] {
			R.drawable.avatar_1,
			R.drawable.avatar_2,
			R.drawable.avatar_3,
			R.drawable.avatar_4,
			R.drawable.avatar_5,
			R.drawable.avatar_6,
			R.drawable.avatar_7,
			R.drawable.avatar_8,
			R.drawable.avatar_9,
			R.drawable.avatar_10
		};
		
		holder.avatar.setImageResource(avatars[new Random().nextInt(avatars.length - 1)]);
		
		// Download ad second, if any
 		if (!Strings.isNullOrEmpty(user.getAvatarUrl())) {
 			UserRowView.downloader.download(BierAppApplication.getHostUrl() + user.getAvatarUrl(), holder.avatar);
 		}
	}
	
	public void refreshUser() {
		ViewHolder holder = (ViewHolder) this.getTag();
		
		// Hide or show balance/score
        switch (user.getType()) {
	        case User.INHABITANT:
	        	holder.score.setVisibility(View.VISIBLE);
	        	break;
	        case User.GUEST:
	        	holder.score.setVisibility(View.INVISIBLE);
	        	break;
        }
		
		// Set the name, score and XP
 		holder.name.setText(user.getName());
 		holder.score.setText(user.getScore() + " XP");
	}
	
	public void refreshProducts() {
		ViewHolder holder = (ViewHolder) this.getTag();
		int i = 0;
		
		// Add products to the place holders
        final Map<Product, ProductInfo> productMap = user.getProducts();
        
        for (Entry<Product, ProductInfo> item : productMap.entrySet()) {
        	if (i >= holder.products.length) {
        		break;
        	} else if (i < productMap.size()) {
        		// Set data
        		UserRowView.this.refreshProductView(holder.products[i], item.getKey(), item.getValue());
        	} else {
        		// Hide view
        		holder.products[i].setVisibility(View.GONE);
        	}
        	
        	// Increment current item
        	i++;
        }
        
        // Show the more button if applicable
        if (productMap.size() > holder.products.length) {
        	holder.more.setVisibility(View.VISIBLE);
        	holder.more.setProductMore();
        	
        	holder.more.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LinearLayout view = new LinearLayout(UserRowView.this.getContext());
					
					for (Entry<Product, ProductInfo> item : productMap.entrySet()) {
						ProductView productView = new ProductView(UserRowView.this.getContext());
						UserRowView.this.refreshProductView(productView, item.getKey(), item.getValue());
						view.addView(productView);
					}
					
	        		AlertDialog.Builder builder = new AlertDialog.Builder(UserRowView.this.getContext());
	    		    builder.setView(view);
	    		    builder.setTitle("Alle producten");
	    		    builder.setPositiveButton("Sluiten", null);
	    		    builder.show();
				}
			});
        } else {
        	holder.more.setVisibility(View.GONE);
        	holder.more.setOnClickListener(null);
        }
	}
	
	private void refreshProductView(final ProductView productView, final Product product, final ProductInfo productInfo) {
		productView.setGuestProduct(this.user.getType() == User.GUEST);
		productView.setChange(productInfo.getChange());
		productView.setCount(productInfo.getCount());
		productView.setProductLogo(product.getLogo());
		
		// Set on click handler
		productView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserRowView.this.callback.onProductClicked(UserRowView.this, UserRowView.this.user, product);
			}
		});
		
		// Prevent long clicks
		productView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return false;
			}
		});
	}
}
