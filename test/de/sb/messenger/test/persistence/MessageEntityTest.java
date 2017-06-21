package de.sb.messenger.test.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.sb.messenger.persistence.BaseEntity;
import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.messenger.persistence.Person.Group;

public class MessageEntityTest extends EntityTest {

	public EntityManager entityManager;
	public Validator validator;
	Set<ConstraintViolation<Message>> constrainViolations;

	@Before
	public void setUpBefore() throws Exception {

		// entity manager object creates entity transaction instance
		entityManager = this.getEntityManagerFactory().createEntityManager();
		validator = this.getEntityValidatorFactory().getValidator();
	}

	@SuppressWarnings("static-access")
	@Test
	public void testConstrains(){
		// valid entity
		Person person = new Person("test@gmail.com", new Document("image/png", null));
		person.getName().setGiven("John");
		person.getName().setFamily("Smith");
		person.getAddress().setStreet("Falkenbergerstr. 1");
		person.getAddress().setPostcode("12345");
		person.getAddress().setCity("Berlin");
		person.setGroup(Group.USER);
		byte[] hash = person.passwordHash("password");
		person.setPasswordHash(hash);

		// BaseEntity
		BaseEntity baseEntity = new BaseEntity();
		Message message = new Message(person, baseEntity, "Hi there!");
		// a non-valid entity
		Message messageNV = new Message(person, baseEntity, "");

		constrainViolations = validator.validate(message);
		assertEquals(constrainViolations.size(), 0);
		// clean up the set
		constrainViolations.clear();

		constrainViolations = validator.validate(messageNV);
		assertEquals(constrainViolations.size(), 1);

	}

	@Test
	
	public void testLifeCycle()   {
		Person person = entityManager.find(Person.class, 8L);
		BaseEntity baseEntity = entityManager.find(BaseEntity.class,1L);
		Message message = new Message(person,baseEntity , "Hi there!");
	
		// add to the DB
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.persist(message);
		entityManager.getTransaction().commit();

		this.getWasteBasket().add(message.getIdentiy());
		
		transaction.begin();
		message = entityManager.find(Message.class, message.getIdentiy());
		assertEquals(message.getAuthor(), person);
		assertEquals(message.getBody(), "Hi there!");
		
		// remove message from DB
		entityManager.remove(message);
		transaction.commit();
		
		// check if it's deleted , find for getter , Reference for setter	
		assertNull(entityManager.find(Message.class, message.getIdentiy()));
		
	}

	@After
	public void tearDownAfter() throws Exception {
		entityManager.clear();
		entityManager.close();

	}
}


