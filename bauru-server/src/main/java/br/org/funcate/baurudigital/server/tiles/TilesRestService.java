package br.org.funcate.baurudigital.server.tiles;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import br.org.funcate.baurudigital.server.util.ConfigException;
import br.org.funcate.baurudigital.server.util.PropertiesReader;
/**
 * Keep's functions that are mapped throw http rest service
 * Works only with Tiles entity
 * @author bogo
 *
 */
@Path("/tiles")
public class TilesRestService
{

	@GET
	@Path("/zip")
	@Produces("application/zip")
	public Response getTiles() throws ConfigException, TilesException, URISyntaxException {
	
		String tilesZipPath = PropertiesReader.getProperty("tiles.file.path");
		URL fileURL = this.getClass().getResource(tilesZipPath);
		File file = new File(fileURL.toURI());
		
		
		
        ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=\"tiles.zip\"");
        response.header("Content-Length", file.length());
        
        return response.build();

	}
	
}