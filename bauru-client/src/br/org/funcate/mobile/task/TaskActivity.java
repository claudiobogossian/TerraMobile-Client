package br.org.funcate.mobile.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.Utility;
import br.org.funcate.mobile.address.Address;
import br.org.funcate.mobile.database.DatabaseAdapter;
import br.org.funcate.mobile.database.DatabaseHelper;
<<<<<<< HEAD
import br.org.funcate.mobile.map.ServiceBaseMap;
=======
import br.org.funcate.mobile.form.Form;
import br.org.funcate.mobile.photo.Photo;
>>>>>>> e9af9b4090123d533170eac67457611616a88c7d
import br.org.funcate.mobile.user.SessionManager;
import br.org.funcate.mobile.user.User;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

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

	private DatabaseAdapter db;

	private final String LOG_TAG = "#" + getClass().getSimpleName();

	private ProgressDialog dialog;
	private TaskActivity self = this;

	private RestTemplate restTemplate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_task);

		db = DatabaseHelper.getDatabase();

		this.getLocalTasks();

		Button btn_get_tasks = (Button) findViewById(R.id.btn_get_tasks);

		btn_get_tasks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Utility.isNetworkAvailable(self)) {
					try {
						self.showLoadingMask();
						self.saveTasksOnServer();
					} catch (Exception e) {
						self.hideLoadMask();
						e.printStackTrace();
					}
				} else {
					Toast.makeText(getApplicationContext(), "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
					Log.i(self.LOG_TAG, "Sem conexão com a internet.");
				}
			}
		});

		String name = SessionManager.getUserName();
		TextView lblName = (TextView) findViewById(R.id.lblName);
		lblName.setText(Html.fromHtml("Nome: <b>" + name + "</b>"));
		Button btnLogout = (Button) findViewById(R.id.btnLogout);
		btnLogout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Clear the session data This will clear all session data and redirect user to LoginActivity
				SessionManager.logoutUser();
			}
		});
		
		Button btn_get_tiles = (Button) findViewById(R.id.btn_get_tiles);

		btn_get_tiles.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Utility.isNetworkAvailable(self)) {
					try {
						self.showLoadingMask();
						self.getTiles();
					} catch (Exception e) {
						self.hideLoadMask();
						e.printStackTrace();
					}
				} else {
					Toast.makeText(getApplicationContext(), "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
					Log.i(self.LOG_TAG, "Sem conexão com a internet.");
				}
			}
		});

		this.restTemplate = new RestTemplate();
		this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
	}
	/**
	 * This function is responsible to request do ServiceBaseMap to get cached tiles zip file from server
	 */
	public void getTiles()
	{
		new ServiceBaseMap().getRemoteZipBaseMap();
	}

	/**
	 * 
	 * This function return the local data, persisted in SQLite Database.
	 * 
	 * @author Paulo Luan
	 * @return List<Task>
	 */
	public List<Task> getLocalTasks() {
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
	 * Instantiate a service object, that realize AJAX call to persist the local
	 * data with the remote database via Rest.
	 * 
	 * @author Paulo Luan
	 * @return List<Task>
	 */
	public void getRemoteTasks() {
		// faz chamada ajax.
		List<Task> remoteTasks = null;
		String hash = SessionManager.getUserHash();
		this.getTasks(hash);
	}

	/**
	 * Save a list of tasks into local database.
	 * 
	 * @author Paulo Luan
	 * 
	 * @param List
	 *            <Task> Tasks that will be saved into database.
	 */
	public void saveTasksIntoLocalSqlite(List<Task> tasks) {
		if(tasks != null){
			DatabaseAdapter db = DatabaseHelper.getDatabase();	
			
			Dao<Task, Integer>  taskDao = db.getTaskDao();
			Dao<Form, Integer> formDao = db.getFormDao();
			Dao<User, Integer> userDao = db.getUserDao();
			Dao<Address, Integer> addressDao = db.getAddressDao();

			try {
				for (Task task : tasks) {
					formDao.create(task.getForm());
					userDao.createIfNotExists(task.getUser());
					addressDao.create(task.getAddress());
					taskDao.create(task);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		self.hideLoadMask();
	}

	/**
	 * 
	 * Delete the rows where is syncronized with the server.
	 * 
	 * @author Paulo Luan
	 * @return Boolean result
	 */
	public Integer deleteSincronizedTasks() {
		DeleteBuilder<Task, Integer> deleteBuilder = db.getTaskDao().deleteBuilder();
		Integer result = 0;

		try {
			// only delete the rows where syncronized is true
			deleteBuilder.where().eq("syncronized", Boolean.TRUE);
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
	public Integer deleteTasks(List<Task> tasks) {
		Dao<Task, Integer> dao = db.getTaskDao();
		Integer result = 0;

		try {
			result = dao.delete(tasks);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Get a list of Taks, sending a get request to server.
	 * 
	 * @author Paulo Luan
	 */
	public void getTasks(String userHash) {		
		String url = "http://192.168.5.60:8080/bauru-server/rest/tasks?user={user_hash}";
		DownloadTasks remote = new DownloadTasks(userHash);
		remote.execute(new String[] { url });
	}

	/**
	 * Save a list of Taks, sending a post request to server.
	 * 
	 * @author Paulo Luan
	 * @param zipfile
	 *            The path of the zip file
	 * @param location
	 *            The new Location that you want to unzip the file
	 */
	public void saveTasksOnServer() {
		String userHash = SessionManager.getUserHash();
		List<Task> tasks = self.getNotSyncronizedTasks();
		
		if(tasks != null) {
			String url = "http://192.168.5.60:8080/bauru-server/rest/tasks?user={user_hash}";
			UploadTasks remote = new UploadTasks(tasks, userHash);
			remote.execute(new String[] { url });
		} else {
			self.getRemoteTasks();
		}
	}
	
	public List<Task> getNotSyncronizedTasks() {
		// db.getTasks ...
		Dao<Task, Integer> dao = db.getTaskDao();
		List<Task> tasks = null;
		
		try {
			tasks = dao.queryForEq("syncronized", Boolean.FALSE);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return tasks;
	}

	/**
	 * Async class implementation to get tasks from server.
	 * 
	 * @author Paulo Luan
	 * 
	 * @param String... urls
	 *            URL's that will called.
	 */
	private class DownloadTasks extends AsyncTask<String, Void, ArrayList<Task>> {

		private String userHash = "";

		public DownloadTasks(String userHash) {
			this.userHash = userHash;
		}

		@Override
		protected ArrayList<Task> doInBackground(String... urls) {
			ArrayList<Task> list = null;

			for (String url : urls) {
				try {					
					ResponseEntity<Task[]> response = restTemplate.getForEntity(url, Task[].class, userHash);
					response.getStatusCode();
					Task[] tasks = response.getBody();
					list = new ArrayList<Task>(Arrays.asList(tasks));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return list;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.i("#TASKSERVICE", " Progress: " + values);
		}
		
		protected void onPostExecute(ArrayList<Task> tasks) {
			self.saveTasksIntoLocalSqlite(tasks);
			Log.i("#TASKSERVICE", "DoPostExecute!");
		}
	}



	/**
	 * Async object implementation to PostTasks to server
	 * 
	 * @param String... urls
	 *            URL's that will called.
	 * @author Paulo Luan 
	 */
	private class UploadTasks extends AsyncTask<String, Void, ArrayList<Task>> {

		private List<Task> tasks;
		private String userHash;

		public UploadTasks(List<Task> tasks, String userHash) {
			this.tasks = tasks;
			this.userHash = userHash;
		}

		@Override
		protected ArrayList<Task> doInBackground(String... urls) {
			ArrayList<Task> list = null;

			for (String url : urls) {
				try {
					restTemplate.postForObject(url, this.tasks, ResponseEntity.class, userHash);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return list;
		}

		@Override
		protected void onProgressUpdate(Void... values) {			
			Log.i(self.LOG_TAG, " Progress: " + values);
		}

		@Override
		protected void onPostExecute(ArrayList<Task> result) {
			//HttpStatus status = response.getStatusCode();
			
			if(result != null){
				//TODO: verificar o status para excluir somente quando tiver certeza de que foi salvo remotamente.
				self.deleteTasks(tasks);
			}
			
			self.getRemoteTasks();
			
			Log.i(self.LOG_TAG, "UploadTasks");
		}
		
	}

	public void showLoadingMask() {
		dialog = ProgressDialog.show(TaskActivity.this, "", "Carregando, aguarde...", true);
	}

	public void showLoadingMask(String message) {
		dialog = ProgressDialog.show(TaskActivity.this, "", message, true);
	}

	public void hideLoadMask() {
		dialog.hide();
	}
}