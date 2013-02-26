package com.warmwit.bierapp.data.model;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class User {
	public static final int INHABITANT = 0;
	public static final int GUEST = 1;
	
	private int id;

	private String firstName;
	private String middleName;
	private String lastName;
	private String avatarUrl;
	
	protected int type;
	
	public User() {
		this.type = User.INHABITANT;
	}
	
	public String getFullName() {
		return Joiner.on(' ').skipNulls().join(new Object[] { this.firstName, Strings.isNullOrEmpty(this.middleName) ? null : this.middleName, this.lastName});
	}
	
	public String toString() {
		return this.getFullName();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getAvatarUrl() {
		return this.avatarUrl;
	}
	
	public int getType() {
		return this.type;
	}

	public String getName() {
		return this.firstName;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public int getScore() {
		return 42;
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
