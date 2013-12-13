package br.org.funcate.baurudigital.server.user;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import br.org.funcate.baurudigital.server.common.JPA.HibernateFactory;
import br.org.funcate.baurudigital.server.common.exception.UtilException;
import br.org.funcate.baurudigital.server.common.utils.Util;
/**
 * Keep's state less functions to comunicate with the Database. Can be only accessed by services. 
 * Works only with User entity
 * @author bogo
 *
 */
public class UserDAO {
	public List<User> retrieve()
	{
		
		SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		String query = "from User";
		@SuppressWarnings("unchecked")
		List<User> users = (List<User>) session.createQuery(query).list();
		session.getTransaction().commit();
		session.close();

		return users;
	}
	public User retrieve(String hash) throws UserException
	{
		SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		String query = "from User where hash=:hash";
		@SuppressWarnings("unchecked")
		List<User> users = (List<User>) session.createQuery(query).setString("hash", hash).list();
		session.getTransaction().commit();
		session.close();
		if (users.size()==0) {
			throw new UserException("Usuário solicitado não foi encontrado");
		}
		
		return users.get(0);
	}
	public void save(List<User> users) throws UtilException
	{
		SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		for (User user : users) {
/*			String passwordHash = Util.generateHashMD5(user.getPassword());
			user.setPassword(passwordHash);*/
			session.saveOrUpdate(user);	
		}
		session.getTransaction().commit();
		session.close();
	}
	
}
