package com.warmwit.bierapp.data.listener;


public interface CRUDListener<T>{
	
	public void onCreate(T t);
	public void onUpdate(T t);
	public void onDelete(T t);
}
