package br.org.funcate.mobile.task;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
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
	private final String hostUrl = "http://200.144.100.34:8080/";

	private TextView txtIncompleteTasks;
	private TextView txtNotSyncRegisters;

	private ProgressDialog dialog;
	private TaskActivity self = this;

	private RestTemplate restTemplate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_task);

		txtIncompleteTasks = (TextView) findViewById(R.id.txt_count_incompleted_tasks);
		txtNotSyncRegisters = (TextView) findViewById(R.id.txt_count_completed_tasks);

		this.setButtonsListeners();
		this.initializeRestTemplate();
		this.updateCountLabels();
	}

	public void setButtonsListeners() {
		Button btn_get_tasks = (Button) findViewById(R.id.btn_get_tasks);
		btn_get_tasks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Utility.isNetworkAvailable(self)) {
					try {
						self.showLoadingMask();
						self.saveTasksOnServer();
						self.savePhotosOnServer();
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
		Button btnLogout = (Button) findViewById(R.id.btnLogout);
		btnLogout.setText(Html.fromHtml("Sair <b>" + name + "</b>"));
		btnLogout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Clear the session data This will clear all session data and redirect user to LoginActivity
				setResult(999);
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
	}

	public void initializeRestTemplate() {
		this.restTemplate = new RestTemplate();

		// Add converters, Note I use the Jackson Converter, I removed the http form converter  because it is not needed when posting String, used for multipart forms.
		this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		this.restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

		// Set the request factory IMPORTANT: This section I had to add for POST request. Not needed for GET
		this.restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
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
	 *  Update the labels that show to user the count of registers on the local database.
	 *
	 */
	public void updateCountLabels() {
		String incompletedTasks = "" + TaskDao.getCountOfIncompletedTasks();
		String completedTasks = "" + TaskDao.getCountOfCompletedTasks();

		txtIncompleteTasks.setText(incompletedTasks);
		txtNotSyncRegisters.setText(completedTasks);
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
		String userHash = SessionManager.getUserHash();
		String url =  hostUrl + "bauru-server/rest/tasks?user={user_hash}";
		DownloadTasks remote = new DownloadTasks(userHash);
		remote.execute(new String[] { url });
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
			//String url = "http://200.144.100.34:8080/bauru-server/rest/tasks?user={user_hash}";
			String url =  hostUrl + "bauru-server/rest/tasks?user={user_hash}";
			UploadTasks remote = new UploadTasks(tasks, userHash);
			remote.execute(new String[] { url });
		} else {
			self.getRemoteTasks();
		}
	}

	/**
	 * Save a list of Tasks, creating an object that send a post request to server.
	 * 
	 * @author Paulo Luan
	 */
	public void savePhotosOnServer() {
		String userHash = SessionManager.getUserHash();
		List<Photo> photos = PhotoDao.getNotSyncPhotos();

		if(photos != null && !photos.isEmpty()) {
			String url = hostUrl + "bauru-server/rest/photos?user={user_hash}";
			UploadPhotos remote = new UploadPhotos(photos, userHash);
			remote.execute(new String[] { url });
		}
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
					Task[] tasks = response.getBody();
					list = new ArrayList<Task>(Arrays.asList(tasks));
					
//					self.setLoadMaskMessage("Salvando tarefas no banco de dados local...");
					self.saveTasksIntoLocalSqlite(list);
				} catch (HttpClientErrorException e) {
					String error = e.getResponseBodyAsString();
					e.printStackTrace();
				}
			}

			return list;
		}
		
		@Override
		protected void onPreExecute() {
			self.setLoadMaskMessage("Fazendo Download das tarefas...");
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.i("#TASKSERVICE", " Progress: " + values);
		}

		protected void onPostExecute(ArrayList<Task> tasks) {
			self.hideLoadMask();
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
	private class UploadTasks extends AsyncTask<String, Void, List<Task>> {

		private List<Task> tasks;
		private String userHash;

		public UploadTasks(List<Task> tasks, String userHash) {
			this.tasks = tasks;
			this.userHash = userHash;
		}

		@Override
		protected List<Task> doInBackground(String... urls) {
			List<Task> response = null;

			for (String url : urls) {
				try {
					Task[] responseTasks = restTemplate.postForObject(url, this.tasks, Task[].class, userHash);
					response = new ArrayList<Task>(Arrays.asList(responseTasks));
					
					if(response != null) {
						//self.setLoadMaskMessage("Excluindo tarefas concluídas...");
						TaskDao.deleteTasks(tasks);
					}
				} catch (HttpClientErrorException e) {
					String error = e.getResponseBodyAsString();
					e.printStackTrace();
				}
			}

			return response;
		}
		
		@Override
		protected void onPreExecute() {
			self.setLoadMaskMessage("Enviando o seu trabalho para o servidor...");
		}

		@Override
		protected void onProgressUpdate(Void... values) {			
			Log.i(self.LOG_TAG, " Progress: " + values);
		}

		@Override
		protected void onPostExecute(List<Task> result) {
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
	private class UploadPhotos extends AsyncTask<String, Void, List<Photo>> {

		private List<Photo> photos;
		private String userHash;

		public UploadPhotos(List<Photo> photos, String userHash) {
			this.photos = photos;
			this.userHash = userHash;
		}

		@Override
		protected List<Photo> doInBackground(String... urls) {
			List<Photo> response = null;
			
			for (String url : urls) {
				try {
					Photo[] responsePhotos = restTemplate.postForObject(url, this.photos, Photo[].class, userHash);
					response = new ArrayList<Photo>(Arrays.asList(responsePhotos));
					
					if(response != null) {
						//self.setLoadMaskMessage("Verificando se existem imagens não utilizadas no aparelho...");
						PhotoDao.deletePhotos(response);
					}
				} catch (HttpClientErrorException e) {
					String error = e.getResponseBodyAsString();
					e.printStackTrace();
				}
			}
			return response;
		}
		

		@Override
		protected void onPreExecute() {
			//self.setLoadMaskMessage("Fazendo Upload das fotos...");
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.i(self.LOG_TAG, " Progress: " + values);
		}

		@Override
		protected void onPostExecute(List<Photo> result) {
			//self.hideLoadMask();
		}	
	}

	public void showLoadingMask() {
		dialog = ProgressDialog.show(TaskActivity.this, "", "Carregando, aguarde...", true);
	}

	public void showLoadingMask(String message) {
		dialog = ProgressDialog.show(TaskActivity.this, "", message, true);
	}
	
	public void setLoadMaskMessage(String message) {
		this.dialog.setMessage(message);
	}

	public void hideLoadMask() {
		dialog.hide();
		this.updateCountLabels();
	}

}