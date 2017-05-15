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

@Entity
@Table(schema="messenger", name = "BaseEntity")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name = "discriminator", discriminatorType=DiscriminatorType.STRING, length=20)
public class BaseEntity implements Comparable<BaseEntity> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "identity")
	private long identiy;
	
	@Column(name = "version")
	@Min(1)
	private int version;
	
	@Column(name = "creationTimestamp")
	private long creationTimestamp;
	
	@OneToMany(mappedBy = "subject", cascade=CascadeType.REMOVE)
	private Set<Message> messagesCaused;

	public BaseEntity() {
		this.identiy = 0;
		this.version = 1;
		this.creationTimestamp = System.currentTimeMillis();
		messagesCaused = Collections.emptySet();
	}

	public long getIdentiy() {
		return identiy;
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
		return Long.compare(this.identiy, obj.identiy);
	}
}
