package br.org.funcate.mobile.task;

/**
 * 
 * Control the transactions between Service and DAO.
 * 
 * */
public class TaskController {

	private TaskService service;
	private TaskDao dao;

	public TaskController() {
		service = new TaskService();
		dao = new TaskDao();
	}

	public Boolean getAllTasks() {
		return true;
	}

	public Boolean sendTasks() {
		return true;
	}

	public Boolean sendTask(Task task) {
		return true;
	}

	public Boolean clearAlltasks() {
		return true;
	}

}
