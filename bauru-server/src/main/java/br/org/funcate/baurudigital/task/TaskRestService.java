package br.org.funcate.baurudigital.task;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.type.TypeReference;

import br.org.funcate.baurudigital.common.service.JSONService;
import br.org.funcate.baurudigital.user.User;
import br.org.funcate.baurudigital.user.UserException;

@XmlRootElement
@Path("/tasks")
@Produces({MediaType.APPLICATION_JSON })
public class TaskRestService
{

	@GET
	@Path("/get")
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
	
	@GET
	@Path("/populatedb")
	public void populateDatabase()
	{
		
	//TaskService.taskDecoderTest();
		List<Task> tasks = new ArrayList<Task>();
	     tasks = (List<Task>) JSONService.parseJSON("/home/bogo/workspace-bauru/bauru-digital/bauru-server/src/main/resources/tasks.json", new TypeReference<List<Task>>() {});
		TaskService.saveTasks(tasks);
		
	}
	
	@POST
	@Path("/save")
	@Produces(MediaType.APPLICATION_JSON)
	public void saveTasks(List<Task> tasks, @QueryParam("user") String userHash) throws TaskException
	{
		if (userHash==null) 
		{
			throw new TaskException("Favor informar o usuário para salvar suas tarefas.");
		}
		if(!tasks.isEmpty())
		{
			saveTasks(tasks, userHash);
		}
	}
		
}