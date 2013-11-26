package br.org.funcate.mobile.photo;

import java.sql.SQLException;
import java.util.List;

import br.org.funcate.mobile.database.DatabaseAdapter;
import br.org.funcate.mobile.database.DatabaseHelper;
import br.org.funcate.mobile.form.Form;
import br.org.funcate.mobile.task.Task;
import br.org.funcate.mobile.user.SessionManager;
import br.org.funcate.mobile.user.User;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class PhotoDao {

	private static DatabaseAdapter db = DatabaseHelper.getDatabase();
	
	public static List<Photo> getNotSyncPhotos() {
		List<Photo> photos = null;

		Dao<Task, Integer> taskDao = db.getTaskDao();
		Dao<Form, Integer> formDao = db.getFormDao();
		Dao<Photo, Integer> photoDao = db.getPhotoDao();
		Dao<User, Integer> userDao = db.getUserDao();

		QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
		QueryBuilder<Form, Integer> formQueryBuilder = formDao.queryBuilder();
		QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
		QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();

		try {
			String userHash = SessionManager.getUserHash();
			userQueryBuilder.where()
				.eq("hash", userHash);

			taskQueryBuilder.where()
				.eq("done", Boolean.TRUE);
			taskQueryBuilder.join(userQueryBuilder);
			
			formQueryBuilder.join(taskQueryBuilder);

			photoQueryBuilder.join(formQueryBuilder);

			photos = photoQueryBuilder.query();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return photos;
	}	

	public static void deletePhotos(List<Photo> photos){
		// TODO: excluir as fotos.
	}

}
