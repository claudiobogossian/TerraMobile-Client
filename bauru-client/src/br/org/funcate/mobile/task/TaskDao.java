package br.org.funcate.mobile.task;

import java.sql.SQLException;
import java.util.List;

import android.util.Log;
import br.org.funcate.mobile.address.Address;
import br.org.funcate.mobile.database.DatabaseAdapter;
import br.org.funcate.mobile.database.DatabaseHelper;
import br.org.funcate.mobile.form.Form;
import br.org.funcate.mobile.user.SessionManager;
import br.org.funcate.mobile.user.User;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

public class TaskDao {

	private static final String LOG_TAG = "#TASKDAO";
	private static DatabaseAdapter db = DatabaseHelper.getDatabase(); 

	/**
	 * 
	 * This function return the local data, persisted in SQLite Database.
	 * 
	 * @author Paulo Luan
	 * @return List<Task>
	 */
	public static List<Task> getLocalTasks() {
		List<Task> list = null;
		try {
			// get our dao
			Dao<Task, Integer> taskDao = db.getTaskDao();
			// query for all of the data objects in the database
			list = taskDao.queryForAll();
		} catch (SQLException e) {
			Log.e(LOG_TAG, "Database exception", e);
		}
		return list;
	}

	/**
	 * 
	 * Delete the rows where is syncronized with the server.
	 * 
	 * @author Paulo Luan
	 * @return Boolean result
	 */
	public static Integer deleteSincronizedTasks() {
		DeleteBuilder<Task, Integer> deleteBuilder = db.getTaskDao().deleteBuilder();
		Integer result = 0;

		try {
			// only delete the rows where syncronized is true
			deleteBuilder.where()
			.eq("done", Boolean.TRUE);
			result = deleteBuilder.delete();
		} catch (SQLException e) {

		}

		return result;
	}

	/**
	 * 
	 * Delete a collection of Tasks from local database.
	 * 
	 * @author Paulo Luan
	 * @return Boolean result
	 */
	public static Integer deleteTasks(List<Task> tasks) {
		Dao<Task, Integer> dao = db.getTaskDao();
		Integer result = 0;

		try {
			result = dao.delete(tasks);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static List<Task> getFinishedTasks() {
		List<Task> tasks = null;

		Dao<Task, Integer> taskDao = DatabaseHelper.getDatabase().getTaskDao();
		Dao<User, Integer> userDao = DatabaseHelper.getDatabase().getUserDao();

		QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
		QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();

		try {
			String userHash = SessionManager.getUserHash();
			userQueryBuilder.where()
			.eq("hash", userHash);

			taskQueryBuilder.where()
			.eq("done", Boolean.TRUE);

			taskQueryBuilder.join(userQueryBuilder);

			tasks = taskQueryBuilder.query();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tasks;
	}

	public static List<Task> getNotFinishedTasks() {
		List<Task> tasks = null;

		Dao<Task, Integer> taskDao = DatabaseHelper.getDatabase().getTaskDao();
		Dao<User, Integer> userDao = DatabaseHelper.getDatabase().getUserDao();

		QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
		QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();

		try {
			String userHash = SessionManager.getUserHash();
			userQueryBuilder.where()
			.eq("hash", userHash);

			taskQueryBuilder.where()
			.eq("done", Boolean.FALSE);

			taskQueryBuilder.join(userQueryBuilder);

			tasks = taskQueryBuilder.query();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tasks;
	}


	/**
	 * Save a list of tasks into local database.
	 * 
	 * @author Paulo Luan
	 * @param List
	 *            <Task> Tasks that will be saved into database.
	 */
	public static boolean saveTasks(List<Task> tasks) {
		boolean isSaved = false;

		if(tasks != null){
			DatabaseAdapter db = DatabaseHelper.getDatabase();	

			Dao<Task, Integer>  taskDao = db.getTaskDao();
			Dao<Form, Integer> formDao = db.getFormDao();
			Dao<User, Integer> userDao = db.getUserDao();
			Dao<Address, Integer> addressDao = db.getAddressDao();

			try {
				for (Task task : tasks) {

					Task persistedTask = taskDao.queryForId(task.getId());

					if(persistedTask == null) {
						formDao.create(task.getForm());
						userDao.create(task.getUser());
						addressDao.create(task.getAddress());
						taskDao.create(task);
					}
				}

				isSaved = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return isSaved;
	}

	/**
	 * Save a task into local database.
	 * 
	 * @author Paulo Luan
	 * @param List
	 *            <Task> Tasks that will be saved into database.
	 */
	public static boolean saveTask(Task task) {
		boolean isSaved = false;

		if(task != null){
			DatabaseAdapter db = DatabaseHelper.getDatabase();	

			Dao<Task, Integer>  taskDao = db.getTaskDao();
			Dao<Form, Integer> formDao = db.getFormDao();
			Dao<User, Integer> userDao = db.getUserDao();
			Dao<Address, Integer> addressDao = db.getAddressDao();

			try {
				Task persistedTask = taskDao.queryForId(task.getId());

				if(persistedTask == null) {
					formDao.create(task.getForm());
					userDao.create(task.getUser());
					addressDao.create(task.getAddress());
					taskDao.create(task);
				}

				isSaved = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return isSaved;
	}
}
