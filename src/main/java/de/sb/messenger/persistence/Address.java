package de.sb.messenger.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Embeddable 
public class Address implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4897635157204661033L;

	@Column(name="street", updatable = false, insertable = true)
	@Size(min = 0, max = 63) @Pattern(regexp = "([A-Za-z0-9/. -]{2,} [0-9a-z]+)")
	private String street;
	
	@Column(name="postcode", updatable = false, insertable = true)
	@Size(min = 0, max = 15) @Pattern(regexp = "^[0-9]*$")
	private String postcode;
	
	@Column(name="city", nullable = false, updatable = false, insertable = true)
	@NotNull @Size(min = 1, max = 63) @Pattern(regexp = "([A-Za-z/ -]{2,})")
	private String city;
	
	public Address() {
		this.street = null;
		this.postcode = null;
		this.city = null;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
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
