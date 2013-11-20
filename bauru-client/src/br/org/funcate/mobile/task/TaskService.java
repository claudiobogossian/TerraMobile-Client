package br.org.funcate.mobile.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.util.Log;

/**
 * 
 * Service REST. Ajax calls to get, post and put objects to server.
 * 
 * @author Paulo Luan
 * */
public class TaskService {

	private RestTemplate restTemplate;

	public TaskService() {
		this.restTemplate = new RestTemplate();
		this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
	}

	/**
	 * Get a list of Taks, sending a get request to server.
	 * 
	 * @author Paulo Luan
	 */
	public void getTasks(String userHash) {		
		String url = "http://192.168.5.60:8080/bauru-server/rest/tasks/get?user={user_hash}";
		
		userHash = "5e292159bb5bb5ac5ed993aaff0c410c"; // TODO: remove this.
		
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
	public void saveTasks(List<Task> tasks, String userHash) {
		String url = "http://192.168.5.60:8080/bauru-server/rest/tasks/save?user={user_hash}";
		userHash = "5e292159bb5bb5ac5ed993aaff0c410c"; // TODO: remove this.
		
		UploadTasks remote = new UploadTasks(tasks, userHash);
		remote.execute(new String[] { url });
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

		protected void onProgressUpdate(Integer... progress) {
			Log.i("#TASKSERVICE", " Progress: " + progress[0]);
		}

		protected void onPostExecute(ArrayList<Task> tasks) {
			Log.i("#TASKSERVICE", "DoPostExecute!");
		}
	}



	/**
	 * Async object implemetation to PostTasks to server
	 * 
	 * @param String... urls
	 *            URL's that will called.
	 * @author Paulo Luan 
	 */
	private class UploadTasks extends AsyncTask<String, Void, ArrayList<Task>> {

		private List<Task> tasks;
		private String userHash;

		public UploadTasks() {}

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

		protected void onProgressUpdate(Integer... progress) {
			Log.i("#TASKSERVICE", " Progress: " + progress[0]);
		}

		protected void onPostExecute(ResponseEntity response) {
			HttpStatus status = response.getStatusCode();
			Log.i("#TASKPOSTSERVICE", "DoPostExecute!");
		}
	}


}