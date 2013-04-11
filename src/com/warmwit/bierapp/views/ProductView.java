package com.warmwit.bierapp.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.R;
import com.warmwit.bierapp.utils.ImageDownloader;

/**
 *
 * 
 * @author Bas Stottelaar
 */
public class ProductView extends FrameLayout {
	private static ImageDownloader downloader = new ImageDownloader();
	
	private static class ViewHolder {
		private TextView change;
		private TextView count;
		private ImageView logo;
		private RelativeLayout bar;	
	}
	
	public ProductView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.view_product, this);
		
		ViewHolder holder = new ViewHolder();
		
		holder.change = (TextView) this.findViewById(R.id.product_change);
		holder.count = (TextView) this.findViewById(R.id.product_count);
		holder.logo = (ImageView) this.findViewById(R.id.product_logo);
		holder.bar = (RelativeLayout) this.findViewById(R.id.product_bar);
		
		this.setTag(holder);
	}
	
	public ProductView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ProductView(Context context) {
		this(context, null, 0);
	}
	
	public void setProductMore() {
		ViewHolder holder = (ViewHolder) this.getTag();
		holder.logo.setImageResource(android.R.drawable.ic_search_category_default);
	}
	
	public void setProductLogo(String logoUrl) {
		ViewHolder holder = (ViewHolder) this.getTag();

		// Set at first
		holder.logo.setImageResource(R.drawable.product_beer_none);
		
		// Download ad second, if any
 		if (!Strings.isNullOrEmpty(logoUrl)) {
 			ProductView.downloader.download(BierAppApplication.getHostUrl() + logoUrl, holder.logo);
 		}
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
		
		// Update content
		holder.change.setTextColor(change < 0 ? Color.RED : Color.GREEN);
		holder.change.setText(change + "");
	}
	
	public void setCount(int count) {
		ViewHolder holder = (ViewHolder) this.getTag();
		
		// Update content
		holder.count.setText(count + "");
	}
}
