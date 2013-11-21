package br.org.funcate.baurudigital.server.user;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.type.TypeReference;

import br.org.funcate.baurudigital.server.common.exception.UtilException;
import br.org.funcate.baurudigital.server.common.service.JSONService;

@XmlRootElement
@Path("/users")
@Produces({MediaType.APPLICATION_JSON })
public class UserRestService
{

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getAllUsers() {

		List<User> users = UserService.getUsers();		
		return users;

	}
	
	@GET
	@Path("/populatedb")
	public void populateDatabase() throws UtilException
	{
		List<User> users = new ArrayList<User>();
		users = (List<User>) JSONService.parseJSON("/home/bogo/workspace-bauru/bauru-digital/bauru-server/src/main/resources/users.json", new TypeReference<List<User>>() {});
		UserService.populateTestUsers(users);
		
	}
	
}