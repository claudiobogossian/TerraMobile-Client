package br.org.funcate.baurudigital.server.task;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import br.org.funcate.baurudigital.server.common.JPA.HibernateFactory;
import br.org.funcate.baurudigital.server.user.User;
/**
 * Keep's state less functions to comunicate with the Database. Can be only accessed by services. 
 * Works only with Task entity
 * @author bogo
 *
 */
public class TaskDAO {
	public List<Task> retrieve(User user) {

		SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String query = "from Task where user_id=:user_id and done=:done";
		@SuppressWarnings("unchecked")
		List<Task> tasks = (List<Task>) session.createQuery(query).setInteger("user_id", user.getId()).setBoolean("done", false).list();

		session.getTransaction().commit();
		session.close();

		return tasks;
	}

	public void save(List<Task> tasks) {
		SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		for (Task task : tasks) {
			session.saveOrUpdate(task);
		}
		session.getTransaction().commit();
		session.close();
	}
}
