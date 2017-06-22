package de.sb.messenger.test.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.net.URI;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Person;
import de.sb.messenger.persistence.Person.Group;

public class PersonServiceTest extends ServiceTest {
	
	
	public String username;
	public String usernameAndPassword;
	public String authorizationHeaderName;
	public String authorizationHeaderValue;
	public String authorizationHeaderInvalidValue;

	public EntityManager entityManager;
	protected static EntityManagerFactory EM_FACTORY;
	WebTarget userTargetValidInes;
	WebTarget userTargetInvalid ;
	WebTarget userTargetValid;
	WebTarget userTargetInvalidUser;

	@Before
	public void setupBefore() {
		
		username = "ines";
		String password = "ines";
		usernameAndPassword = username + ":" + password;
		authorizationHeaderName = "Authorization";
		authorizationHeaderValue = "Basic"
				+ java.util.Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
		EM_FACTORY = Persistence.createEntityManagerFactory("messenger");
		entityManager = EM_FACTORY.createEntityManager();

	}

	@Test
	public void testCriteriaQueries() {
		// target correct? ines plain text or encoded?

	}

	@Test
	public void testIdentityQueries() {
		 userTargetValidInes = this.newWebTarget("ines", "ines").path("people/1");
		 userTargetInvalid = this.newWebTarget("ines", "password").path("people");
		 userTargetValid = this.newWebTarget("ines", "ines").path("people");
		 userTargetInvalidUser = this.newWebTarget("ines", "ines").path("/people/20");
		
		Response res = userTargetValid.request()
				.header(authorizationHeaderName, authorizationHeaderValue).get();
		// correct authentication.no exception
		assertTrue(res.getStatus() == 200);

		res = userTargetInvalid.request().header(authorizationHeaderName, authorizationHeaderInvalidValue).get();
		// 401 Unauthorized
		assertTrue(res.getStatus() == 401);

		res = userTargetValidInes.request().header(authorizationHeaderName, authorizationHeaderValue).get();
		// valid. keine exception
		assertTrue(res.getStatus() == 200);

		res = userTargetInvalidUser.request()
				.header(authorizationHeaderName, authorizationHeaderValue).get();
		// ClientErrorException 404keine passende Entitaet
		assertTrue(res.getStatus() == 404);

		res = userTargetValidInes.request().header(authorizationHeaderName, authorizationHeaderValue)
				.accept(APPLICATION_JSON).get();
		// return media type APPLICATION_JSON
		assertEquals(APPLICATION_JSON_TYPE, res.getMediaType());

		res = userTargetValidInes.request().header(authorizationHeaderName, authorizationHeaderValue)
				.accept(APPLICATION_XML).get();
		// returns media type APPLICATION_XML_TYPE because APPLICATION_XMLis
		// simply a string
		assertEquals(APPLICATION_XML_TYPE, res.getMediaType());
		//this.getWasteBasket().add(userTargetIvalidUser.get)
	}

	@Test
	public void ObserverRelationQueries() {
		
	}

	@Test
	public void testLifecycle() {
		String s = "some content";
		byte[] content = s.getBytes();
		Document doc = new Document("image/jpeg", content);
		// create entity
		Person person = new Person("test@gmail.com", doc);
		person.getName().setGiven("John");
		person.getName().setFamily("Smith");
		person.getAddress().setStreet("Falkenbergerstr. 1");
		person.getAddress().setPostcode("12345");
		person.getAddress().setCity("Berlin");
		person.setGroup(Group.USER);
		byte[] hash = person.passwordHash("password");
		person.setPasswordHash(hash);

		// add to the DB

		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.persist(doc);
		transaction.commit();
		transaction.begin();
		entityManager.persist(person);
		transaction.commit();
		this.getWasteBasket().add(doc.getIdentiy());
		this.getWasteBasket().add(person.getIdentiy());

		transaction.begin();
		person = entityManager.find(Person.class, person.getIdentiy());

		
		// put a person
		Response res =  userTargetValid.request()
				.header(authorizationHeaderName, authorizationHeaderValue).header("Set-password", "Password")
				.put(Entity.json(person));
		// successful put
		assertTrue(res.getStatus() == 200);
		//
		Long resIdentity = res.readEntity(Long.class);
		// assertEquals(???,resIdentity)
		
		WebTarget userTargetPut = this.newWebTarget("john", "john").path("people/"+resIdentity);
		
		//get person 
		Person personPut = userTargetPut.request()
				.header(authorizationHeaderName, authorizationHeaderValue).get(Person.class);
		assertEquals(person.getIdentiy(),personPut.getIdentiy());
		
		
		
	}
	
	//https://dennis-xlc.gitbooks.io/restful-java-with-jax-rs-2-0-2rd-edition/en/part1/chapter8/building_and_invoking_requests.html
}
