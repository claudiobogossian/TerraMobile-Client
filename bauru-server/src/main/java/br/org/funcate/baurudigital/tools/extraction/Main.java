package br.org.funcate.baurudigital.tools.extraction;

import java.util.ArrayList;
import java.util.List;

import br.org.funcate.baurudigital.server.address.Address;
import br.org.funcate.baurudigital.server.form.Form;
import br.org.funcate.baurudigital.server.task.Task;
import br.org.funcate.baurudigital.server.task.TaskException;
import br.org.funcate.baurudigital.server.task.TaskService;
import br.org.funcate.baurudigital.server.user.User;
import br.org.funcate.baurudigital.server.user.UserService;
import br.org.funcate.baurudigital.tools.extraction.domain.DAO.SourceAddressDAO;

public class Main {

	/**
	 * @param args
	 * @throws TaskException 
	 */
	public static void main(String[] args) throws TaskException {
		
		List<User> users = UserService.getUsers();
		String blockId1 = "042264"; 
		String blockId2 = "042265";

		List<Task> tasks = new ArrayList<Task>();

		// Primeira quadra - Primeiro Usuário
		List<Address> addressList =  new SourceAddressDAO().getAddressByBlock(blockId1);
		for (Address address : addressList) {
			Task task = new Task();
			task.setAddress(address);
			task.setForm(new Form());
			task.setSyncronized(false);
			task.setUser(users.get(0));
			tasks.add(task);
		}
		
		
		// Segunda quadra - Segundo Usuário		
		List<Address> addressList2 =  new SourceAddressDAO().getAddressByBlock(blockId2);

		for (Address address : addressList2) {
			Task task = new Task();
			task.setAddress(address);
			task.setForm(new Form());
			task.setSyncronized(false);
			task.setUser(users.get(1));
			tasks.add(task);
		}
		TaskService.saveTasks(tasks, null);
		
	}

}
