package br.org.funcate.baurudigital.common.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import br.org.funcate.baurudigital.user.User;

public class JSONService {
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
