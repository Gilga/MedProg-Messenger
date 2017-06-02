package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.sb.messenger.persistence.BaseEntity;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.toolbox.Copyright;
import de.sb.toolbox.net.RestCredentials;
import de.sb.toolbox.net.RestJpaLifecycleProvider;

@Path("messages")
@Copyright(year=2013, holders="Sascha Baumeister")
public class AuctionService {
	
	static EntityManagerFactory messengerManagerFactory = null;
	
	static private EntityManagerFactory getEntityManagerFactory(){
		if(messengerManagerFactory==null) {
			final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
			messengerManagerFactory=messengerManager.getEntityManagerFactory();
		}
		return messengerManagerFactory;
	}
	
	@PUT
	@Path("")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public long createMessage (@HeaderParam("Authorization") final String authentication, BaseEntity subject, String content) { //body, subjectReference
		Person author = PersonService.getRequester(authentication);
		
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
       
        messengerManager.getEntityManagerFactory().getCache().evict(Person.class, author.getIdentiy());
        messengerManager.getEntityManagerFactory().getCache().evict(BaseEntity.class, subject.getIdentiy());
        
		Message message  = new Message(author,subject,content);
        messengerManager.persist(message);
        
		return message.getIdentiy();
	}
	
	@GET
	@Path("{identity}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Message getMessage (@PathParam("identity") final long identity) {
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final Message entity = messengerManager.find(Message.class, identity);
		if (entity == null) throw new NotFoundException();
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
