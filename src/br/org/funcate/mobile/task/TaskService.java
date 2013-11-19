package br.org.funcate.mobile.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import br.org.funcate.mobile.Utility;

import android.os.AsyncTask;
import android.util.Log;

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
		DownloadRemoteTasks remote = new DownloadRemoteTasks();
	    remote.execute(new String[] { url });
	}

	public Boolean saveTasks(List<Task> tasks) {
		// ajax call
		String url = "";
		Task returnedTask = restTemplate.postForObject(url, tasks, Task.class);
		return true;
	}

	private class DownloadRemoteTasks extends AsyncTask<String, Void, ArrayList<Task>> {
		
		@Override
		protected ArrayList<Task> doInBackground(String... urls) {
			ArrayList<Task> list = null;
			
			for (String url : urls) {
				try {
					String hash = Utility.generateHashMD5("123");
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

}