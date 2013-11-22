package br.org.funcate.baurudigital.server.user;

import java.util.List;

import br.org.funcate.baurudigital.server.common.exception.UtilException;

public class UserService {

	public static List<User> getUsers()
	{
		return new UserDAO().retrieve();
	}
	public static User getUserByHash(String hash) throws UserException
	{
		return new UserDAO().retrieve(hash);
	}
	public static void populateTestUsers(List<User> users) throws UtilException
	{
		new UserDAO().save(users);
	}
	
}
