package br.org.funcate.mobile.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.Utility;
import br.org.funcate.mobile.map.ServiceBaseMap;
import br.org.funcate.mobile.photo.Photo;
import br.org.funcate.mobile.photo.PhotoDao;
import br.org.funcate.mobile.user.SessionManager;

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

	private final String LOG_TAG = "#" + getClass().getSimpleName();

	private ProgressDialog dialog;
	private TaskActivity self = this;

	private RestTemplate restTemplate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_task);

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
				setResult(000);
				SessionManager.logoutUser();
				finish();
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

<<<<<<< HEAD

=======
>>>>>>> 86b8be9ee273bf82ca4866f69d3dd0f4f6f8a9dd
	/**
	 * This function is responsible to request do ServiceBaseMap to get cached tiles zip file from server
	 */
	public void getTiles()
	{
		new ServiceBaseMap().getRemoteZipBaseMap();
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
		if(tasks != null) {
			TaskDao.saveTasks(tasks);
		}

		self.hideLoadMask();
	}

	/**
	 * Get a list of Taks, sending a get request to server.
	 * 
	 * @author Paulo Luan
	 */
	public void getTasks(String userHash) {		
		String url = "http://200.144.100.34:8080/bauru-server/rest/tasks?user={user_hash}";
		DownloadTasks remote = new DownloadTasks(userHash);
		remote.execute(new String[] { url });
	}

	/**
	 * Save a list of Tasks, creating an object that send a post request to server.
	 * 
	 * @author Paulo Luan
	 */
	public void saveTasksOnServer() {
		String userHash = SessionManager.getUserHash();
		List<Task> tasks = TaskDao.getFinishedTasks();

		if(tasks != null && !tasks.isEmpty()) {
			String url = "http://200.144.100.34:8080/bauru-server/rest/tasks?user={user_hash}";
			UploadTasks remote = new UploadTasks(tasks, userHash);
			remote.execute(new String[] { url });
		} else {
			self.getRemoteTasks();
		}
	}

<<<<<<< HEAD
=======
	/**
	 * Save a list of Tasks, creating an object that send a post request to server.
	 * 
	 * @author Paulo Luan
	 */
	public void savePhotosOnServer() {
		String userHash = SessionManager.getUserHash();
		List<Photo> photos = PhotoDao.getNotSyncPhotos();

		if(photos != null && !photos.isEmpty()) {
			String url = "http://200.144.100.34:8080/bauru-server/rest/photos?user={user_hash}";
			UploadPhotos remote = new UploadPhotos(photos, userHash);
			remote.execute(new String[] { url });
		}
	}
>>>>>>> 86b8be9ee273bf82ca4866f69d3dd0f4f6f8a9dd

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
					String response = restTemplate.postForObject(url, this.tasks, String.class, userHash);				
					//HttpStatus status = response.getStatusCode();
					//Task[] responseTasks = response.getBody();
//					//list = new ArrayList<Task>(Arrays.asList(responseTasks));
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
			if(result != null){
				//TODO: verificar o status para excluir somente quando tiver certeza de que foi salvo remotamente.
				TaskDao.deleteTasks(tasks);
			}

			self.getRemoteTasks();
		}	
	}


	/**
	 * Async object implementation to Post Photos to server
	 * 
	 * @param String... urls
	 *            URL's that will called.
	 * @author Paulo Luan 
	 */
	private class UploadPhotos extends AsyncTask<String, Void, ArrayList<Photo>> {

		private List<Photo> photos;
		private String userHash;

		public UploadPhotos(List<Photo> photos, String userHash) {
			this.photos = photos;
			this.userHash = userHash;
		}

		@Override
		protected ArrayList<Photo> doInBackground(String... urls) {
			ArrayList<Photo> list = null;

			for (String url : urls) {
				try {
					ResponseEntity<Photo[]> response = restTemplate.postForObject(url, this.photos, ResponseEntity.class, userHash);
					Photo[] photos = response.getBody();
					HttpStatus status = response.getStatusCode();
					list = new ArrayList<Photo>(Arrays.asList(photos));
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
		protected void onPostExecute(ArrayList<Photo> result) {
			if(result != null){
				PhotoDao.deletePhotos(result);
			}
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