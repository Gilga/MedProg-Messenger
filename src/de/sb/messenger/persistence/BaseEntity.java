package de.sb.messenger.persistence;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Min;

@Entity
@Table(name = "BaseEntity")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name = "BaseEntity_Type", discriminatorType=DiscriminatorType.STRING, length=20)
public class BaseEntity implements Comparable<BaseEntity> {

	@Id
	@GeneratedValue
	@Column(name = "identity")
	@Min(value = 0)
	private long identiy;
	
	@Column(name = "version")
	@Min(value = 1)
	private int version;
	
	@Column(name = "creationTimestamp")
	private long creationTimestamp;
	
	@OneToMany(mappedBy = "BaseEntity" , cascade = CascadeType.REMOVE)
	private Set <Message> messagesCaused;

	public BaseEntity() {
		this.identiy = 0;
		this.version = 1;
		this.creationTimestamp = System.currentTimeMillis();
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

	public void setMessagesCaused(Set<Message> messagesCaused) {
		this.messagesCaused = messagesCaused;
	}

	@Override
	public int compareTo(final BaseEntity obj) {
		return Long.compare(this.identiy, obj.identiy);
	}
}
