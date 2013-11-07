package br.org.funcate.mobile.task;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * Service REST. Ajax calls to get, post and put objects to server.
 * 
 * */
public class TaskService {
	
	private RestTemplate restTemplate;

	public TaskService() {
		this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
	}
	
	public List<Task> getTasks(){
		String url = "";
		List<Task> tasks = (List<Task>) restTemplate.getForObject(url, Task.class);
        return tasks;
	}
	
	public Boolean saveTasks(ArrayList<Task> tasks){
		// ajax call
		String url = "";
		Task returnedTask = restTemplate.postForObject(url, tasks, Task.class);
		return true;
	}
	
	public Boolean updateTask(Task task){
		String url = "";
		this.restTemplate.put(url, task);
		return true;
	}

}