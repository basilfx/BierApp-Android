package com.warmwit.bierapp.callbacks;

import com.warmwit.bierapp.utils.ProductInfo;

public interface OnProductInfoChanged {
	public void onProductCountChanged(ProductInfo info);
	public void onProductChangeChanged(ProductInfo info);
}
