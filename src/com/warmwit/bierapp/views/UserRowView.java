package com.warmwit.bierapp.views;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.callbacks.OnProductClickListener;
import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.utils.FlowLayout;
import com.warmwit.bierapp.utils.ProductInfo;

/**
 *
 * 
 * @author Bas Stottelaar
 */
public class UserRowView extends LinearLayout {	
	private static class ViewHolder { 
		private TextView name;
		private TextView score;
		private ImageView avatar;
		
		private ProductView[] products;
		private ProductView more;
		
	}
	
	private User user;
	private OnProductClickListener callback;
	
	public UserRowView(Context context) {
		super(context);
		
		// Inflate layout
		LayoutInflater.from(context).inflate(R.layout.listview_row_user, this);
		
		// Build a ViewHolder
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
    	
		// Save the holder internally
    	this.setTag(holder);
	}
	
	public User getUser() {
		return this.user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public void setCallback(OnProductClickListener callback) {
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
		int avatar = avatars[new Random().nextInt(avatars.length - 1)];
		
		DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
			.cacheOnDisc()	
			.cacheInMemory()
			.build();
		
		String url = Strings.isNullOrEmpty(user.getAvatarUrl()) ? "drawable://" + avatar : user.getAvatarUrl();
 		ImageLoader.getInstance().displayImage(url, holder.avatar);
	}
	
	public void refreshUser() {
		ViewHolder holder = (ViewHolder) this.getTag();
		
		// Set the name, score and XP
 		holder.name.setText(user.getName());
 		holder.score.setText(user.getScore() + " XP");
	}
	
	public void refreshProducts() {
		ViewHolder holder = (ViewHolder) this.getTag();
		int i = 0;
		
		// Add products to the place holders
        final Map<Product, ProductInfo> productMap = user.getProducts();
        ProductInfo productInfoMore = new ProductInfo(0, 0); 
        
        //for (i = 0; i < holder.products.length; i++) {
        // 	holder.products[i].setVisibility(View.INVISIBLE);
        //}
        
        for (Entry<Product, ProductInfo> item : productMap.entrySet()) {
        	if (i >= holder.products.length) {
        		productInfoMore.setChange(productInfoMore.getChange() + item.getValue().getChange());
        		productInfoMore.setCount(productInfoMore.getCount() + item.getValue().getCount());
        	} else if (i < productMap.size()) {
        		UserRowView.this.refreshProduct(holder.products[i], item.getKey(), item.getValue(), false);
        	}
        	
        	// Increment current item
        	i++;
        }
        
        // Show the more button if applicable
        if (productMap.size() > holder.products.length) {
        	holder.more.setVisibility(View.VISIBLE);
        	
        	holder.more.setCount(productInfoMore.getCount());
        	holder.more.setChange(productInfoMore.getChange());
        	holder.more.setGuestProduct(this.user.getType() == User.GUEST);
        	holder.more.setProductMore();
        	
        	holder.more.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FlowLayout view = new FlowLayout(UserRowView.this.getContext());
					
					for (Entry<Product, ProductInfo> item : productMap.entrySet()) {
						ProductView productView = new ProductView(UserRowView.this.getContext());
						productView.setPadding(5, 5, 5, 5);
						UserRowView.this.refreshProduct(productView, item.getKey(), item.getValue(), true);
						view.addView(productView);
					}
					
	        		new AlertDialog.Builder(UserRowView.this.getContext())
	    		    	.setView(view)
	    		    	.setTitle("Alle producten")
	    		    	.setPositiveButton("Sluiten", null)
	    		    	.show();
				}
			});
        } else {
        	holder.more.setVisibility(View.GONE);
        	holder.more.setOnClickListener(null);
        }
	}
	
	public void refreshProduct(ProductView productView, ProductInfo productInfo) {
		productView.setChange(productInfo.getChange());
		productView.setCount(productInfo.getCount());
	}
	
	private void refreshProduct(final ProductView productView, final Product product, final ProductInfo productInfo, final boolean inDialog) {
		productView.setGuestProduct(this.user.getType() == User.GUEST);
		productView.setChange(productInfo.getChange());
		productView.setCount(productInfo.getCount());
		productView.setProductLogo(product.getLogo());
		
		// Set on click handler
		productView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserRowView.this.callback.onProductClickListener(UserRowView.this, (ProductView) v, UserRowView.this.user, inDialog, product, 1);
			}
		});
		
		// Prevent long clicks
		productView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(final View v) {
				final NumberPicker picker = new NumberPicker(UserRowView.this.getContext());
				
				picker.setMinValue(1);
				picker.setMaxValue(100);
				
				new AlertDialog.Builder(UserRowView.this.getContext())
					.setTitle("Aantal " + product.getTitle() + " selecteren")
					.setView(picker)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							UserRowView.this.callback.onProductClickListener(UserRowView.this, (ProductView) v, UserRowView.this.user, inDialog, product, picker.getValue());
						}
					})
					.show();
				
				return true;
			}
		});
	}
}
