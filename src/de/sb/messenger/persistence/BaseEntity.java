package de.sb.messenger.persistence;
import java.util.Collections;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(schema="messenger", name = "BaseEntity")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name = "discriminator", discriminatorType=DiscriminatorType.STRING, length=20)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name="BaseEntity")
@XmlSeeAlso({Message.class, Person.class, Document.class})
public class BaseEntity implements Comparable<BaseEntity> {

	@Id
	@NotNull
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "identity")
	@XmlElement
	private long identity;
	
	@NotNull
	@Column(name = "version")
	@Min(1)
	@XmlElement
	private int version;

	@NotNull
	@Column(name = "creationTimestamp")
	@XmlElement
	private long creationTimestamp;
	
	@OneToMany(mappedBy = "subject", cascade=CascadeType.REMOVE)
	private Set<Message> messagesCaused;

	public BaseEntity() {
		this.identity = 0;
		this.version = 1;
		this.creationTimestamp = System.currentTimeMillis();
		messagesCaused = Collections.emptySet();
	}

	public long getIdentity() {
		return identity;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public double getCreationTimestamp() {
		return creationTimestamp;
	}

	public Set <Message> getMessagesCaused() {
		return messagesCaused;
	}

	@Override
	public int compareTo(final BaseEntity obj) {
		return Long.compare(this.identity, obj.identity);
	}
}
