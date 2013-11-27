package br.org.funcate.baurudigital.server.task;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import br.org.funcate.baurudigital.server.user.UserException;
/**
 * Keep's functions that are mapped throw http rest service
 * Works only with Task entity
 * @author bogo
 *
 */
@XmlRootElement
@Path("/tasks")
@Produces({MediaType.APPLICATION_JSON })
public class TaskRestService
{

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Task> getTasks(@QueryParam("user") String userHash) throws TaskException
	{
		if (userHash==null) 
		{
			throw new TaskException("Favor informar o usuário para obter as lista de tarefas.");
		}
		List<Task> tasks = TaskService.getTask(userHash);	
		return tasks; 
	}
	

	@POST
	public Response saveTasks(List<Task> tasks, @QueryParam("user") String userHash) throws TaskException, UserException
	{
		if (userHash==null) 
		{
			throw new TaskException("Favor informar o usuário para salvar suas tarefas.");
		}
		if(!tasks.isEmpty())
		{
			TaskService.saveTasks(tasks, userHash);
		}
		return Response.status(201).entity("luanzito").build();

	}
		
}