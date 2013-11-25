package br.org.funcate.baurudigital.server.user;

import java.util.List;

import br.org.funcate.baurudigital.server.common.exception.UtilException;
/**
 * Keep's state less functions as a service class and comunicate with DAO and others entity's service.  
 * @author bogo
 *
 */
public class UserService {
	/**
	 * List all users
	 * @return
	 */
	public static List<User> getUsers()
	{
		return new UserDAO().retrieve();
	}
	public static User getUserByHash(String hash) throws UserException
	{
		return new UserDAO().retrieve(hash);
	}
	/**
	 * Test only
	 * @param users
	 * @throws UtilException
	 */
	public static void populateTestUsers(List<User> users) throws UtilException
	{
		new UserDAO().save(users);
	}
	
}
