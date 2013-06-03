package com.warmwit.bierapp.data.models;

import java.util.Date;
import java.util.Map;
import java.util.Observable;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.warmwit.bierapp.utils.ProductInfo;

@DatabaseTable(tableName = "user")
public class User extends Observable {
	public static final int INHABITANT = 0;
	public static final int GUEST = 1;
	
	@DatabaseField(columnName = "id", index = true, id = true)
	private int id;

	@DatabaseField(columnName = "firstName")
	private String firstName;
	
	@DatabaseField(columnName = "lastName")
	private String lastName;
	
	@DatabaseField(columnName = "avatarUrl")
	private String avatarUrl;
	
	@DatabaseField(columnName = "type", index = true)
	private int type;
	
	@DatabaseField(columnName = "score")
	private int score;
	
	@DatabaseField(columnName = "dirty")
	private boolean dirty;
	
	@DatabaseField(columnName = "synced")
	private boolean synced;
	
	@DatabaseField(columnName = "dateChanged", canBeNull = true)
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
        
        return 	Objects.equal(this.id, other.id) &&
        		Objects.equal(this.firstName, other.firstName) &&
        		Objects.equal(this.lastName, other.lastName) &&
        		Objects.equal(this.avatarUrl, other.avatarUrl) &&
        		Objects.equal(this.type, other.type) &&
        		Objects.equal(this.score, other.score) &&
        		Objects.equal(this.dateChanged, other.dateChanged);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.id, this.firstName, this.lastName, 
				this.avatarUrl, this.type, this.score, this.dateChanged);
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

	public Date setDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
}
