package br.org.funcate.mobile.task;

import java.sql.SQLException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import br.org.funcate.mobile.R;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

/**
 * Activity for loading layout resources
 * 
 * This activity is used to display the Task screen, that.
 * 
 * @author Paulo Luan
 * @version 1.0
 * @since 1.0
 */
public class TaskActivity extends Activity {
	/**
	 * You'll need this in your class to cache the helper in the class.
	 */
	private TaskDatabaseHelper databaseHelper = null;
	private TaskService service = new TaskService();

	private final String LOG_TAG = "#" + getClass().getSimpleName();

	private ProgressDialog dialog;
	private TaskActivity self = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_task);
		
		getHelper().createMockFeatures();
		this.getLocalTasks();

		Button btn_get_tasks = (Button) findViewById(R.id.btn_get_tasks);
		Button btn_send_tasks = (Button) findViewById(R.id.btn_send_tasks);
		Button btn_clear_tasks = (Button) findViewById(R.id.btn_clear_tasks);

		btn_get_tasks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.showLoadingMask();

				try {
					self.getRemoteTasks();
				} catch (Exception e) {
					e.printStackTrace();
				}

				self.hideLoadMask();
			}
		});

		btn_send_tasks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.showLoadingMask();

				try {
					self.sendTasks();
				} catch (Exception e) {
					e.printStackTrace();
				}

				self.hideLoadMask();
			}
		});

		btn_clear_tasks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.showLoadingMask();

				try {
					self.clearAllTasks();
				} catch (Exception e) {
					e.printStackTrace();
				}

				self.hideLoadMask();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		/*
		 * You'll need this in your class to release the helper when done.
		 */
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}

	/**
	 * You'll need this in your class to get the helper from the manager once
	 * per class.
	 */
	private TaskDatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = new TaskDatabaseHelper(getApplicationContext());
		}
		return databaseHelper;
	}

	public List<Task> getLocalTasks() {
		List<Task> list = null;
		try {
			// get our dao
			Dao<Task, Integer> taskDao = getHelper().getTaskDao();
			// query for all of the data objects in the database
			list = taskDao.queryForAll();
			Log.i(LOG_TAG, "GetAll!");
		} catch (SQLException e) {
			Log.e(LOG_TAG, "Database exception", e);
		}
		return list;
	}

	public List<Task> getRemoteTasks() {
		// faz chamada ajax.
		List<Task> remoteTasks = service.getTasks();
		return remoteTasks;
	}

	public void saveTasks(List<Task> tasks) {

	}

	public void sendTasks() {
		service.getTasks();
	}

	public void clearAllTasks() {
		// local.
	}

	public void showLoadingMask() {
		dialog = ProgressDialog.show(TaskActivity.this, "",
				"Carregando, aguarde...", true);
	}

	public void showLoadingMask(String message) {
		dialog = ProgressDialog.show(TaskActivity.this, "", message, true);
	}

	public void hideLoadMask() {
		dialog.hide();
	}
}