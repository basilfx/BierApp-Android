package com.warmwit.bierapp.callbacks;

import com.warmwit.bierapp.data.models.Product;
import com.warmwit.bierapp.data.models.User;
import com.warmwit.bierapp.views.ProductView;
import com.warmwit.bierapp.views.UserRowView;

public interface OnProductClickListener {
	public void onProductClickListener(UserRowView userView, ProductView productView, User user, boolean inDialog, Product product, int count);
}
