package com.warmwit.bierapp.data.models;

import java.util.Date;
import java.util.Map;
import java.util.Observable;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.warmwit.bierapp.utils.ProductInfo;

@DatabaseTable
public class User extends Observable {
	public static final int INHABITANT = 0;
	public static final int GUEST = 1;
	
	@DatabaseField(index = true, id = true)
	private int id;

	@DatabaseField
	private String firstName;
	
	@DatabaseField
	private String lastName;
	
	@DatabaseField
	private String avatarUrl;
	
	@DatabaseField(index = true)
	private int type;
	
	@DatabaseField
	private int score;
	
	@DatabaseField(foreign = true, canBeNull = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 3)
	private Hosting hosting;
	
	@DatabaseField
	private boolean dirty;
	
	@DatabaseField
	private boolean synced;
	
	@DatabaseField(canBeNull = true)
	private Date dateChanged;
	
	public String getFullName() {
		return Joiner.on(' ').skipNulls().join(new Object[] { this.firstName, this.lastName});
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

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public void setScore(int score) {
		this.score = score;
		this.notifyObservers(0);
	}
	
	public int getScore() {
		return this.score;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof User)) return false;
			
        final User other = (User) o;
        
        return Objects.equal(this.firstName, other.firstName) &&
    		Objects.equal(this.lastName, other.lastName) &&
    		Objects.equal(this.avatarUrl, other.avatarUrl) &&
    		Objects.equal(this.type, other.type);
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isSynced() {
		return synced;
	}

	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	Map<Product, ProductInfo> productMap;
	
	public void setProducts(Map<Product, ProductInfo> productMap) {
		this.productMap = productMap;
	}

	public Map<Product, ProductInfo> getProducts() {
		return this.productMap;
	}

	public Hosting getHosting() {
		return hosting;
	}

	public void setHosting(Hosting hosting) {
		this.hosting = hosting;
	}

	public Date setDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
}
