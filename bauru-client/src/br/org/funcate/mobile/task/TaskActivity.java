package br.org.funcate.mobile.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.Utility;
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
						self.getRemoteZipBaseMap();
					} catch (Exception e) {
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
	private class DownloadTasks extends AsyncTask<String, String, ArrayList<Task>> {

		private String userHash = "";

		public DownloadTasks(String userHash) {
			this.userHash = userHash;
		}


		@Override
		protected ArrayList<Task> doInBackground(String... urls) {
			ArrayList<Task> list = null;

			for (String url : urls) {
				try {
					publishProgress("Fazendo Download das tarefas...");
					ResponseEntity<Task[]> response = restTemplate.getForEntity(url, Task[].class, userHash);
					Task[] tasks = response.getBody();
					list = new ArrayList<Task>(Arrays.asList(tasks));
					
					publishProgress("Salvando tarefas no banco de dados local...");
					self.saveTasksIntoLocalSqlite(list);
				} catch (HttpClientErrorException e) {
					String error = e.getResponseBodyAsString();
					e.printStackTrace();
				}
			}

			return list;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			dialog.setMessage(values[0]);
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
	private class UploadTasks extends AsyncTask<String, String, List<Task>> {

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
					publishProgress("Enviando o seu trabalho para o servidor...");
					Task[] responseTasks = restTemplate.postForObject(url, this.tasks, Task[].class, userHash);
					response = new ArrayList<Task>(Arrays.asList(responseTasks));
					if(response != null) {
						publishProgress("Excluindo tarefas concluídas...");
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
		}

		@Override
		protected void onProgressUpdate(String... values) {
			setLoadMaskMessage(values[0]);
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
	private class UploadPhotos extends AsyncTask<String, String, List<Photo>> {

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
					publishProgress("Fazendo Upload das fotos...");
					Photo[] responsePhotos = restTemplate.postForObject(url, this.photos, Photo[].class, userHash);
					response = new ArrayList<Photo>(Arrays.asList(responsePhotos));

					if(response != null) {
						publishProgress("Verificando se existem imagens não utilizadas no aparelho...");
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
		}

		@Override
		protected void onProgressUpdate(String... values) {
			setLoadMaskMessage(values[0]);
			Log.i(self.LOG_TAG, " Progress: " + values);
		}

		@Override
		protected void onPostExecute(List<Photo> result) {
			//TODO: mudar mensagem de feedback
			
			if(result != null) {
				PhotoDao.deletePhotos(result);
			}
			
			self.hideLoadMask();
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
		dialog.cancel();
		this.updateCountLabels();
	}
	
	/******************************************************************************************************************
	 * This function is responsible to request do ServiceBaseMap to get cached tiles zip file from server
	 ******************************************************************************************************************/
	public void getRemoteZipBaseMap(){
		String url = "http://200.144.100.34:8080/bauru-server/rest/tiles/zip";
		new DownloadZipAsync().execute(url);
	}

	class DownloadZipAsync extends AsyncTask<String, String, String> {
		private ProgressDialog mProgressDialog;
		String path = null;
		String filePath = null;
		
		@Override
		protected String doInBackground(String... urls) {
			
			try {
				path = Environment.getExternalStorageDirectory() + "/osmdroid/tiles/";
				filePath = path + "base_map.zip";
				
				File baseMapZip = new File(path);
				
				if(!baseMapZip.exists()) {
					this.getRemoteBaseMap(urls[0]);					
				}
			} catch (Exception e) {
				Utility.showToast("Ocorreu um erro ao baixar o arquivo.", Toast.LENGTH_LONG, self);
				e.printStackTrace();
			}
			
			try {
				this.unzip(filePath, path);
			} catch (IOException e) {
				Utility.showToast("Ocorreu um erro ao descompactar o arquivo.", Toast.LENGTH_LONG, self);
				e.printStackTrace();
			}

			return path;
		}
		
		/**
		 * Get remote file using URLConnection.
		 * 
		 * @param remoteURL
		 *            The remote url of the file.
		 *            
		 * @throws IOException 
		 */
		public void getRemoteBaseMap(String remoteUrl) throws IOException {
			int count;
			
			URL url = new URL(remoteUrl);
			URLConnection conexion = url.openConnection();
			conexion.connect();

			int fileSize = conexion.getContentLength();

			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(filePath);

			byte data[] = new byte[1024];
			long total = 0;

			while ((count = input.read(data)) != -1) {
				total += count;
				Integer progress = (int) ((total * 100) / fileSize);
				publishProgress("Baixando mapa de base...", "" + progress);
				output.write(data, 0, count);
			}

			output.flush();
			output.close();
			input.close();			
		}

		/**
		 * Unzip files.
		 * 
		 * @param zipfile
		 *            The path of the zip file
		 * @param location
		 *            The new Location that you want to unzip the file
		 * @throws IOException 
		 */
		private void unzip(String zipFile, String outputFolder) throws IOException {	
			int progress = 0;
			
			ZipFile zip = new ZipFile(zipFile);
			publishProgress("Descompactando mapa de base...", "0", "" + zip.size());
			
			FileInputStream fin = new FileInputStream(zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			
			while ((ze = zin.getNextEntry()) != null) {
				if (ze.isDirectory()) {
					Utility.dirChecker(ze.getName());
				} else {
					progress++;
					publishProgress("Descompactando mapa de base...", "" + progress);

					FileOutputStream fout = new FileOutputStream(outputFolder + ze.getName());
					for (int c = zin.read(); c != -1; c = zin.read()) {
						fout.write(c);
					}
					zin.closeEntry();
					fout.close();
				}
			}
			
			zin.close();
		}
		
		void showDialog(String message) {
			mProgressDialog = new ProgressDialog(self);
			mProgressDialog.setMessage(message);
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}
		
		void hideDialog() {
			if(mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}	

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog("Carregando, aguarde...");
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			super.onProgressUpdate(progress);
			
			if(progress.length == 3) {
				mProgressDialog.setMax(Integer.parseInt(progress[2]));
			}
			
			mProgressDialog.setMessage(progress[0]);
			mProgressDialog.setProgress(Integer.parseInt(progress[1]));
		}

		@Override
		protected void onPostExecute(String path) {
			this.hideDialog();
		}		
	}
}