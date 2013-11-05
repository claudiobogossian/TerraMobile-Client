package br.org.funcate.mobile.job;

import java.util.ArrayList;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * Service REST. Ajax calls to get, post and put objects to server.
 * 
 * */
public class JobService {
	
	private RestTemplate restTemplate;

	public JobService() {
		this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
	}
	
	public Job getJobs(){
        Job job = restTemplate.getForObject("http://url", Job.class);
        return job;
	}
	
	public Boolean saveJobs(ArrayList<Job> jobs){
		return true;
	}
	
	public Boolean updateJob(Job job){
		this.restTemplate.put("http://url", job);
		return true;
	}

}
