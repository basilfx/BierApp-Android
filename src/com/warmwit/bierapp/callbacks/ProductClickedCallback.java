package com.warmwit.bierapp.callbacks;

import com.warmwit.bierapp.data.model.Product;
import com.warmwit.bierapp.data.model.User;

public interface ProductClickedCallback {
	public void onProductClicked(User user, Product product);
}
