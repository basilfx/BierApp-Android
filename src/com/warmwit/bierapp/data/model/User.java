package com.warmwit.bierapp.data.model;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

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

	@Override
	public boolean equals(Object o) {
	 	if (o instanceof User){
	        final User other = (User) o;
	        
	        return Objects.equal(this.firstName, other.firstName) &&
        		Objects.equal(this.middleName, other.middleName) &&
        		Objects.equal(this.lastName, other.lastName) &&
        		Objects.equal(this.avatarUrl, other.avatarUrl) &&
        		Objects.equal(this.type, other.type);
	    } else{
	        return false;
	    }
	}
}
