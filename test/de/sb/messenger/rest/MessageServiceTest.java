package de.sb.messenger.rest;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

public class MessageServiceTest extends ServiceTest {

	Client client;
	static private final URI SERVICE_URI = URI.create("http://localhost:8001/e");
	String usernameAndPassword;
	String authorizationHeaderName;
	String authorizationHeaderValue;

	@Before
	public void setupBefore() {
		client = ClientBuilder.newClient();
		String username = "John";
		String password = "Smith";
		usernameAndPassword = username + ":" + password;
		authorizationHeaderName = "Authorization";
		authorizationHeaderValue = "Basic"
				+ java.util.Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
		client = ClientBuilder.newClient();
	}

	@Test
	public void testCriteriaQueries(){
		//Response res = client.target(SERVICE_URI + "/messages").request().header(authorizationHeaderName, authorizationHeaderValue).
	}

	@Test
	public void testIdentityQueries() {

	}

	@Test
	public void testBidRelations() {

	}

	// links
	// https://dennis-xlc.gitbooks.io/restful-java-with-jax-rs-2-0-2rd-edition/en/part1/chapter8/client_and_web_target.html
	// authorization -
	// http://www.developerscrappad.com/2364/java/java-ee/rest-jax-rs/how-to-perform-http-basic-access-authentication-with-jax-rs-rest-client/
}
