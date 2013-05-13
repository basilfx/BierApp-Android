package com.warmwit.bierapp.data;

import java.util.Date;

public class ApiUser {
	public int id;
	
	public String first_name;
	public String last_name;
	public String avatar;
	public int user_type;
	public Date date_changed;
	
	public ApiUserInfo[] product_info;
}
