package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import de.sb.messenger.persistence.BaseEntity;
import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.messenger.persistence.Person.Group;

public class MessageServiceTest extends ServiceTest {

	Client client;
	static private final URI SERVICE_URI = URI.create("http://localhost:8001/e");
	String usernameAndPassword;
	String authorizationHeaderName;
	String authorizationHeaderValue;
	WebTarget webTarget;
	Person returnedPerson;
	Message message;
	

	@Before
	public void setupBefore() {
		client = ClientBuilder.newClient();
		String username = "ines";
		String password = "ines";
		usernameAndPassword = username + ":" + password;
		authorizationHeaderName = "Authorization";
		authorizationHeaderValue = "Basic"
				+ java.util.Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
		client = ClientBuilder.newClient();			
				newWebTarget("ines", "ines");
	}

	@Test
	public void testCriteriaQueries(){
	
		Response res = webTarget.path("people/2").request().accept(APPLICATION_JSON).get();
		returnedPerson = res.readEntity(Person.class);
		
		BaseEntity baseEntity = new BaseEntity();
		message = new Message(returnedPerson, baseEntity, "Hi there!");
		 
		Response response = webTarget.request().accept(TEXT_PLAIN).header("Authorization", "authorization")
				.put(Entity.json(message));
	}

	@Test
	public void testIdentityQueries() {
		/*
		 * Test getMessage 
		 */		
		Response response =  webTarget.path("messages/id?????").request().accept(APPLICATION_JSON).get();
		Message returnedMsg = response.readEntity(Message.class);
		//TODO how to get the msg ID
		
		assertNotNull(returnedMsg);
		assertTrue(response.getStatus() == 200);
		assertEquals(APPLICATION_JSON_TYPE, response.getMediaType());
		
		/*
		 * Test getAuthor
		 */
		
		response =  webTarget.path("messages/id?????/author").request().accept(APPLICATION_JSON).get();
		returnedMsg = response.readEntity(Message.class);
		
		assertNotNull(returnedMsg);
		assertEquals("Ines",returnedMsg.getAuthor().getName().getGiven()) ;
		assertTrue(response.getStatus() == 200);
		assertEquals(APPLICATION_JSON_TYPE, response.getMediaType());
		
		/*
		 * Test getSubject
		 */
		response =  webTarget.path("messages/id?????/subject").request().accept(APPLICATION_JSON).get();
		returnedMsg = response.readEntity(Message.class);
		
		assertNotNull(returnedMsg);
		assertEquals(message.getSubject(),returnedMsg.getSubject()) ;
		assertTrue(response.getStatus() == 200);
		assertEquals(APPLICATION_JSON_TYPE, response.getMediaType());
		
		this.getWasteBasket().add(message.getIdentiy());
		
	}

	

	// links
	// https://dennis-xlc.gitbooks.io/restful-java-with-jax-rs-2-0-2rd-edition/en/part1/chapter8/client_and_web_target.html
	// authorization -
	// http://www.developerscrappad.com/2364/java/java-ee/rest-jax-rs/how-to-perform-http-basic-access-authentication-with-jax-rs-rest-client/
}
