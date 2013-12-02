package br.org.funcate.baurudigital.tools.extraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

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

import com.vividsolutions.jts.io.ParseException;

/**
 * This class was created to extract data from original database to this
 * application model database
 * 
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
	 * @throws FactoryException
	 * @throws TransformException
	 * @throws MismatchedDimensionException
	 */
	public static void main(String[] args) throws TaskException, IOException,
			ParseException, UtilException, MismatchedDimensionException,
			TransformException, FactoryException {
		
		new SourceAddressDAO().dropAllContent();

		List<User> users = new ArrayList<User>();
		users = (List<User>) JSONService
				.parseJSON(
						"/home/bogo/workspace-bauru/bauru-digital/bauru-server/src/main/resources/users.json",
						new TypeReference<List<User>>() {
						});
		UserService.populateTestUsers(users);

		users = UserService.getUsers();
		
		
		String[] blocks = {"020375",
				"020389",
				"020622",
				"010022",
				"010061"};
		
		ArrayList<String> blocks2 = new ArrayList<String>(Arrays.asList(blocks));
			
		ArrayList<Task> tasks = new ArrayList<Task>();
		
		
		
		for (User user : users) {
			
			Collections.shuffle(blocks2);
			
			if(blocks2.size()==0)
			{
				break;
			}
			
			String block = blocks2.get(0);
			
			blocks2.remove(0);
			
			if (block==null) {
				continue;
			}
			
			// Primeira quadra - Primeiro Usuário
			List<Address> addressList = new SourceAddressDAO()
					.getAddressByBlock(block);
			for (Address address : addressList) {
				Task task = new Task();
				task.setAddress(address);
				task.setForm(new Form());
				task.setDone(false);
				task.setUser(user);
				tasks.add(task);
			}
		}
		

		TaskService.saveTasks(tasks, null);

		/*
		 * Query para escolher as melhores quadras select count(substring(imo_inscricao, 1,6)), substring(imo_inscricao, 1,6)  from view_fct_endereco_imovel_bogo where numero <> '0-0' group by substring(imo_inscricao, 1,6)
		 */
		/*
		 * List<User> users = UserService.getUsers(); for (User user : users) {
		 * List<Task> tasks = TaskService.getTask(user.getHash()); for (Task
		 * task : tasks) {
		 * 
		 * if (!task.getAddress().getCoordx().isNaN() &&
		 * !task.getAddress().getCoordy().isNaN()) {
		 * 
		 * System.out.println(task.getAddress().getCoordx() + " - " +
		 * task.getAddress().getCoordy()); GeometryBuilder gb = new
		 * GeometryBuilder(); Point point =
		 * gb.point(task.getAddress().getCoordx(), task
		 * .getAddress().getCoordy());
		 * 
		 * CoordinateReferenceSystem toCRS = CRS.decode("EPSG:4326");
		 * CoordinateReferenceSystem fromCRS = CRS .decode("EPSG:29192");
		 * boolean lenient = true; // allow for some error due to // different
		 * datums MathTransform transform = CRS.findMathTransform(fromCRS,
		 * toCRS, lenient); Point p = (Point) JTS.transform(point, transform);
		 * System.out.println(p.getCentroid().getX());
		 * System.out.println(p.getCentroid().getY());
		 * 
		 * task.getAddress().setCoordx(p.getCentroid().getX());
		 * task.getAddress().setCoordy(p.getCentroid().getY()); } }
		 * 
		 * TaskService.saveTasks(tasks, user.getHash()); }
		 */

	}
}
