package br.org.funcate.baurudigital.common.JPA;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class HibernateFactory {
	private static EntityManager em;
	private static EntityManagerFactory emf;

	public static synchronized EntityManager getEntityManager() {
		if (em == null) {
			if (emf == null)
				emf = Persistence.createEntityManagerFactory("baurudigital-sqlite");
			em = emf.createEntityManager();
		}
		return em;
	}

}