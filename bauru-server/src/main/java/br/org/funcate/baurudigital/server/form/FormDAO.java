package br.org.funcate.baurudigital.server.form;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import br.org.funcate.baurudigital.server.common.JPA.HibernateFactory;
import br.org.funcate.baurudigital.server.user.User;

public class FormDAO {

	public Form retrieve(int id) throws FormException {
		SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		List<Form> forms = (List<Form>) session.createQuery("from Form where id=:id").setInteger("id", id).list();
		session.getTransaction().commit();
		session.close();
		
		if(forms.size()==0)
		{
			throw new FormException("Não existe nenhuma registro para o form informado.");
		}
		return forms.get(0);
	}
}