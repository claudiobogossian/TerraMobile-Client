package br.org.funcate.baurudigital.test;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import br.org.funcate.baurudigital.common.exception.UtilException;
import br.org.funcate.baurudigital.common.service.JSONService;
import br.org.funcate.baurudigital.task.Task;
import br.org.funcate.baurudigital.task.TaskService;
import br.org.funcate.baurudigital.user.User;
import br.org.funcate.baurudigital.user.UserService;

public class PopulateDatabase {

	/**
	 * @param args
	 * @throws UtilException 
	 */
	public static void main(String[] args) throws UtilException {
		// TODO Auto-generated method stub
		
/*		TaskService.taskDecoderTest();

		System.exit(1);*/
		
		List<User> users = new ArrayList<User>();
		users = (List<User>) JSONService
				.parseJSON(
						"/home/bogo/workspace-bauru/bauru-digital/bauru-server/src/main/resources/users.json",
						new TypeReference<List<User>>() {
						});
		UserService.populateTestUsers(users);

		List<Task> tasks = new ArrayList<Task>();
		tasks = (List<Task>) JSONService
				.parseJSON(
						"/home/bogo/workspace-bauru/bauru-digital/bauru-server/src/main/resources/tasks.json",
						new TypeReference<List<Task>>() {
						});
		TaskService.populateTasks(tasks);

	}

}
