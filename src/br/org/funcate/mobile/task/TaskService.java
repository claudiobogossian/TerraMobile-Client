package br.org.funcate.mobile.task;

import java.util.ArrayList;

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
	
	public Task getTasks(){
        Task task = restTemplate.getForObject("http://url", Task.class);
        return task;
	}
	
	public Boolean saveTasks(ArrayList<Task> tasks){
		return true;
	}
	
	public Boolean updateTask(Task task){
		this.restTemplate.put("http://url", task);
		return true;
	}

}
