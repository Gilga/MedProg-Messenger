package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
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
	public List<Person> getPeople (final String[] criteria) {
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		CriteriaQuery<Person> cq = messengerManager.getCriteriaBuilder().createQuery(Person.class);
		Root<Person> p = cq.from(Person.class);
		
		Metamodel m = messengerManager.getMetamodel();
		EntityType<Person> Person_ = m.entity(Person.class);
		//cq.where(p.get(Person_.getName()).isNull());
		// TODO
		
		List<Person> people = messengerManager.createQuery(cq).getResultList();

		return people;
	}
	
	@PUT
	public long createsPerson (final Person template, @HeaderParam("Set-Password") final String password) {
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
	
		Person person = null;
		final long identity = template.getIdentiy();
		
		if(identity == 0) {
			person = new Person(template.getEmail(), template.getAvatar());
			messengerManager.getEntityManagerFactory().getCache().evict(Person.class, person.getIdentiy());
		}
		else {
			person = getPerson(identity);
			person.setAvatar(template.getAvatar());
			person.setEmail(template.getEmail());
			//person.setGroup(template.getGroup());
		}
		
		if (person == null) throw new ClientErrorException(NOT_FOUND);
		
		if(password.equals("") != true) {
			person.setPasswordHash(Person.passwordHash(password));
		}
		
		messengerManager.persist(person); // update?
		
		return identity;
	}
	
	@GET
	@Path("requester")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public static Person getRequester (@HeaderParam("Authorization") final String authentication) {
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
	public void updatePerson (@PathParam("identity") final long identity, Set<Long> peopleObservedIdentities) {
		// TODO
		//messengerManager.getEntityManagerFactory().getCache().evict(Person.class, author.getIdentiy());
	}
	
	@PUT
	@Path("{identity}/avatar")
	@Consumes(MEDIA_TYPE_WILDCARD)
	public void updateAvatar (@HeaderParam("Authorization") final String authentication, @PathParam("identity") final long identity, String mediaType, byte[] content) {
		Person owner = PersonService.getRequester(authentication);
		// TODO
	}
}
