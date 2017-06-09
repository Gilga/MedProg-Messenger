package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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

import com.mysql.fabric.Response;

import de.sb.messenger.persistence.Address;
import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Name;
import de.sb.messenger.persistence.Person;
import de.sb.messenger.persistence.Person.Group;
import de.sb.toolbox.Copyright;
import de.sb.toolbox.net.RestCredentials;

@Path("people")
@Copyright(year=2013, holders="Sascha Baumeister")
public class PersonService {

	static EntityManagerFactory messengerManagerFactory = null;
	
	static private EntityManagerFactory getEntityManagerFactory(EntityManager em) {
		if(messengerManagerFactory==null) messengerManagerFactory=em.getEntityManagerFactory();
		return messengerManagerFactory;
	}
	
	@GET
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	// benutze den vornamen und die unterelemente von addresse
	public List<Person> getPeople (Long lowerCreationTimestamp, Long upperCreationTimestamp,
			String email, Name name, Address address, Group group,int offset, int resultLength) { //Long avatarID
		// search for passwordHash, author, peopleObserved, messenger?

		final EntityManager em = EntityService.getEntityManager();

		//Document avatar = avatarID == null ? null : em.find(Document.class, avatarID);
		// optional pql query, angabe von offset und laenge fehlt
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> cq = cb.createQuery(Person.class);
		Root<Person> e = cq.from(Person.class);

		// Constructing list of parameters
		List<Predicate> predicates = new ArrayList<Predicate>();

		// Adding predicates
		Predicate p1 = cb.or(cb.isNull(cb.literal(lowerCreationTimestamp)), cb.greaterThanOrEqualTo(e.get("identity"), lowerCreationTimestamp));
		Predicate p2 = cb.or(cb.isNull(cb.literal(upperCreationTimestamp)), cb.lessThanOrEqualTo(e.get("identity"), upperCreationTimestamp));
		Predicate p3 = cb.or(cb.isNull(cb.literal(email)), cb.equal(e.get("email"), email));
		Predicate p4 = cb.or(cb.isNull(cb.literal(name)), cb.equal(e.get("name"), name));
		Predicate p5 = cb.or(cb.isNull(cb.literal(address)), cb.equal(e.get("address"), address));
		Predicate p6 = cb.or(cb.isNull(cb.literal(group)), cb.equal(e.get("groupAlias"), group));
//		Predicate p7 = cb.or(cb.isNull(cb.literal(avatar)), cb.equal(e.get("avatarReference"), avatar));

		predicates.add(cb.and(p1, p2, p3, p4, p5, p6));

		cq.select(e).where(predicates.toArray(new Predicate[] {}));

		// schleife ueber jede id, use second level cache, listen ordnung ist nicht garantiert, sortieren anch einem belieben kriterium
		// beim iterieren ein array generieren und die objekte sortiert mit der id hinzufuegen\
		// bei jedem listen objekt alles sortieren und ein array generieren
		return em.createQuery(cq).getResultList();
	}
	
	@PUT
	public long updatePerson (final Person template, @HeaderParam("Set-Password") final String password) {
		final EntityManager em = EntityService.getEntityManager();
		final EntityManagerFactory emf = getEntityManagerFactory(em);
		
		boolean update = template.getIdentiy() != 0;
		Person person = null;
		
		// avatar ueber die default id aus der datenbank holem em.find(Typ, ID)
		if(!update){
			person = new Person(template.getEmail(), template.getAvatar());
		} else {
			person = em.find(Person.class, template.getIdentiy());

			if (person == null)
				throw new ClientErrorException(NOT_FOUND);
		}			
			
		person.setEmail(template.getEmail());
		person.setGroup(template.getGroup());
		
		person.getName().setGiven(template.getName().getGiven());
		
		if(update) {
			em.flush();
		} else {
			em.persist(person);
		}
		
		em.getTransaction().commit();
		// clear Cache
		emf.getCache().evict(Person.class, person.getIdentiy());
		
		if(password.equals("") != true)
			person.setPasswordHash(Person.passwordHash(password));
		
		EntityService.update(em,!update ? person : null);

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
		// return avatar und response type
		return getPerson(identity).getAvatar();
	}
	
	// TODO
	@PUT
	@Path("{identity}/peopleObserved")
	public void updatePerson (@PathParam("identity") final long identity, long[] peopleObservedIdentities) {
		
		final EntityManager em = EntityService.getEntityManager();
		final EntityManagerFactory emf = getEntityManagerFactory(em);
		
		Person person = getPerson(identity);

		// clear Cache
		emf.getCache().evict(Person.class, person.getIdentiy());
		
		// ...
		
		EntityService.update(em,null);
	}
	
	@PUT
	@Path("{identity}/avatar")
	@Consumes(MEDIA_TYPE_WILDCARD)
	public void updateAvatar (@HeaderParam("Authorization") final String authentication, @HeaderParam("Content-type") String mediaType, byte[] content) {
		Person owner = PersonService.getRequester(authentication);
		final EntityManager em = EntityService.getEntityManager();
		final EntityManagerFactory emf = getEntityManagerFactory(em);
		//HTTP request body, 

		// ist das medium schon vorhanden, contenhash berechnen, query in document, document mit hash vorhanden id zurueckgeben ! daten verbinden: committen
		// wenn nicht vorhanden ein neues document erstellen in die datenbank committen, dann die relation zur person aendern und das committen
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
		
		// id des documents returnen !
		EntityService.update(em,null);
	}
}
