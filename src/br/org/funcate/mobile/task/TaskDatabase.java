package br.org.funcate.mobile.task;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import br.org.funcate.mobile.form.Form;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.AndroidDatabaseConnection;
import com.j256.ormlite.android.DatabaseTableConfigUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class TaskDatabase extends SQLiteOpenHelper {

	private final String LOG_TAG = "#" + getClass().getSimpleName();
	protected AndroidConnectionSource connectionSource = new AndroidConnectionSource(this);

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "tasks.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;

	// the DAO object we use to access the Task table
	private Dao<Task, Integer> taskDao = null;
	private Dao<Form, Integer> formDao = null;

	private static TaskDatabase instance;
	
	private TaskDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static TaskDatabase getInstance(Context context) {
		if (instance == null)
			instance = new TaskDatabase(context);
		return instance;
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		DatabaseConnection conn = connectionSource.getSpecialConnection();
		boolean clearSpecial = false;
		if (conn == null) {
			conn = new AndroidDatabaseConnection(db, true);
			try {
				connectionSource.saveSpecialConnection(conn);
				clearSpecial = true;
				TableUtils.createTableIfNotExists(connectionSource, Form.class);
        		TableUtils.createTableIfNotExists(connectionSource, Task.class);
			} catch (SQLException e) {
				throw new IllegalStateException("Could not save special connection", e);
			}
		}
		try {
			this.createMockFeatures();
		} finally {
			if (clearSpecial) {
				connectionSource.clearSpecialConnection(conn);
			}
		}
	}

	@Override
	public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		DatabaseConnection conn = connectionSource.getSpecialConnection();
		boolean clearSpecial = false;
		if (conn == null) {
			conn = new AndroidDatabaseConnection(db, true);
			try {
				connectionSource.saveSpecialConnection(conn);
				clearSpecial = true;
			} catch (SQLException e) {
				throw new IllegalStateException("Could not save special connection", e);
			}
		}
		try {
			this.upgradeDatabase(oldVersion, newVersion);
		} finally {
			if (clearSpecial) {
				connectionSource.clearSpecialConnection(conn);
			}
		}
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		taskDao = null;
	}

	/**
	 * Returns the Database Access Object (DAO) for our Task class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Task, Integer> getTaskDao() throws SQLException {
		if (taskDao == null) {
			taskDao = getDao(Task.class);
		}
		return taskDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for our Form class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Form, Integer> getFormDao() throws SQLException {
		if (formDao == null) {
			formDao = getDao(Form.class);
		}
		return formDao;
	}

	public void createMockFeatures() {
		try {
			TableUtils.dropTable(connectionSource, Form.class, true);
			TableUtils.dropTable(connectionSource, Task.class, true);
			
			TableUtils.createTableIfNotExists(connectionSource, Form.class);
    		TableUtils.createTableIfNotExists(connectionSource, Task.class);
			
			// here we try inserting data in the on-create as a test
			taskDao = getTaskDao();
			formDao = getFormDao();

			for (int i = 0; i < 20; i++) {
				Task task = new Task();
				Form form = new Form();
				
				task.setAddressName("Address " + i);
				task.setBuildingNumber(i);
				task.setFeatureCode(i);
				task.setIdAddress(i);
				task.setIsSyncronized(false);
				task.setLatitude((double) -23.1791);
				task.setLongitude((double) -45.8872 + ((i + 10) * 3));
				
				form.setAddress("address" + i);
				form.setCity("city" + i);
				form.setDate("date" + i);
				form.setIf1("if1" + i);
				form.setIf2("if2" + i);
				form.setLatitude("latitude");
				form.setLongitude("longitude");
				form.setNumber("number");
				form.setPhoto("photo" + i);
				form.setPostalCode("Code(postalCode" + i);
				form.setState("state" + i);
				
				task.setForm(form);

				formDao.create(form);
				taskDao.create(task);
				
				Log.i(LOG_TAG, "ID_TASK: " + task.getId() + "  ID_FORM: " + form.getId());
			}

			Log.i(LOG_TAG, "created new entries in onCreate: ");
		} catch (SQLException e) {
			Log.e(LOG_TAG, "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	private void upgradeDatabase(int oldVersion, int newVersion) {
		try {
			Log.i(LOG_TAG, "onUpgrade");
			TableUtils.dropTable(connectionSource, Task.class, true);
			// after we drop the old databases, we create the new ones
			this.createMockFeatures();
		} catch (SQLException e) {
			Log.e(LOG_TAG, "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	private <D extends Dao<T, ?>, T> D getDao(Class<T> clazz) throws SQLException {
		// lookup the dao, possibly invoking the cached database config
		Dao<T, ?> dao = DaoManager.lookupDao(connectionSource, clazz);
		if (dao == null) {
			// try to use our new reflection magic
			DatabaseTableConfig<T> tableConfig = DatabaseTableConfigUtil.fromClass(connectionSource, clazz);
			if (tableConfig == null) {
				/**
				 * TODO: we have to do this to get to see if they are using the deprecated annotations like
				 * {@link DatabaseFieldSimple}.
				 */
				dao = (Dao<T, ?>) DaoManager.createDao(connectionSource, clazz);
			} else {
				dao = (Dao<T, ?>) DaoManager.createDao(connectionSource, tableConfig);
			}
		}

		@SuppressWarnings("unchecked")
		D castDao = (D) dao;
		return castDao;
	}
}
