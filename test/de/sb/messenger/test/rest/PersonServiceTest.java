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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import de.sb.messenger.persistence.Person;

public class PersonServiceTest extends ServiceTest {
	Client client;
	static private final URI SERVICE_URI = URI.create("http://localhost:8001/services");
	String username;
	String usernameAndPassword;
	String authorizationHeaderName;
	String authorizationHeaderValue;
	String authorizationHeaderInvalidValue;

	@Before
	public void setupBefore() {
		client = ClientBuilder.newClient();
		username = "ines";
		String password = "ines";
		usernameAndPassword = username + ":" + password;
		authorizationHeaderName = "Authorization";
		authorizationHeaderValue = "Basic"
				+ java.util.Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
		client = ClientBuilder.newClient();
	}

	@Test
	public void testCriteriaQueries() {
		// target correct? ines plain text or encoded?


	}

	@Test
	public void testIdentityQueries() {

		Response response = client.target(SERVICE_URI + "/people").request()
				.header(authorizationHeaderName, authorizationHeaderValue).get();
		// correct authentication.no exception
		assertEquals(200, response.getStatus());
		 
		response = client.target(SERVICE_URI + "/people").request()
				.header(authorizationHeaderName,authorizationHeaderInvalidValue).get();
		//401 Unauthorized 
		assertEquals(401, response.getStatus());

		response = client.target(SERVICE_URI + "/people/" + 1).request()
				.header(authorizationHeaderName, authorizationHeaderValue).get();
		//valid. keine exception
		assertEquals(200, response.getStatus());

		response = client.target(SERVICE_URI + "/people/" + 20).request()
				.header(authorizationHeaderName, authorizationHeaderValue).get();
		//ClientErrorException 404keine passende Entitaet
		assertEquals(404, response.getStatus());

		
		response = client.target(SERVICE_URI + "/people/" + 1).request()
				.header(authorizationHeaderName, authorizationHeaderValue).accept(APPLICATION_JSON).get();
		//return media type APPLICATION_JSON
		assertEquals(APPLICATION_JSON_TYPE, response.getMediaType());

		response = client.target(SERVICE_URI + "/people/" + 1).request()
				.header(authorizationHeaderName, authorizationHeaderValue).accept(APPLICATION_XML).get();
		//returns media type APPLICATION_XML_TYPE because APPLICATION_XMLis simply a string
		assertEquals(APPLICATION_XML_TYPE, response.getMediaType());
	}
}
