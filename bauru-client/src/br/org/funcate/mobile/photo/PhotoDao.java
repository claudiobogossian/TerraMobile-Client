package br.org.funcate.mobile.photo;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import android.util.Log;
import br.org.funcate.mobile.database.DatabaseAdapter;
import br.org.funcate.mobile.database.DatabaseHelper;
import br.org.funcate.mobile.form.Form;
import br.org.funcate.mobile.task.Task;
import br.org.funcate.mobile.user.SessionManager;
import br.org.funcate.mobile.user.User;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class PhotoDao {

	private static final String LOG_TAG = "#PHOTODAO";
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


	/**
	 * 
	 *  Delete photos locally.
	 * 
	 * @author Paulo Luan
	 * @return Boolean result
	 */
	public static Integer deletePhotos(List<Photo> photos){
		Dao<Photo, Integer> dao = db.getPhotoDao();
		Integer result = 0;

		try {
			for (Photo photo : photos) {
				File file = new File(photo.getPath());
				file.delete();
				result = dao.delete(photo);
			}
		} catch (SQLException e) {
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Save a list of photos into local database.
	 * 
	 * @author Paulo Luan
	 * @param List
	 *            <Photo> Photos that will be saved into database.
	 */
	public static boolean savePhotos(List<Photo> photos) {
		boolean isSaved = false;
		
		if(photos != null){			
			Dao<Photo, Integer>  photoDao = db.getPhotoDao();

			try {
				for (Photo photo : photos) {
					
					boolean exists = photoDao.idExists(photo.getId());
					
					if(exists){
						photoDao.update(photo);
					} else {
						photoDao.create(photo);
					}
				}
				
				isSaved = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return isSaved;
	}
}
