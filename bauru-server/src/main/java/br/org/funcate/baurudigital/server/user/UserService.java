package br.org.funcate.baurudigital.server.user;

import java.util.ArrayList;
import java.util.List;

import br.org.funcate.baurudigital.server.common.exception.UtilException;
import br.org.funcate.baurudigital.server.common.utils.Util;

/**
 * Keep's state less functions as a service class and comunicate with DAO and
 * others entity's service.
 * 
 * @author bogo
 * 
 */
public class UserService {
	/**
	 * List all users
	 * 
	 * @return
	 * @throws UtilException 
	 */
	public static List<User> getUsers() throws UtilException {
		List<User> users = new UserDAO().retrieve();
		updateUsersHash(users);
		return users;
	}

	public static User getUserByHash(String hash) throws UserException {
		return new UserDAO().retrieve(hash);
	}

	/**
	 * Test only
	 * 
	 * @param users
	 * @throws UtilException
	 */
	public static void populateTestUsers(List<User> users) throws UtilException {
		new UserDAO().save(users);
	}

	public static void updateUsersHash(List<User> users) throws UtilException {
		List<User> usersWithoutHash = new ArrayList<User>();
		for (User user : users) {
			if (user.getHash() == null) {
				String userHash = Util.generateHashMD5(user.getLogin()+user.getPassword());
				user.setHash(userHash);
				usersWithoutHash.add(user);
			}
		}
		new UserDAO().save(usersWithoutHash);
	}

}
