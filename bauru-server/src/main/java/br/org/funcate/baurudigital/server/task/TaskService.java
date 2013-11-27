package br.org.funcate.baurudigital.server.task;

import java.util.ArrayList;
import java.util.List;

import br.org.funcate.baurudigital.server.address.Address;
import br.org.funcate.baurudigital.server.common.service.JSONService;
import br.org.funcate.baurudigital.server.form.Form;
import br.org.funcate.baurudigital.server.user.User;
import br.org.funcate.baurudigital.server.user.UserException;
import br.org.funcate.baurudigital.server.user.UserService;
/**
 * Keep's state less functions as a service class and comunicate with DAO and others entity's service.  
 * @author bogo
 *
 */
public class TaskService {
	/**
	 * List tasks for user Hash 
	 * @param hash User hash
	 * @return
	 * @throws TaskException
	 */
	public static List<Task> getTask(String hash) throws TaskException
	{
		User user;
		try {
			user = UserService.getUserByHash(hash);
		} catch (UserException e) {
			throw new TaskException("Não foi possível obter as task solicitadas pois o usuário solicitado não existe.", e);
		}
		
		return new TaskDAO().retrieve(user);
	}
	/**
	 * Save tasks if user is valid 
	 * @param tasks
	 * @param userHash
	 * @throws TaskException
	 */
	public static void saveTasks(List<Task> tasks, String userHash) throws TaskException 
	{
		//Remover clausula em produção
		if(userHash!=null)
		{
			User user;
			try {
				user = UserService.getUserByHash(userHash);
			} catch (UserException e) {
				throw new TaskException(
						"Não foi possível obter as task solicitadas pois o usuário solicitado não existe.",
						e);
			}
		}
		new TaskDAO().save(tasks);
	}
	/**
	 * Only for testing
	 */
	public static void taskDecoderTest()
	{
		List<Task> tasks = new ArrayList<Task>();
		Task task = new Task();
		task.setAddress(new Address());
		task.getAddress().setCity("SJCampos");
		task.getAddress().setCoordx(0.1);
		task.getAddress().setCoordy(0.1);
		task.getAddress().setExtra("Casa 23");
		task.getAddress().setFeatureId("1");
		//task.getAddress().setId(1);
		task.getAddress().setName("Estrada Velha Rio-São Paulo");
		task.getAddress().setNumber("4850");
		task.getAddress().setPostalCode("12247-001");
		task.getAddress().setState("SP");
		task.setForm(new Form());
		//task.getForm().setId(1);
		//task.setId(1);
		task.setDone(false);
		task.setUser(new User());
		task.getUser().setId(1);
		
		tasks.add(task);
		
		JSONService.parse2JSON("/home/bogo/workspace-bauru/bauru-digital/bauru-server/src/main/resources/tasks.json", tasks);
	}
	
}
