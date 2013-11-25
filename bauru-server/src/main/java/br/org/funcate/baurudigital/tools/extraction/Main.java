package br.org.funcate.baurudigital.tools.extraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.vividsolutions.jts.io.ParseException;

import br.org.funcate.baurudigital.server.address.Address;
import br.org.funcate.baurudigital.server.common.exception.UtilException;
import br.org.funcate.baurudigital.server.common.service.JSONService;
import br.org.funcate.baurudigital.server.form.Form;
import br.org.funcate.baurudigital.server.task.Task;
import br.org.funcate.baurudigital.server.task.TaskException;
import br.org.funcate.baurudigital.server.task.TaskService;
import br.org.funcate.baurudigital.server.user.User;
import br.org.funcate.baurudigital.server.user.UserService;
import br.org.funcate.baurudigital.tools.extraction.domain.DAO.SourceAddressDAO;
/**
 * This class was created to extract data from original database to this application model database
 * @author bogo
 *
 */
public class Main {

	/**
	 * @param args
	 * @throws TaskException 
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws UtilException 
	 */
	public static void main(String[] args) throws TaskException, IOException, ParseException, UtilException {
		
		List<User> users = new ArrayList<User>();
		users = (List<User>) JSONService.parseJSON("/home/bogo/workspace-bauru/bauru-digital/bauru-server/src/main/resources/users.json", new TypeReference<List<User>>() {});
		UserService.populateTestUsers(users);
		
		
		users = UserService.getUsers();
		String blockId1 = "020375"; 
		String blockId2 = "020389";
		String blockId3 = "020622";

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
		
		List<Address> addressList3 =  new SourceAddressDAO().getAddressByBlock(blockId3);

		for (Address address : addressList3) {
			Task task = new Task();
			task.setAddress(address);
			task.setForm(new Form());
			task.setSyncronized(false);
			task.setUser(users.get(2));
			tasks.add(task);
		}
		TaskService.saveTasks(tasks, null);
		/**
		 * Query para escolher as melhores quadras
		 * select count(substring(imo_inscricao, 1,6)), substring(imo_inscricao, 1,6)  from view_fct_endereco_imovel_bogo where numero <> '0-0' group by substring(imo_inscricao, 1,6)
		 */
	}

}
