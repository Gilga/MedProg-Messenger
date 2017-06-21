package de.sb.messenger.test.persistence;

import javax.persistence.Persistence;

public class SanityTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Persistence.createEntityManagerFactory("messenger").createEntityManager();
	}
}
