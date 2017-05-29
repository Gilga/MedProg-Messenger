package de.sb.messenger.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Embeddable 
public class Address {

	
	@Column(name="street", updatable = false, insertable =false)
	@Size(min = 0, max = 63) @Pattern(regexp = "([A-Za-z0-9/. -]{2,} [0-9a-z]+)")
	private String street;
	
	@Column(name="postcode", updatable = false, insertable =false)
	@Size(min = 0, max = 15) @Pattern(regexp = "^[0-9]*$")
	private String postcode;
	
	@Column(name="city", nullable = false, updatable = false, insertable =false)
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
}
