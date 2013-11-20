package br.org.funcate.baurudigital.user.controller.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import br.org.funcate.baurudigital.user.model.domain.User;

@XmlRootElement
@Path("/users")
@Produces({MediaType.APPLICATION_JSON })
public class UsersRestService
{

	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUsers() {

		User user = new User();
		user.setId(1);
		user.setLogin("bogo");
		user.setName("Claudio Henrique Bogossian");
		user.setPassword("12qwaszx");
		
       
		return user;

	}
	
}