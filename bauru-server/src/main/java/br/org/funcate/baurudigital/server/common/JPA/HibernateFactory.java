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
/**
 * Keeps Hibernate functions and variables, to be user by DAO Classes
 * @author bogo
 *
 */
public class HibernateFactory {
	private static EntityManager em;
	private static EntityManagerFactory emf;
	private static SessionFactory sessionFactory;

	
	/**
	 * Allow to get a SessionFactory to start a new HQL
	 * @return
	 */
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
}