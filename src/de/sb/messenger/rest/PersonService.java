package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Arrays;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.sb.messenger.persistence.BaseEntity;
import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.toolbox.Copyright;
import de.sb.toolbox.net.RestCredentials;
import de.sb.toolbox.net.RestJpaLifecycleProvider;

@Path("people")
@Copyright(year=2013, holders="Sascha Baumeister")
public class PersonService {
	static EntityManagerFactory messengerManagerFactory = null;
	
	static private EntityManagerFactory getEntityManagerFactory(){
		if(messengerManagerFactory==null) {
			final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
			messengerManagerFactory=messengerManager.getEntityManagerFactory();
		}
		return messengerManagerFactory;
	}
	
	@GET
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person[] getPeople (final String criteria) {
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		//messengerManager.getCriteriaBuilder().ge(arg0, arg1);
		// TODO
		return null;
	}
	
	// TODO
	@PUT
	public long createsPerson (final Person template, @HeaderParam("Set-Password") final String password) {
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
	
		Person person = null;
		final long identity = template.getIdentiy();
		
		if(identity == 0) {} //person = (Person) messengerManager.create
		else {
			person = getPerson(identity);
			
			// update
			// TODO
		}
		
		if (person == null) throw new ClientErrorException(NOT_FOUND);
		
		if(password.equals("") != true) {
			// TODO
		}
		
		return identity;
	}
	
	@GET
	@Path("requester")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person getRequester (@HeaderParam("Authorization") final String authentication) {
		return Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
	}
	
	@GET
	@Path("{identity}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person getPerson (@PathParam("identity") final long identity) {
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final BaseEntity entity = messengerManager.find(BaseEntity.class, identity);
		if (entity == null) throw new NotFoundException();
		return (Person) entity;
	}
	
	@GET
	@Path("{identity}/messagesAuthored")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Set<Message> getMessagesAuthored (@PathParam("identity") final long identity) {
		return getPerson(identity).getMessagesAuthored();
	}
	
	@GET
	@Path("{identity}/peopleObserving")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Set<Person> getPeopleObserving (@PathParam("identity") final long identity) {
		return getPerson(identity).getPeopleObserving();
	}
	
	@GET
	@Path("{identity}/peopleObserved")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Set<Person> getPeopleObserved (@PathParam("identity") final long identity) {
		return getPerson(identity).getPeopleObserved();
	}
	
	@GET
	@Path("{identity}/avatar")
	@Produces({ MEDIA_TYPE_WILDCARD })
	public Document getAvatar (@PathParam("identity") final long identity) {
		return getPerson(identity).getAvatar();
	}
	
	@PUT
	@Path("{identity}/peopleObserved")
	public void updatePerson (@PathParam("identity") final long identity) {
		// TODO
	}
	
	@PUT
	@Path("{identity}/avatar")
	public void updateAvatar (@PathParam("identity") final long identity) {
		// TODO
	}
}
