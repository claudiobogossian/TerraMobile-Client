package br.org.funcate.mobile.task;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 * 
 * @author Paulo Luan
 */
public class TaskDatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "task.db";
	private static final int DATABASE_VERSION = 1;

	private Dao<Task, Integer> taskDao;

	public TaskDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Task.class);
		} catch (SQLException e) {
			Log.e(TaskDatabaseHelper.class.getName(), "Unable to create datbases", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, Task.class, true);
			onCreate(sqliteDatabase, connectionSource);
		} catch (SQLException e) {
			Log.e(TaskDatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "
					+ newVer, e);
		}
	}

	public Dao<Task, Integer> getTaskDao() throws SQLException {
		if (taskDao == null) {
			taskDao = getDao(Task.class);
		}
		return taskDao;
	}
}
