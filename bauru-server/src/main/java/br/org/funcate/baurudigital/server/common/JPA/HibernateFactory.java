package br.org.funcate.baurudigital.server.common.JPA;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class HibernateFactory {
	private static EntityManager em;
	private static EntityManagerFactory emf;
	private static SessionFactory sessionFactory;

	public static synchronized EntityManager getEntityManager() {
		if (em == null) {
			if (emf == null)
				emf = Persistence
						.createEntityManagerFactory("baurudigital");
			em = emf.createEntityManager();
		}
		return em;
	}

	public static synchronized SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			Configuration configuration = new Configuration();
			configuration.configure();
			ServiceRegistry sr = new ServiceRegistryBuilder().applySettings(
					configuration.getProperties()).buildServiceRegistry();
			sessionFactory = configuration.buildSessionFactory(sr);

		}
		return sessionFactory;
	}
	
	public static void updateSchema() {
		try {
			Configuration configuration = new Configuration();
			configuration.configure();
			SchemaExport schema = new SchemaExport(configuration);
			schema.create(true, true);
			getSessionFactory().openSession().beginTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}