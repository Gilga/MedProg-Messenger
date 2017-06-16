package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
	public Person[] getPeople (
			@QueryParam("offset") int offset,
			@QueryParam("limit") int limit,
			@QueryParam("lowerCreationTimestamp") Long lowerCreationTimestamp,
			@QueryParam("upperCreationTimestamp") Long upperCreationTimestamp,
			@QueryParam("group") Group group,
			@QueryParam("email") String email,
			@QueryParam("givenName") String givenName,
			@QueryParam("familyName") String familyName,
			@QueryParam("city") String city,
			@QueryParam("postCode") String postCode,
			@QueryParam("street") String street) {
		
		final EntityManager em = EntityService.getEntityManager();
		
		final String pql = "select p.identity from Person as p where"
		+" (:lowerCreationTimestamp is null or p.creationTimestamp >= :lowerCreationTimestamp)"
		+" and (:upperCreationTimestamp is null or p.creationTimestamp <= :upperCreationTimestamp)"
		+" and (:groupAlias is null or p.groupAlias = :groupAlias)"
		+" and (:email is null or p.email = :email)"
		+" and (:givenName is null or p.name.given = :givenName)"
		+" and (:familyName is null or p.name.family = :familyName)"
		+" and (:city is null or p.address.city = :city)"
		+" and (:postCode is null or p.address.postCode = :postCode)"
		+" and (:street is null or p.address.street = :street)"
		;
	
		TypedQuery<Long> query = em.createQuery(pql, Long.class);
		
		if(offset>0) query.setFirstResult(offset);
		if(limit>0) query.setMaxResults(limit);
		
		query.setParameter("lowerCreationTimestamp", lowerCreationTimestamp);
		query.setParameter("upperCreationTimestamp", upperCreationTimestamp);
		query.setParameter("groupAlias", group);
		query.setParameter("email", email);
		query.setParameter("givenName", givenName);
		query.setParameter("familyName", familyName);
		query.setParameter("city", city);
		query.setParameter("postCode", postCode);
		query.setParameter("street", street);
		
		List<Long> ids = query.getResultList();
		List<Person> people = new ArrayList<>();
		
		for (Long id : ids)
		{
			Person person = em.find(Person.class, id);
			
			if (person == null)	throw new ClientErrorException(NOT_FOUND);
			
			people.add(person);
		};
		
		Person[] result = people.toArray(new Person[]{});
		Arrays.sort( result ); // sort array
		
		return result;
	}
	
	@PUT
	@Consumes({ APPLICATION_JSON, APPLICATION_XML })
	@Produces({ TEXT_PLAIN })
	public long updatePerson (final Person template, @HeaderParam("Set-Password") final String password) {
		final EntityManager em = EntityService.getEntityManager();
		
		boolean update = template.getIdentiy() != 0;
		Person person = null;
		
		if(!update){
			person = new Person(template.getEmail(), em.find(Document.class, 1));
		} else {
			person = em.find(Person.class, template.getIdentiy());

			if (person == null)
				throw new ClientErrorException(NOT_FOUND);
		}			
			
		person.setEmail(template.getEmail());
		
		// only admin can change his group
		if(person.getGroup() == Group.ADMIN) person.setGroup(template.getGroup());
		
		person.getName().setGiven(template.getName().getGiven());
		person.getName().setFamily(template.getName().getFamily());
		
		person.getAddress().setCity(template.getAddress().getCity());
		person.getAddress().setPostcode(template.getAddress().getPostcode());
		person.getAddress().setStreet(template.getAddress().getStreet());
		
		if(password != null && !password.equals(""))
			person.setPasswordHash(Person.passwordHash(password));
		
		// insert / update
		EntityService.update(em, person);
		
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
		if (entity == null) throw new ClientErrorException(NOT_FOUND);
		return entity;
	}
	
	@GET
	@Path("{identity}/messagesAuthored")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Message[] getMessagesAuthored (@PathParam("identity") final long identity) {

		Message[] result = getPerson(identity).getMessagesAuthored().toArray(new Message[]{});
		Arrays.sort( result ); // sort array
		
		return result;
	}
	
	@GET
	@Path("{identity}/peopleObserving")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person[] getPeopleObserving (@PathParam("identity") final long identity) {

		Person[] result = getPerson(identity).getPeopleObserving().toArray(new Person[]{});
		Arrays.sort( result ); // sort array
		
		return result;
	}
	
	@GET
	@Path("{identity}/peopleObserved")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person[] getPeopleObserved (@PathParam("identity") final long identity) {
		
		Person[] result = getPerson(identity).getPeopleObserved().toArray(new Person[]{});
		Arrays.sort( result ); // sort array
		
		return result;
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
		if(peopleObservedIdentities == null) throw new ClientErrorException(BAD_REQUEST);
		
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
		
		// what if peopleObservedIdentities.size() == 0?

		EntityService.update(em,person);
		
		// clear Cache
		em.getEntityManagerFactory().getCache().evict(Person.class, person.getIdentiy());
	}
	
	@PUT
	@Path("{identity}/avatar")
	@Consumes(MEDIA_TYPE_WILDCARD)
	@Produces({ TEXT_PLAIN })
	public long updateAvatar (@HeaderParam("Authorization") final String authentication, @HeaderParam("Content-type") String mediaType, byte[] content) {
		if(content == null || content.length==0) throw new ClientErrorException(BAD_REQUEST);
		
		Person owner = PersonService.getRequester(authentication);
		final EntityManager em = EntityService.getEntityManager();
		
		byte[] contentHash = Document.mediaHash(content);
		
		final String pql = "select d.identity from Document as d where d.contentHash = :contentHash";
		TypedQuery<Long> query = em.createQuery(pql, Long.class);
		query.setParameter("contentHash", contentHash);
		List<Long> ids = query.getResultList();
		
		Document avatar;
		
		if(ids.isEmpty()){ // insert
			avatar = new Document(mediaType, content);
		}
		else if(ids.size() == 1){ // update
			avatar = em.find(Document.class, ids.get(0));
			if(avatar == null) throw new ClientErrorException(NOT_FOUND);
		}
		else {
			throw new ClientErrorException(BAD_REQUEST); //NOT_IMPLEMENTED or METHOD_NOT_ALLOWED?
		}

		owner.setAvatar(avatar);
		
		EntityService.update(em, avatar, owner);
		
		// clear Cache
		// nicht notwendig weil relation unidirectional
		// em.getEntityManagerFactory().getCache().evict(Document.class, avatar.getIdentiy());
		
		// return new avatar id
		return avatar.getIdentiy();
	}
}
