package br.org.funcate.baurudigital.test;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import br.org.funcate.baurudigital.server.common.exception.UtilException;
import br.org.funcate.baurudigital.server.common.service.JSONService;
import br.org.funcate.baurudigital.server.task.Task;
import br.org.funcate.baurudigital.server.task.TaskException;
import br.org.funcate.baurudigital.server.task.TaskService;
import br.org.funcate.baurudigital.server.user.User;
import br.org.funcate.baurudigital.server.user.UserException;
import br.org.funcate.baurudigital.server.user.UserService;

public class PopulateDatabase {

	/**
	 * @param args
	 * @throws UtilException 
	 * @throws TaskException 
	 * @throws UserException 
	 */
	public static void main(String[] args) throws UtilException, TaskException {
		// TODO Auto-generated method stub  
		
/*		TaskService.taskDecoderTest();

		System.exit(1);*/


    


	//	HibernateFactory.updateSchema();
		
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
		TaskService.saveTasks(tasks, null);
		
		

	}

}