package br.org.funcate.mobile.job;

/**
 * 
 * Control the transactions, control the transactions between Service and DAO.
 * 
 * */
public class JobController {

	private JobService service = new JobService();
	private JobDao dao = new JobDao();

	public JobController() {
		// TODO Auto-generated constructor stub
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
