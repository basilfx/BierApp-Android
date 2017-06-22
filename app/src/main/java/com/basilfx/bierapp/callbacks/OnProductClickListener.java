package com.basilfx.bierapp.callbacks;

import com.basilfx.bierapp.data.models.Product;
import com.basilfx.bierapp.data.models.User;
import com.basilfx.bierapp.views.ProductView;
import com.basilfx.bierapp.views.UserRowView;

public interface OnProductClickListener {
	public void onProductClickListener(UserRowView userView, ProductView productView, User user, boolean inDialog, Product product, int count);
}
