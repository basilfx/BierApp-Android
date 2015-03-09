package com.basilfx.bierapp.data.models;

import java.util.Date;
import java.util.Map;
import java.util.Observable;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.basilfx.bierapp.utils.ProductInfo;

@DatabaseTable(tableName = "user")
public class User extends Observable {
	public static final int ADMIN = 1;
	public static final int MEMBER = 2;
	public static final int GUEST = 3;
	
	@DatabaseField(columnName = "id", index = true, id = true)
	private int id;

	@DatabaseField(columnName = "firstName")
	private String firstName;
	
	@DatabaseField(columnName = "lastName")
	private String lastName;
	
	@DatabaseField(columnName = "avatarUrl")
	private String avatarUrl;
	
	@DatabaseField(columnName = "role", index = true)
	private int role;
	
	@DatabaseField(columnName = "xp")
	private int xp;

	@DatabaseField(columnName = "synced")
	private boolean synced;
	
	@DatabaseField(columnName = "created")
	private Date created;
	
	@DatabaseField(columnName = "modified", canBeNull = true)
	private Date modified;
	
	@DatabaseField(columnName = "dirty")
	private boolean dirty;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getAvatarUrl() {
		return this.avatarUrl;
	}
	
	public int getRole() {
		return this.role;
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
        		Objects.equal(this.role, other.role) &&
        		Objects.equal(this.created, other.created) &&
        		Objects.equal(this.modified, other.modified);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.id, this.firstName, this.lastName, 
				this.avatarUrl, this.role, this.created, this.modified);
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

	public void setRole(int role) {
		this.role = role;
	}
	
	Map<Product, ProductInfo> productMap;
	
	public void setProducts(Map<Product, ProductInfo> productMap) {
		this.productMap = productMap;
	}

	public Map<Product, ProductInfo> getProducts() {
		return this.productMap;
	}

	public Date getCreated() {
		return this.created;
	}

	public void setCreated(Date created) {
		this.created = created;
		this.dirty = true;
	}
	
	public Date getModified() {
		return this.modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
		this.dirty = true;
	}
	
	public int getXp() {
		return this.xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}
	
	public String getFullName() {
		return Joiner.on(' ').skipNulls().join(new Object[] { this.firstName, this.lastName});
	}
	
	public String toString() {
		return this.getFullName();
	}
}
