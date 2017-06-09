package de.sb.messenger.persistence;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(schema="messenger", name = "Document")
@DiscriminatorValue(value = "Document")
@PrimaryKeyJoinColumn(name="documentIdentity")
public class Document extends BaseEntity {
	static private final byte[] EMPTY_HASH = mediaHash(new byte[0]);
	
	@Column(name = "contentHash", unique = true, nullable = false)
	@NotNull 
	@Size(min = 32, max = 32)
	private byte[] contentHash;
	
	@Column(name = "contentType", nullable = false)
	@NotNull 
	@Size(min=1, max=63) 
	@Pattern(regexp = "([a-z]+)/([a-z.+-]+)")
	private String contentType;
	
	@Column(name = "content" , nullable = false)
	@NotNull 
	@Size(min = 1, max = 16777215)
	private byte[] content;

	public Document(String contentType, byte[] content) {
		this.contentHash = content == null ? EMPTY_HASH : mediaHash(content);
		this.contentType = contentType;
		this.content = content;
	}

	protected Document() {
		this(null,null);
	}
	
	public byte[] getContentHash() {
		return contentHash;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	static public byte[] mediaHash(byte[] content) {
		try{
			return MessageDigest.getInstance("SHA-256").digest(content);
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}
}
