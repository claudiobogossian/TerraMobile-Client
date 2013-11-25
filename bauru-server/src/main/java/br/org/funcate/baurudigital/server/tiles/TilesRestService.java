package br.org.funcate.baurudigital.server.tiles;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
	public Response getTiles() throws ConfigException, TilesException {
	
		String tilesZipPath = PropertiesReader.getProperty("tiles.file.path");
		File tilesZip = new File(tilesZipPath);
		if(!tilesZip.exists())
		{
			throw new TilesException("Não foi possível localizar arquivo compactado com os tiles. Verifique as configurações.");
		}
        ResponseBuilder response = Response.ok((Object) tilesZip);
        response.header("Content-Disposition", "attachment; filename=\"tiles.zip\"");
		return response.build();

	}
	
}