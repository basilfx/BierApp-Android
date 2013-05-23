package com.warmwit.bierapp.data;

import com.warmwit.bierapp.R;
import java.util.Date;

public class ApiTransaction {
	public int id;
	public String description;
	public ApiTransactionItem[] transaction_items;
	public Date date_created;
}