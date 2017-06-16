package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.messenger.persistence.Person.Group;
import de.sb.toolbox.Copyright;
import de.sb.toolbox.net.RestCredentials;

@Path("people")
@Copyright(year=2013, holders="Sascha Baumeister")
public class PersonService {

	@GET
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public List<Person> getPeople (
			int offset, int limit,
			Long lowerCreationTimestamp, Long upperCreationTimestamp,
			Group group, String email,
			String givenName, String familyName,
			String city, String postCode, String street) {
		
		final EntityManager em = EntityService.getEntityManager();
		
		final String pql = "select p.identity from Person as p where "
		+"(:lowerCreationTimestamp is null or p.creationTimestamp >= :lowerCreationTimestamp)"
		+"and (:upperCreationTimestamp is null or p.creationTimestamp <= :upperCreationTimestamp)"
		+"and (:groupAlias is null or p.groupAlias = :groupAlias)"
		+"and (:email is null or p.email = :email)"
		+"and (:givenName is null or p.givenName = :givenName)"
		+"and (:familyName is null or p.familyName = :familyName)"
		+"and (:city is null or p.city = :city)"
		+"and (:postCode is null or p.postCode = :postCode)"
		+"and (:street is null or p.street = :street)"
		+" limit :limit offset :offset"
		;
	
		TypedQuery<Long> query = em.createQuery(pql, Long.class);
		query.setParameter("limit", limit);
		query.setParameter("offset", offset);
		query.setParameter("lowerCreationTimestamp", lowerCreationTimestamp);
		query.setParameter("upperCreationTimestamp", upperCreationTimestamp);
		query.setParameter("groupAlias", group);
		query.setParameter("email", email);
		query.setParameter("givenName", givenName);
		query.setParameter("familyName", familyName);
		query.setParameter("city", email);
		query.setParameter("postCode", email);
		query.setParameter("street", email);
		
		Long[] ids = query.getResultList().toArray(new Long[]{});
		Arrays.sort( ids ); // sort array
		
		List<Person> persons = Collections.emptyList();
		
		final EntityManagerFactory emf = em.getEntityManagerFactory();
		
		for (Long id : ids)
		{
			Person person = em.find(Person.class, id);
			if(person!=null) {
				persons.add(person);
				emf.getCache().evict(Person.class, person.getIdentiy());
			}
		};
		
		return persons;
	}
	
	@PUT
	public long updatePerson (final Person template, @HeaderParam("Set-Password") final String password) {
		final EntityManager em = EntityService.getEntityManager();
		
		boolean update = template.getIdentiy() != 0;
		Person person = null;
		
		if(!update){
			person = new Person(template.getEmail(), em.find(Document.class, 1)); // template.getAvatar().getIdentiy()
		} else {
			person = em.find(Person.class, template.getIdentiy());

			if (person == null)
				throw new ClientErrorException(NOT_FOUND);
		}			
			
		person.setEmail(template.getEmail());
		person.setGroup(template.getGroup());
		
		person.getName().setGiven(template.getName().getGiven());
		person.getName().setFamily(template.getName().getFamily());
		
		person.getAddress().setCity(template.getAddress().getCity());
		person.getAddress().setPostcode(template.getAddress().getPostcode());
		person.getAddress().setStreet(template.getAddress().getStreet());
		
		if(password.equals("") != true)
			person.setPasswordHash(Person.passwordHash(password));
		
		// insert / update
		EntityService.update(em, update ? person : null);
		
		// clear Cache
		em.getEntityManagerFactory().getCache().evict(Person.class, person.getIdentiy());

		return person.getIdentiy();
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
		final Person entity = EntityService.getEntityManager().find(Person.class, identity);
		if (entity == null) throw new NotFoundException();
		return entity;
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
	public Response getAvatar (@PathParam("identity") final long identity) {
		Document avatar = getPerson(identity).getAvatar();
		ResponseBuilder rb = Response.ok(avatar.getContent(), avatar.getContentType());
		return rb.build();	// return avatar und response type
	}
	
	@PUT
	@Path("{identity}/peopleObserved")
	public void updatePerson (@PathParam("identity") final long identity, long[] peopleObservedIdentities) {
		final EntityManager em = EntityService.getEntityManager();

		// remove dublicates
		Set<Long> plist = new HashSet<Long>();
		for(long id : peopleObservedIdentities){
			if(!plist.contains(id)) plist.add(id);
		}
		
		Person person = getPerson(identity);
		Set<Person> list = person.getPeopleObserved();
		
		for(long id : peopleObservedIdentities){
			Person p = em.find(Person.class, id);
			if(list.contains(id)) list.remove(p); // remove if id is in list
			else list.add(p); // add if id is not in list
		}

		EntityService.update(em,null);
		
		// clear Cache
		em.getEntityManagerFactory().getCache().evict(Person.class, person.getIdentiy());
	}
	
	@PUT
	@Path("{identity}/avatar")
	@Consumes(MEDIA_TYPE_WILDCARD)
	public long updateAvatar (@HeaderParam("Authorization") final String authentication, @HeaderParam("Content-type") String mediaType, byte[] content) {
		Person owner = PersonService.getRequester(authentication);
		final EntityManager em = EntityService.getEntityManager();

		boolean insert = true;
		long id  = 1; // default value
		
		if(! (content == null || content.length==0) ) {
			byte[] contentHash = Document.mediaHash(content);
			
			final String pql = "select d.identity from Document as d where d.contentHash = :contentHash";
			TypedQuery<Long> query = em.createQuery(pql, Long.class);
			query.setParameter("contentHash", contentHash);
			id = query.getSingleResult();
		}
		
		Document avatar = em.find(Document.class, id);

		if(avatar==null) {
			// content can be empty!
			avatar = new Document(mediaType, content);
			insert=true;
		}
		
		owner.setAvatar(avatar);
		
		EntityService.update(em,insert?avatar:null,null);
		
		// clear Cache
		em.getEntityManagerFactory().getCache().evict(Document.class, avatar.getIdentiy());
		
		// return new avatar id
		return avatar.getIdentiy();
	}
}
