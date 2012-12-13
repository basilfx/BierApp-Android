package com.warmwit.bierapp.data.model;

import com.google.common.base.Joiner;

public class User {
	public static final int INHABITANT = 0;
	public static final int GUEST = 1;
	
	private String firstName;
	private String middleName;
	private String lastName;
	private String avatarUrl;
	private int type;
	
	public String getFullName() {
		return Joiner.on(' ').skipNulls().join(new Object[] { this.firstName, this.middleName.isEmpty() ? null : this.middleName, this.lastName});
	}
	
	public String toString() {
		return this.getFullName();
	}
	
	public String getAvatarUrl() {
		return this.avatarUrl;
	}
	
	public int getType() {
		return this.type;
	}
}
