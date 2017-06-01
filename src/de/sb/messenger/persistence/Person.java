package de.sb.messenger.persistence;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(schema="messenger", name = "Person")
@DiscriminatorValue(value = "Person")
@PrimaryKeyJoinColumn(name="personIdentity")
@XmlRootElement
@XmlType
public class Person extends BaseEntity {

	@Column(name = "groupAlias", nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull
	@XmlElement
	private Group group;
	
	@Column(name = "email", unique = true, nullable = false)
	@Pattern(regexp = "(.+)@(.+)", message = "{invalid.email}")
	@NotNull
	@Size(min = 1, max = 128)
	private String email;
	
	@Column(name = "passwordHash", nullable = false)
	@NotNull
	@Size(min = 32, max = 32)
	private byte[] passwordHash;

	@Embedded 
	@Valid
	@NotNull
	@XmlElement
	private Name name;

	@Embedded 
	@Valid
	@NotNull
	@XmlElement
	private Address address;
	
	@ManyToOne
	@JoinColumn(name="avatarReference", nullable = false)
	// TODO: Updateable Nullable
	private Document avatar;
	
	@OneToMany(mappedBy = "author", cascade=CascadeType.REMOVE)
	@JoinColumn(updatable = false, insertable =false)
	private Set<Message> messagesAuthored;
	
	@ManyToMany(mappedBy = "peopleObserved", cascade=CascadeType.REMOVE)
	@JoinColumn(updatable = false, insertable =false)
	private Set<Person> peopleObserving;
	
	@ManyToMany
	@JoinTable(
		schema="messenger",
		name = "observationassociation",
		joinColumns = @JoinColumn(name="observingReference", updatable = true, nullable = false),
		inverseJoinColumns = @JoinColumn(name="observedReference" , updatable = false, nullable = false)
	)
	private Set<Person> peopleObserved;

	public Person(String email, Document avatar) {
		this.group = Group.USER;
		this.email = email;
		this.name = new Name();
		this.address = new Address();
		this.avatar = avatar;
		peopleObserved = Collections.emptySet();
		peopleObserving = Collections.emptySet();
		messagesAuthored = Collections.emptySet();
	}

	protected Person() {
		this(null, null);
	}

	static public enum Group {
		ADMIN, USER
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public byte[] getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public Name getName() {
		return name;
	}

	public Address getAddress() {
		return address;
	}

	public Document getAvatar() {
		return avatar;
	}

	public void setAvatar(Document avatar) {
		this.avatar = avatar;
	}

	public Set<Message> getMessagesAuthored() {
		return messagesAuthored;
	}

	public Set<Person> getPeopleObserving() {
		return peopleObserving;
	}

	public Set<Person> getPeopleObserved() {
		return peopleObserved;
	}

	@Size(min = 32, max = 32)
	static public byte[] passwordHash(String password){
		try{
			return MessageDigest.getInstance("SHA-256").digest(password.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new AssertionError(e);
			}
	}
}
