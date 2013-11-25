package br.org.funcate.baurudigital.server.common.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import br.org.funcate.baurudigital.server.user.User;
/**
 * This class keeps state less JSON functions and util, as a service with static methods
 * @author bogo
 *
 */
public class JSONService {
	/**
	 * Allow parsing a file with JSON content into a Java Object with the same structure of the JSON objects.
	 * @param jsonFile Path to File to be parsed
	 * @param type Type of the object do be mapped the JSON file. 
	 * @return parsed object
	 */
	public static Object parseJSON(String jsonFile, TypeReference type)
	{
		ObjectMapper mapper = new ObjectMapper();
		List<User> users=null;
		try {
			
			users = mapper.readValue(new File(jsonFile), type);
			
		} catch (JsonGenerationException e) {
	 
			e.printStackTrace();
	 
		} catch (JsonMappingException e) {
	 
			e.printStackTrace();
	 
		} catch (IOException e) {
	 
			e.printStackTrace();
	 
		}
		return users;
		
	}
	/**
	 * Allow parsing a object into a JSON File
	 * @param jsonFile File path to be saved
	 * @param object Object to be parsed
	 */
	public static void parse2JSON(String jsonFile, Object object)
	{
		ObjectMapper mapper = new ObjectMapper();
		try {
			
			mapper.writeValue(new File(jsonFile), object);
			
		} catch (JsonGenerationException e) {
	 
			e.printStackTrace();
	 
		} catch (JsonMappingException e) {
	 
			e.printStackTrace();
	 
		} catch (IOException e) {
	 
			e.printStackTrace();
	 
		}
	}
}
