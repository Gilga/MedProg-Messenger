package de.sb.messenger.persistence;


import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EntityTest {

	protected static EntityManagerFactory EM_FACTORY;
	protected static ValidatorFactory VALIDATOR_FACTORY;
	private Set<Long> wasteBasket = new HashSet<>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// entity manager object creates entity transaction instance
		EM_FACTORY = Persistence.createEntityManagerFactory("messenger");
		VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
	}

	// creates a persistent unit "messenger"
	public EntityManagerFactory getEntityManagerFactory() {

		return EM_FACTORY;

	}

	public ValidatorFactory getEntityValidatorFactory() {
		return VALIDATOR_FACTORY;

	}

	public Set<Long> getWasteBasket() {
		return wasteBasket;
	}
	
	@After
	public void emptyWasteBasket () {
		final EntityManager entityManager = EM_FACTORY.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			for (final Long identity : this.wasteBasket) {
				
					final Object entity = entityManager.find(BaseEntity.class, identity);
					if (entity != null) entityManager.remove(entity);
			}
			entityManager.getTransaction().commit();
			this.wasteBasket.clear();
		} finally {
			entityManager.close();
		}
	}


	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		EM_FACTORY.close();

	}

}

