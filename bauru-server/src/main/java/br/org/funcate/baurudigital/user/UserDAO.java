package br.org.funcate.baurudigital.user;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import br.org.funcate.baurudigital.common.JPA.HibernateFactory;
import br.org.funcate.baurudigital.common.exception.UtilException;
import br.org.funcate.baurudigital.common.utils.Util;

public class UserDAO {
	public List<User> getAll()
	{
		EntityManager em = HibernateFactory.getEntityManager();
		List<User> users = em.createQuery("from User").getResultList();
		return users;
	}
	public User getUserByHash(String hash) throws UserException
	{
		EntityManager em = HibernateFactory.getEntityManager();
		List<User> users = em.createQuery("from User where hash=:hash").setParameter("hash", hash).getResultList();
		if (users.size()==0) {
			throw new UserException("Usuário solicitado não foi encontrado");
		}
		return users.get(0);
	}
	public void populate(List<User> users) throws UtilException
	{
		EntityManager em = HibernateFactory.getEntityManager();
		em.getTransaction().begin();
		for (User user : users) {
			String passwordHash = Util.generateHashMD5(user.getPassword());
			user.setPassword(passwordHash);
			String userHash = Util.generateHashMD5(user.getLogin()+user.getPassword());
			user.setHash(userHash);
			em.persist(user);	
		}
		em.flush();
		em.getTransaction().commit();
	}
	
}
