package de.sb.messenger.persistence;

import static org.junit.Assert.*;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.sb.messenger.persistence.Person.Group;

public class PersonEntityTest extends EntityTest {

	public EntityManager entityManager;
	public Validator validator;
	Set<ConstraintViolation<Person>> constrainViolations;

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
		Person person = new Person("test@gmail.com", new Document());
		person.getName().setGiven("John");
		person.getName().setFamily("Smith");
		person.getAddress().setStreet("Falkenbergerstr. 1");
		person.getAddress().setPostcode("12345");
		person.getAddress().setCity("Berlin");
		person.setGroup(Group.USER);
		byte[] hash;
		hash = person.passwordHash("password");
		person.setPasswordHash(hash);

		// non-valid entity
		Person personNV = new Person("testgmail.com", new Document()); // @missing
		personNV.getName().setGiven(""); // empty
		personNV.getName().setFamily(""); // empty
		personNV.getAddress().setStreet("Falkenbergerstr. 1");
		personNV.getAddress().setPostcode("12345678910111213141516"); // longer
																		// then
																		// 15
																		// char
		personNV.getAddress().setCity(""); // empty two errors city size and
											// regex match
		personNV.setGroup(Group.USER);

		constrainViolations = validator.validate(person);
		assertEquals(0, constrainViolations.size());
		// clean up the set
		constrainViolations.clear();
		constrainViolations = validator.validate(personNV);

		assertEquals(7, constrainViolations.size());

	}

	@SuppressWarnings("static-access")
	@Test
	public void testLifeCycle() {
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

		// // add to the DB

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
		assertEquals(person.getName().getGiven(), "John");
		assertEquals(person.getName().getFamily(), "Smith");
		assertEquals(person.getAddress().getCity(), "Berlin");

		// remove person from DB
		entityManager.remove(person);
		transaction.commit();
		transaction.begin();
		entityManager.remove(doc);
		transaction.commit();

		// check if person is deleted , find for getter , Reference for setter
		assertNull(entityManager.find(Document.class, doc.getIdentiy()));
		assertNull(entityManager.find(Person.class, person.getIdentiy()));

	}

	@After
	public void tearDownAfter() throws Exception {
		entityManager.clear();
		entityManager.close();
	}

}