package br.org.funcate.baurudigital.server.task;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import br.org.funcate.baurudigital.server.common.JPA.HibernateFactory;
import br.org.funcate.baurudigital.server.user.User;

public class TaskDAO {
	public List<Task> retrieve(User user) {

		SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String query = "from Task where user_id=:user_id";
		@SuppressWarnings("unchecked")
		List<Task> tasks = (List<Task>) session.createQuery(query)
				.setString("user_id", user.getId().toString()).list();

		session.getTransaction().commit();
		session.close();

		return tasks;
	}

	public void save(List<Task> tasks) {
		SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		for (Task task : tasks) {
			session.save(task);
		}
		session.getTransaction().commit();
		session.close();
	}
}