


package br.org.funcate.mobile.database;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import br.org.funcate.mobile.address.Address;
import br.org.funcate.mobile.form.Form;
import br.org.funcate.mobile.photo.Photo;
import br.org.funcate.mobile.task.Task;
import br.org.funcate.mobile.user.User;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.AndroidDatabaseConnection;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 */
public class DatabaseAdapter extends OrmLiteSqliteOpenHelper {

	private final String LOG_TAG = "#" + getClass().getSimpleName();
	protected AndroidConnectionSource connectionSource = new AndroidConnectionSource(this);

	// name of the database file for your application -- change to something
	// appropriate for your app
	private static final String DATABASE_NAME = "tasks.db";
	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final int DATABASE_VERSION = 1;

	// the DAO object we use to access the Task table
	private Dao<Task, Integer> taskDao = null;
	private Dao<Form, Integer> formDao = null;
	private Dao<Photo, Integer> photoDao = null;
	private Dao<User, Integer> userDao = null;
	private Dao<Address, Integer> addressDao = null;

//	private static DatabaseAdapter instance;

	public DatabaseAdapter(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		try {
			this.createDaos();
			this.dropTables();
			this.createTables();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

//	public static DatabaseAdapter getInstance(Context context) {
//		if (instance == null)
//			instance = new DatabaseAdapter(context);
//		return instance;
//	}

	public void createDaos() throws SQLException {
		taskDao = getDao(Task.class);
		formDao = getDao(Form.class);
		photoDao = getDao(Photo.class);
		userDao = getDao(User.class);
		addressDao = getDao(Address.class);
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		DatabaseConnection conn = connectionSource.getSpecialConnection();
		boolean clearSpecial = false;
		if (conn == null) {
			conn = new AndroidDatabaseConnection(db, true);
			try {
				connectionSource.saveSpecialConnection(conn);
				clearSpecial = true;
				this.createTables();
			} catch (SQLException e) {
				throw new IllegalStateException("Could not save special connection", e);
			}
		}
		try {
//			this.createMockFeatures();
		} finally {
			if (clearSpecial) {
				connectionSource.clearSpecialConnection(conn);
			}
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	public void onUpgrade(SQLiteDatabase db, ConnectionSource arg1, int oldVersion, int newVersion) {
		DatabaseConnection conn = connectionSource.getSpecialConnection();
		boolean clearSpecial = false;
		if (conn == null) {
			conn = new AndroidDatabaseConnection(db, true);
			try {
				connectionSource.saveSpecialConnection(conn);
				clearSpecial = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		try {
			this.dropTables();
			this.createTables();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (clearSpecial) {
				connectionSource.clearSpecialConnection(conn);
			}
		}
	}

	public void dropTables() throws SQLException {
		TableUtils.dropTable(connectionSource, Address.class, true);
		TableUtils.dropTable(connectionSource, User.class, true);
		TableUtils.dropTable(connectionSource, Photo.class, true);
		TableUtils.dropTable(connectionSource, Form.class, true);
		TableUtils.dropTable(connectionSource, Task.class, true);
	}

	public void createTables() throws SQLException {
		TableUtils.createTable(connectionSource, Address.class);
		TableUtils.createTable(connectionSource, User.class);
		TableUtils.createTable(connectionSource, Photo.class);
		TableUtils.createTable(connectionSource, Form.class);
		TableUtils.createTable(connectionSource, Task.class);
	}
	
	/**
	 * Clear all user registers, by droping and recreating the user table. 
	 * 
	 * @author Paulo Luan
	 * */
	public void resetUserTable() throws SQLException {
		TableUtils.dropTable(connectionSource, User.class, true);
		TableUtils.createTable(connectionSource, User.class);
	}

	public <D extends Dao<T, ?>, T> D getDao(Class<T> clazz) throws SQLException {
		Dao<T, ?> dao = (Dao<T, ?>) DaoManager.createDao(connectionSource, clazz);
		@SuppressWarnings("unchecked")
		D castDao = (D) dao;
		return castDao;
	}	
	
	/**
	 * Save a Photo into local database.
	 * 
	 * @author Paulo Luan
	 * @param List
	 *            <Task> Tasks that will be saved into database.
	 */
	public static boolean savePhotos(List<Photo> photos) {
		boolean isSaved = false;
		
		if(photos != null) {
			DatabaseAdapter db = DatabaseHelper.getDatabase();	
			Dao<Photo, Integer>  photoDao = db.getPhotoDao();

			try {
				for (Photo photo : photos) {
					photoDao.createIfNotExists(photo);
				}
				isSaved = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return isSaved;
	}

	public Dao<Task, Integer> getTaskDao() {
		return taskDao;
	}

	public void setTaskDao(Dao<Task, Integer> taskDao) {
		this.taskDao = taskDao;
	}

	public Dao<Form, Integer> getFormDao() {
		return formDao;
	}

	public void setFormDao(Dao<Form, Integer> formDao) {
		this.formDao = formDao;
	}

	public Dao<Photo, Integer> getPhotoDao() {
		return photoDao;
	}

	public void setPhotoDao(Dao<Photo, Integer> photoDao) {
		this.photoDao = photoDao;
	}

	public Dao<User, Integer> getUserDao() {
		return userDao;
	}

	public void setUserDao(Dao<User, Integer> userDao) {
		this.userDao = userDao;
	}

	public Dao<Address, Integer> getAddressDao() {
		return addressDao;
	}

	public void setAddressDao(Dao<Address, Integer> addressDao) {
		this.addressDao = addressDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		taskDao = null;
		formDao = null;
		photoDao = null;
		userDao = null;
		addressDao = null;
	}
}