package com.warmwit.bierapp.callbacks;

import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.views.UserRowView;

public interface ProductClickedCallback {
	public void onProductClicked(UserRowView view, User user, Product product);
}
