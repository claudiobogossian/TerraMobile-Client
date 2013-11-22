package br.org.funcate.baurudigital.user;

import java.util.List;

import br.org.funcate.baurudigital.common.exception.UtilException;

public class UserService {

	public static List<User> getUsers()
	{
		return new UserDAO().getAll();
	}
	public static User getUserByHash(String hash) throws UserException
	{
		return new UserDAO().getUserByHash(hash);
	}
	public static void populateTestUsers(List<User> users) throws UtilException
	{
		new UserDAO().populate(users);
	}
	
}
