package de.sb.messenger.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Embeddable 
public class Name implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9172380838305893592L;

	@Column(name="givenName", nullable = false, updatable = false, insertable = true)
	@NotNull 
	@Size(min = 1, max = 31)
	private String given;
	
	@Column(name="familyName", nullable = false, updatable = false, insertable = true)
	@NotNull 
	@Size(min = 1, max = 31)
	private String family;

	public Name() {
		this.given = null;
		this.family = null;
	}

	public String getGiven() {
		return given;
	}

	public void setGiven(String given) {
		this.given = given;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}
	
	@Override
	public boolean equals(Object o) {
		return this.hashCode() == o.hashCode();
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
}
