package br.org.funcate.mobile.user;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import br.org.funcate.mobile.DatabaseAdapter;
import br.org.funcate.mobile.data.DatabaseHelper;

import com.j256.ormlite.dao.Dao;

public class UserService {

	private RestTemplate restTemplate;

	public UserService() {
		this.restTemplate = new RestTemplate();
		this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
	}

	public void getRemoteUsers(){
		String url = "http://192.168.5.60:8080/bauru-server/rest/users";
		DownloadUsers remote = new DownloadUsers();
		remote.execute(new String[] { url });
	}

	/**
	 * Save a list of users into local database.
	 * 
	 * @author Paulo Luan
	 * 
	 * @param List<User> 
	 *            Users that will be saved into database.
	 */
	public void saveUsersIntoLocalSqlite(List<User> users){
		DatabaseAdapter db = DatabaseHelper.getInstance().getDatabase();
		Dao<User, Integer> dao = db.getUserDao();
		
		try {
			for (User user : dao) {
				dao.create(user);
			}				
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Async class implementation to get users from server.
	 * 
	 * @author Paulo Luan
	 * 
	 * @param String... urls
	 *            URL's that will called.
	 */
	private class DownloadUsers extends AsyncTask<String, Void, ArrayList<User>> {

		@Override
		protected ArrayList<User> doInBackground(String... urls) {
			ArrayList<User> list = null;

			for (String url : urls) {
				try {					
					ResponseEntity<User[]> response = restTemplate.getForEntity(url, User[].class);
					User[] users = response.getBody();
					list = new ArrayList<User>(Arrays.asList(users));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return list;
		}

		protected void onPostExecute(ArrayList<User> users) {
			saveUsersIntoLocalSqlite(users);
		}
	}

}
