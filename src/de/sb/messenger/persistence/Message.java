package de.sb.messenger.persistence;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(schema="messenger", name = "Message")
@DiscriminatorValue(value = "Message")
@PrimaryKeyJoinColumn(name="messageIdentity")
public class Message extends BaseEntity {
	
	@ManyToOne
	@JoinColumn(name="authorReference", nullable = false)
	private Person author;
	
	@ManyToOne
	@JoinColumn(name="subjectReference", nullable = false)
	private BaseEntity subject;
	
	@Column(name = "body", nullable = false)
	@Size(min = 1, max = 4093)
	private String body;

	public Message(Person author, BaseEntity subject, String body) {
		this.author = author;
		this.subject = subject;
		this.body = body;
	}

	protected Message() {
		this(null, null, null);
	}
	
	public Person getAuthor() {
		return author;
	}

	public BaseEntity getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public BaseEntity getSubjectReference() {
		return this.subject;
	}
}
