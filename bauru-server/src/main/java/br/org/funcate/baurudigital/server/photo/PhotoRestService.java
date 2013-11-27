package br.org.funcate.baurudigital.server.photo;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import br.org.funcate.baurudigital.server.form.FormException;
/**
 * Keep's functions that are mapped throw http rest service
 * Works only with Photo entity
 * @author bogo
 *
 */
@XmlRootElement
@Path("/photos")
public class PhotoRestService
{
	@POST
	public Response savePhotos(List<Photo> photos, @QueryParam("user") String userHash) throws PhotoException 
	{
		if (userHash==null) 
		{
			throw new PhotoException("Favor informar o usuário para salvar suas tarefas.");
		}
		if (!photos.isEmpty()) {
			PhotoService.savePhotos(photos, userHash);	
		}
		return Response.status(201).entity("luanzito").build();
		
	}
	@GET
	@Path("/getDisk")
	@Produces(MediaType.APPLICATION_JSON)
	public Photo retrieveTest() throws IOException, FormException 
	{
		Photo photo = PhotoService.retrievePhotoFromDisk();
		
		return photo;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Photo retrieve(@QueryParam("id") int id) throws PhotoException, IOException 
	{
		Photo photo = PhotoService.retrieve(id);
	
		return photo;
	}
	
		
		
}