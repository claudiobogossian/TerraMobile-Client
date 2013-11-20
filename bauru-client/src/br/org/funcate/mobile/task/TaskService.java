package br.org.funcate.mobile.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.util.Log;
import br.org.funcate.mobile.Utility;

/**
 * 
 * Service REST. Ajax calls to get, post and put objects to server.
 * 
 * */
public class TaskService {

	private RestTemplate restTemplate;

	public TaskService() {
		this.restTemplate = new RestTemplate();
		this.restTemplate.getMessageConverters().add(
				new MappingJackson2HttpMessageConverter());
	}

	public void getTasks() {		
		String url = "http://192.168.5.60:8080/bauru-server/rest/tasks/get?user={user_hash}";
		DownloadTasks remote = new DownloadTasks();
		remote.execute(new String[] { url });
	}

	public void saveTasks() {
		String url = "http://192.168.5.60:8080/bauru-server/rest/tasks/get?user={user_hash}";
		UploadTasks remote = new UploadTasks();
		remote.execute(new String[] { url });
	}

	private class DownloadTasks extends AsyncTask<String, Void, ArrayList<Task>> {

		@Override
		protected ArrayList<Task> doInBackground(String... urls) {
			ArrayList<Task> list = null;

			for (String url : urls) {
				try {
					//String hash = Utility.generateHashMD5("123");
					String hash = "5e292159bb5bb5ac5ed993aaff0c410c";
					ResponseEntity<Task[]> response = restTemplate.getForEntity(url, Task[].class, hash);
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

	private class UploadTasks extends AsyncTask<String, Void, ArrayList<Task>> {
		@Override
		protected ArrayList<Task> doInBackground(String... urls) {
			ArrayList<Task> list = null;

			for (String url : urls) {
				try {
					String hash = Utility.generateHashMD5("123");
					List<Task> tasks = null; // TODO: pegar do Banco todas as consultas marcadas como true.
					Task returnedTask = restTemplate.postForObject(url, tasks, Task.class);
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


}