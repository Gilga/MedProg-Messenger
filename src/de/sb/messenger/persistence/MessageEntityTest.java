package de.sb.messenger.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
	public void testConstrains() {
		// valid entity
		Person person = new Person("test@gmail.com", new Document("image/png", null));
		person.getName().setGiven("John");
		person.getName().setFamily("Smith");
		person.getAddress().setStreet("Falkenbergerstr. 1");
		person.getAddress().setPostcode("12345");
		person.getAddress().setCity("Berlin");
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

	public void testLifeCycle() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		// Person person = entityManager.find(Person.class, 8L);
		// BaseEntity baseEntity = entityManager.find(BaseEntity.class,8L);
		// Message message = new Message(person,baseEntity , "Hi there!");

		// create entity
		String s = "some content";
		byte[] content = s.getBytes();
		Document doc = new Document("image/jpeg", content);
		Person person = new Person("test@gmail.com", doc);
		person.getName().setGiven("John");
		person.getName().setFamily("Smith");
		person.getAddress().setStreet("Falkenbergerstr. 1");
		person.getAddress().setPostcode("12345");
		person.getAddress().setCity("Berlin");

		byte[] hash = person.passwordHash("password");
		person.setPasswordHash(hash);

		// case 1
		// Person poly_subject = new Person("subject@gmail.com", doc);
		// poly_subject.getName().setGiven("Poly");
		// poly_subject.getName().setFamily("Subject");
		// poly_subject.getAddress().setStreet("Falkenbergerstr. 1");
		// poly_subject.getAddress().setPostcode("12345");
		// poly_subject.getAddress().setCity("Berlin");
		// byte[] hash2 = poly_subject.passwordHash("password");
		// poly_subject.setPasswordHash(hash2);

		// case 2
		// Document poly_subject = new Document("image/jpeg", content);

		// BaseEntity subject = new BaseEntity();
		Message message = new Message(person, doc, "Hi there!");

		// case 3
		// Message poly_subject = new Message(person, message, "Hi there!");

		// // add to the DB
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.persist(doc);
		entityManager.getTransaction().commit();
		transaction.begin();
		entityManager.persist(person);
		entityManager.getTransaction().commit();
		// transaction.begin();
		// entityManager.persist(poly_subject);
		// entityManager.getTransaction().commit();
		transaction.begin();
		entityManager.persist(message);
		entityManager.getTransaction().commit();

		this.getWasteBasket().add(doc.getIdentiy());
		this.getWasteBasket().add(person.getIdentiy());
		// this.getWasteBasket().add(poly_subject.getIdentiy());
		this.getWasteBasket().add(message.getIdentiy());

		transaction.begin();
		message = entityManager.find(Message.class, message.getIdentiy());
		// assertEquals(message.getAuthor(), person);
		// assertEquals(message.getBody(), "Hi there!");
		// assertEquals(message.getIdentiy(), poly_subject);

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
