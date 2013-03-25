package com.warmwit.bierapp.callbacks;

import com.warmwit.bierapp.data.adapters.UserRowView;
import com.warmwit.bierapp.data.models.Product;

public interface ProductClickedCallback {
	public void onProductClicked(UserRowView userRowView, Product product);
}
