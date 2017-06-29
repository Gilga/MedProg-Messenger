package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Message;
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
	WebTarget userTargetInvalid;
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
		WebTarget webTarget = newWebTarget("root", "root").path("people");

		/*
		 * Test GET queryParam
		 */
		List<Person> people;

		final int offset = 10, limit = 60;
		Response response = webTarget.queryParam("offset", offset).queryParam("limit", limit).request()
				.accept(APPLICATION_JSON).get();

		people = response.readEntity(new GenericType<List<Person>>() {
		});

		for (Person p : people) {
			assertTrue(offset <= p.getVersion());
			assertTrue(limit >= p.getVersion());
		}

		assertTrue(response.getStatus() == 200);

		/*
		 * Test timeStamp
		 */
		final long lowerCreationTimestamp = 0, upperCreationTimestamp = 50;
		response = webTarget.queryParam("lowerCreationTimestamp", lowerCreationTimestamp)
				.queryParam("upperCreationTimestamp", upperCreationTimestamp).request().accept(APPLICATION_JSON).get();

		people = response.readEntity(new GenericType<List<Person>>() {
		});

		for (Person p : people) {
			assertTrue(lowerCreationTimestamp <= p.getCreationTimestamp());
			assertTrue(upperCreationTimestamp >= p.getCreationTimestamp());
		}
		assertTrue(response.getStatus() == 200);
		/*
		 * Test givenName
		 */
		response = webTarget.queryParam("giveName", person.getName().getGiven()).request().accept(APPLICATION_JSON)
				.get();

		people = response.readEntity(new GenericType<List<Person>>() {
		});

		assertEquals("John", people.get(0).getName().getGiven());
		assertTrue(response.getStatus() == 200);

	}

	/*
	 * Test authentification and
	 */
	@Test
	public void testIdentityQueries() {
		userTargetValidInes = newWebTarget("ines", "ines").path("people/1");
		userTargetInvalid = newWebTarget("ines", "password").path("people");
		userTargetValid = newWebTarget("ines", "ines").path("people");
		userTargetInvalidUser = newWebTarget("ines", "ines").path("/people/20");

		/*
		 * Test getRequester
		 */
		Response res = userTargetValid.request().header(authorizationHeaderName, authorizationHeaderValue).get();
		// correct authentication.no exception
		assertTrue(res.getStatus() == 200);

		res = userTargetInvalid.request().header(authorizationHeaderName, authorizationHeaderInvalidValue).get();
		// 401 Unauthorized
		assertTrue(res.getStatus() == 401);

		res = userTargetValidInes.request().header(authorizationHeaderName, authorizationHeaderValue).get();
		// valid. keine exception
		assertTrue(res.getStatus() == 200);

		res = userTargetInvalidUser.request().header(authorizationHeaderName, authorizationHeaderValue).get();
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
		// .getWasteBasket().add(userTargetIvalidUser.ge)
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

		WebTarget webTarget = newWebTarget("root", "root").path("people");
		/*
		 * test PUT update person/create person
		 */
		Response response = webTarget.request().accept(APPLICATION_JSON).header("Set-password", "password")
				.put(Entity.json(person));

		long idPerson = response.readEntity(Long.class);

		assertNotEquals(0, idPerson);
		assertTrue(response.getStatus() == 200);

		
		// TODO authenticated requester

		/*
		 * Test getPerson
		 */
		WebTarget webTargetInes = this.newWebTarget("ines", "ines").path("people/2");
		response = webTargetInes.request().accept(APPLICATION_JSON).get();
		Person returnedPerson = response.readEntity(Person.class);
		assertTrue(response.getStatus() == 200);
		assertEquals(2L, returnedPerson.getIdentiy());

		/*
		 * Test getMessagesAuthored
		 */
		
		List<Message> msgs;		
		WebTarget webTargetInesMsgs = this.newWebTarget("ines", "ines").path("people/2/messagesAuthored");
		response = webTargetInesMsgs.request().accept(APPLICATION_JSON).get();		
		msgs = response.readEntity(new GenericType<List<Message>>() {});	
		assertNotEquals(0, msgs.size());
		assertTrue(response.getStatus() == 200);
	
		/*
		 * Test peopleObserving
		 */
		WebTarget webTargetInesPeopleObserving = this.newWebTarget("ines", "ines").path("people/2/peopleObserving");
		List<Person>peopleObserving;		
		response = webTargetInesPeopleObserving.request().accept(APPLICATION_JSON).get();		
		peopleObserving = response.readEntity(new GenericType<List<Person>>() {});
		
		assertEquals(6, peopleObserving.size());
		assertTrue(response.getStatus() == 200);
		assertEquals(3L, peopleObserving.get(0).getIdentiy()); 
		assertEquals(4L, peopleObserving.get(0).getIdentiy());
		assertEquals(5L, peopleObserving.get(0).getIdentiy());
		assertEquals(6L, peopleObserving.get(0).getIdentiy());
		
		/*
		 * Test peopleObserved
		 */
		WebTarget webTargetInesPeopleObserved = this.newWebTarget("ines", "ines").path("people/2/peopleObserved");
		List<Person>peopleObserved;		
		response = webTargetInesPeopleObserved.request().accept(APPLICATION_JSON).get();		
		peopleObserved = response.readEntity(new GenericType<List<Person>>() {});
		
		assertEquals(6, peopleObserved.size());
		assertTrue(response.getStatus() == 200);
		assertEquals(4L, peopleObserved.get(0).getIdentiy()); 
		
		/*
		 * Test put updatePerson peopleObserved
		 */
		//TODO is it correct?
		WebTarget webTargetInesPutPeopleObserved = this.newWebTarget("ines", "ines").path("people/2/peopleObserved");	
		response = webTargetInesPutPeopleObserved.request().accept(APPLICATION_JSON).put(Entity.json(person));		
		peopleObserved = response.readEntity(new GenericType<List<Person>>() {});
		Person testP = null;
		for (Person p : peopleObserved) {
			if(p.getName().getFamily() == "Smith"){
				testP = p;
			}
		}
		
		assertEquals(7, peopleObserved.size());
		assertTrue(testP.getName().getGiven() == "John");
		assertTrue(response.getStatus() == 200);
		
		

		
		this.getWasteBasket().add(idPerson);
	}

	// https://dennis-xlc.gitbooks.io/restful-java-with-jax-rs-2-0-2rd-edition/en/part1/chapter8/building_and_invoking_requests.html
	// https://stackoverflow.com/questions/27211012/how-to-send-json-object-from-rest-client-using-javax-ws-rs-client-webtarget
}
