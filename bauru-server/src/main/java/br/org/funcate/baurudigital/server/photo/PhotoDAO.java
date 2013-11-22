package br.org.funcate.baurudigital.server.photo;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import br.org.funcate.baurudigital.server.common.JPA.HibernateFactory;
import br.org.funcate.baurudigital.server.form.Form;
import br.org.funcate.baurudigital.server.form.FormException;
import br.org.funcate.baurudigital.server.user.User;

public class PhotoDAO {

	public void save(List<Photo> photos) {
		SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		for (Photo photo: photos) {
			session.save(photo);
		}
		session.getTransaction().commit();
		session.close();
	}
	public Photo retrieve(int id) throws PhotoException {
		SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		
		List<Photo> photos = (List<Photo>) session.createQuery("from Photo where id=:id").setInteger("id", id).list();
		
		if(photos.size()==0)
		{
			throw new PhotoException("Não existe nenhuma registro para a foto informada.");
		}
		

		session.getTransaction().commit();
		session.close();
		return photos.get(0);
	}
}