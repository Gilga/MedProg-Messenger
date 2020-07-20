package de.sb.messenger.rest;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.EXPECTATION_FAILED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.util.Arrays;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.sb.messenger.persistence.Person;
import de.sb.toolbox.net.HttpCredentials;

/**
 * Facade interface for HTTP authentication purposes.
 */
public interface Authenticator {
	
	static final Logger LOGGER = LogManager.getLogger(Authenticator.class);
	
	/**
	 * Returns the authenticated requester (a person) for the given HTTP Basic authentication credentials.
	 * A SHA-256 hash-code is calculated for the contained password, and uses it in conjunction with the
	 * user email to query and return a suitable Person entity from the database.
	 * @param credentials the HTTP Basic authentication credentials
	 * @return the authenticated requestor
	 * @throws NotAuthorizedException (HTTP 401) if the given credentials are invalid
	 * @throws PersistenceException (HTTP 500) if there is a problem with the persistence layer
	 * @throws IllegalStateException (HTTP 500) if the entity manager associated with the current
	 *         thread is not open
	 * @throws NullPointerException (HTTP 500) if the given credentials are {@code null}
	 */
	static public Person authenticate (final HttpCredentials.Basic credentials) {
		
		try {
			final String pql = "select p from Person as p where p.email = :email"; // and p.password = :password
	
			String email = credentials.getUsername(); // username == email?
			byte[] passwordHash = Person.passwordHash(credentials.getPassword());
			
			EntityManager em  = EntityService.getEntityManager();
			
			Person person = em.createQuery(pql, Person.class)
					.setParameter("email", email)
					//sql passowrd check: pql+=" and p.password = :password"; query.setParameter("password", passwordHash)
					.getSingleResult();
			
			//if(person != null && !Arrays.equals(person.getPasswordHash(),passwordHash)){ person = null; }
			if(person == null) throw new ClientErrorException(UNAUTHORIZED); // HTTP 401, new NotAuthorizedException("Basic");
			
			return person;
			
		} catch(NotFoundException e){
			LOGGER.error("authenticate",e);
			throw new ClientErrorException(NOT_FOUND); // HTTP 404
		} catch(PersistenceException e){
			LOGGER.error("authenticate",e);
			throw new ClientErrorException(INTERNAL_SERVER_ERROR); // HTTP 500
		} catch(IllegalStateException e){
			LOGGER.error("authenticate",e);
			throw new ClientErrorException(INTERNAL_SERVER_ERROR); // HTTP 500
		} catch(NullPointerException e){
			LOGGER.error("authenticate",e);
			throw new ClientErrorException(INTERNAL_SERVER_ERROR); // HTTP 500
		} catch(Exception e){
			LOGGER.error("authenticate",e);
			throw e;
		}
	}
}
