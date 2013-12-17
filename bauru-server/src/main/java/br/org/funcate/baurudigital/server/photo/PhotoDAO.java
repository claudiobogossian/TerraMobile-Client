package br.org.funcate.baurudigital.server.photo;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import br.org.funcate.baurudigital.server.common.JPA.HibernateFactory;
/**
 * Keep's state less functions to comunicate with the Database. Can be only accessed by services. 
 * Works only with Photo entity
 * @author bogo
 *
 */
public class PhotoDAO {

	public void save(List<Photo> photos) {
		for (Photo photo: photos) {
			SessionFactory sessionFactory = HibernateFactory.getSessionFactory();
			Session session = sessionFactory.openSession();
			session.beginTransaction();
			session.save(photo);
			session.getTransaction().commit();
			session.close();
		}
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
