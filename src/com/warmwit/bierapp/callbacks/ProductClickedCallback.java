package com.warmwit.bierapp.callbacks;

import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.User;

public interface ProductClickedCallback {
	public void onProductClicked(User user, Product product);
}
