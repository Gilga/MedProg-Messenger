package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import de.sb.messenger.persistence.BaseEntity;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.toolbox.Copyright;

@Path("messages")
@Copyright(year=2013, holders="Sascha Baumeister")
public class MessageService {
	
	@PUT
	@Consumes({ TEXT_PLAIN })
	@Produces({ TEXT_PLAIN })
	public long createMessage (@HeaderParam("Authorization") final String authentication, @QueryParam("subjectReference") long subjectReference, String content) {
		Person author = PersonService.getRequester(authentication);
		
		final EntityManager em = EntityService.getEntityManager();
		final EntityManagerFactory emf = em.getEntityManagerFactory();
		
		BaseEntity subject = em.find(BaseEntity.class, subjectReference);
		if (subject == null) throw new ClientErrorException(NOT_FOUND);
		
		Message message  = new Message(author,subject,content);
		
		EntityService.update(em, message);
		
		// clear Cache
		emf.getCache().evict(Person.class, author.getIdentity());
		emf.getCache().evict(BaseEntity.class, subject.getIdentity());
		
		return message.getIdentity();
	}
	
	@GET
	@Path("{identity}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Message getMessage (@PathParam("identity") final long identity) {
		final Message entity = EntityService.getEntityManager().find(Message.class, identity);
		if (entity == null) throw new ClientErrorException(NOT_FOUND);
		return entity;
	}
	
	@GET
	@Path("{identity}/author")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person getAuthor (@PathParam("identity") final long identity) {
		return getMessage(identity).getAuthor();
	}
	
	@GET
	@Path("{identity}/subject")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public BaseEntity getSubject (@PathParam("identity") final long identity) {
		return getMessage(identity).getSubject();
	}
}
