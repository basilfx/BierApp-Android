package com.warmwit.bierapp.data.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.warmwit.bierapp.R;

public class ProductView extends FrameLayout {

	private TextView change;
	private TextView count;
	private ImageView logo;
	private RelativeLayout bar;
	
	private boolean isGuestProduct;
	
	public ProductView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		// Inflate layout
		View.inflate(context, R.layout.view_product, this);
		
		// Find fields
		this.change = (TextView) this.findViewById(R.id.product_change);
		this.count = (TextView) this.findViewById(R.id.product_count);
		this.logo = (ImageView) this.findViewById(R.id.product_logo);
		this.bar = (RelativeLayout) this.findViewById(R.id.product_bar);
	}
	
	public ProductView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ProductView(Context context) {
		this(context, null, 0);
	}
	
	public void setupView(boolean isGuestProduct, int change, int count) {
		this.isGuestProduct = isGuestProduct;
		
		this.count.setVisibility(isGuestProduct ? View.INVISIBLE : View.VISIBLE);
		this.change.setVisibility(View.VISIBLE);
		this.setChange(change);
		this.setCount(count);
	}
	
	public void setChange(int change) {
		// Hide container if nothing to show
		this.bar.setVisibility(this.isGuestProduct && change == 0 ? View.INVISIBLE : View.VISIBLE);
		this.change.setVisibility(change == 0 ? View.INVISIBLE : View.VISIBLE);
		
		// Update content
		this.change.setTextColor(change < 0 ? Color.RED : Color.GREEN);
		this.change.setText(change + "");
	}
	
	public void setCount(int count) {
		if (this.isGuestProduct)
			return;
		
		// Update content
		this.count.setTextColor(count < 0 ? Color.RED : Color.WHITE);
		this.count.setText(count + "");
	}
}
