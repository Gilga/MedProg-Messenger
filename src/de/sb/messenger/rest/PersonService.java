package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
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

@Path("people")
@Copyright(year=2013, holders="Sascha Baumeister")
public class PersonService {

	static EntityManagerFactory messengerManagerFactory = null;
	
	static private EntityManagerFactory getEntityManagerFactory() {
		if(messengerManagerFactory==null) {
			final EntityManager em = EntityService.getEntityManager();
			messengerManagerFactory=em.getEntityManagerFactory();
		}
		return messengerManagerFactory;
	}
	
	@GET
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public List<Person> getPeople (List<Predicate> criteria) {
		final EntityManager em = EntityService.getEntityManager();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> cq = cb.createQuery(Person.class);
		//Root<Person> e = cq.from(Person.class);
		//CriteriaQuery<Person> all = cq.select(e);
		if(criteria.size() > 0) cq.where(criteria.toArray(new Predicate[]{}));

		return em.createQuery(cq).getResultList();
	}
	
	@SuppressWarnings("unused")
	@PUT
	public long createPerson (final Person template, @HeaderParam("Set-Password") final String password) {
		final EntityManager em = EntityService.getEntityManager();
		final EntityManagerFactory emf = getEntityManagerFactory();
		
		boolean update = false;
		Person person = null;
		long identity = template.getIdentiy();
		
		if(identity == 0) person = new Person(template.getEmail(), template.getAvatar());
		else {
			person = getPerson(identity);
			
			// clear Cache
			emf.getCache().evict(Person.class, person.getIdentiy());
			
			person.setAvatar(template.getAvatar());
			person.setEmail(template.getEmail());
			person.setGroup(template.getGroup());
			update = true;
		}
		
		if (person == null)
			throw new ClientErrorException(NOT_FOUND);
		
		if(password.equals("") != true)
			person.setPasswordHash(Person.passwordHash(password));
		
		if(!update) em.persist(person);
		else {
			em.merge(person);
		}
		
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
		final BaseEntity entity = EntityService.getEntityManager().find(BaseEntity.class, identity);
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
	
	// TODO
	@PUT
	@Path("{identity}/peopleObserved")
	public void updatePerson (@PathParam("identity") final long identity, Set<Long> peopleObservedIdentities) {
		final EntityManager em = EntityService.getEntityManager();
		final EntityManagerFactory emf = getEntityManagerFactory();
		
		Person person = getPerson(identity);

		// clear Cache
		emf.getCache().evict(Person.class, person.getIdentiy());
		
		// ...
		em.merge(person);
	}
	
	@PUT
	@Path("{identity}/avatar")
	@Consumes(MEDIA_TYPE_WILDCARD)
	public void updateAvatar (@HeaderParam("Authorization") final String authentication, @PathParam("identity") final long identity, @HeaderParam("Content-type") String mediaType, byte[] content) {
		Person owner = PersonService.getRequester(authentication);
		final EntityManager em = EntityService.getEntityManager();
		final EntityManagerFactory emf = getEntityManagerFactory();
		//HTTP request body, 

		Document avatar;
		if(content == null || content.length==0 ){
			avatar = em.find(Document.class, 1);
			
			// clear Cache
			emf.getCache().evict(Document.class, 1);
			
			if (avatar == null) throw new NotFoundException();
		}
		else {
			 //@Context HttpHeaders hh
			//hh.getHeaderString("Content-type")
			avatar = new Document(mediaType,content);
		}
		
		owner.setAvatar(avatar);
		em.merge(owner);
	}
}
