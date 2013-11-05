package br.org.funcate.mobile.job;

/**
 * 
 * Control the transactions between Service and DAO.
 * 
 * */
public class JobController {

	private JobService service;
	private JobDao dao;

	public JobController() {
		service = new JobService();
		dao = new JobDao();
	}

	public Boolean getAllJobs() {
		return true;
	}

	public Boolean sendJobs() {
		return true;
	}

	public Boolean sendJob(Job job) {
		return true;
	}

	public Boolean clearAlljobs() {
		return true;
	}

}
