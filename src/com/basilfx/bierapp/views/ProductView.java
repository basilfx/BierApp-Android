package com.basilfx.bierapp.views;

import java.io.FileNotFoundException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.basilfx.bierapp.BierAppApplication;
import com.basilfx.bierapp.R;
import com.basilfx.bierapp.utils.Convert;
import com.google.common.base.Strings;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 *
 * 
 * @author Bas Stottelaar
 */
public class ProductView extends FrameLayout {
	public static final String LOG_TAG = "ProductView";
	
	private static class ViewHolder {
		private TextView change;
		private TextView count;
		private TextView title;
		private ImageView logo;
		private RelativeLayout bar;	
	}
	
	public ProductView(Context context) {
		super(context);
		
		// Inflate layout and create a holder
		LayoutInflater.from(context).inflate(R.layout.view_product, this);
		Convert convert = new Convert(context);
		
		// Configure layout parameters
		if (this.getLayoutParams() == null) {
			this.setLayoutParams(new LinearLayout.LayoutParams(convert.toPx(65), convert.toPx(65)));
		} else {
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.getLayoutParams();
			params.width = convert.toPx(65);
			params.height = convert.toPx(65);
		}
		
		this.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.image_border));
		this.setForeground(this.getResources().getDrawable(convert.toResource(android.R.attr.selectableItemBackground)));

		// Bind controls
		ViewHolder holder = new ViewHolder();
		
		holder.change = (TextView) this.findViewById(R.id.product_change);
		holder.count = (TextView) this.findViewById(R.id.product_count);
		holder.title = (TextView) this.findViewById(R.id.product_title);
		holder.logo = (ImageView) this.findViewById(R.id.product_logo);
		holder.bar = (RelativeLayout) this.findViewById(R.id.product_bar);
		
		// Save holder internally
		this.setTag(holder);
	}
	
	public void setProductMore() {
		ViewHolder holder = (ViewHolder) this.getTag();
		
		ImageLoader.getInstance().displayImage("drawable://" + android.R.drawable.ic_search_category_default, holder.logo);
		holder.count.setVisibility(View.INVISIBLE);
		holder.title.setVisibility(View.INVISIBLE);
	}
	
	public void setProductLogo(String url) {
		final ViewHolder holder = (ViewHolder) this.getTag();
	
		if (Strings.isNullOrEmpty(url) || BierAppApplication.badImageUrls.contains(url)) {
			url = "drawable://" + R.drawable.product_beer_none;
		}
		
		// Make title visible
		holder.title.setVisibility(View.VISIBLE);
		
		// Load image
		ImageLoader.getInstance().displayImage(url, holder.logo, new SimpleImageLoadingListener() {
		    @Override
		    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		    	if (!imageUri.startsWith("drawable://")) {
		    		holder.title.setVisibility(View.INVISIBLE);
		    	}
		    }

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				if (!Strings.isNullOrEmpty(imageUri) && failReason.getCause() instanceof FileNotFoundException) {
					BierAppApplication.badImageUrls.add(imageUri);
				}
			}
		});
	}
	
	public void setGuestProduct(boolean guestProduct) {
		ViewHolder holder = (ViewHolder) this.getTag();
		holder.count.setVisibility(guestProduct ? View.INVISIBLE : View.VISIBLE);
	}
	
	public void setChange(int change) {
		ViewHolder holder = (ViewHolder) this.getTag();
		
		// Hide container if nothing to show
		holder.bar.setVisibility(holder.count.getVisibility() == View.INVISIBLE && change == 0 ? View.INVISIBLE : View.VISIBLE);
		holder.change.setVisibility(change == 0 ? View.INVISIBLE : View.VISIBLE);
		
		// Hide background of change and title
		holder.count.setBackgroundColor(change == 0 ? Color.argb(0xAA, 0, 0, 0) : Color.argb(0, 0, 0, 0));
		holder.title.setBackgroundColor(change == 0 ? Color.argb(0xAA, 0, 0, 0) : Color.argb(0, 0, 0, 0));
		
		// Update content
		holder.change.setTextColor(change < 0 ? Color.RED : Color.GREEN);
		holder.change.setText(change + "");
	}
	
	public void setCount(int count) {
		ViewHolder holder = (ViewHolder) this.getTag();
		
		// Update content
		holder.count.setText(count + "");
	}
	
	public void setTitle(String title) {
		ViewHolder holder = (ViewHolder) this.getTag();
		
		// Update content
		holder.title.setText(title);
	}
}
