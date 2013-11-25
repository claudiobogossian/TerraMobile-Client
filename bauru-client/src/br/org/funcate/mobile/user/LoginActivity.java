package br.org.funcate.mobile.user;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.Utility;
import br.org.funcate.mobile.database.DatabaseAdapter;
import br.org.funcate.mobile.database.DatabaseHelper;
import br.org.funcate.mobile.map.GeoMap;

import com.j256.ormlite.dao.Dao;

public class LoginActivity extends Activity {
	// tag used to debug
	private final String LOG_TAG = "#" + getClass().getSimpleName();

	// widgets
	private Button bt_begin, bt_exit;

	// this instance
	private LoginActivity self = this;

	// session Manager
	private SessionManager session;

	private static ProgressDialog dialog;

	private RestTemplate restTemplate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		
		session = new SessionManager(getApplicationContext());

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);

		// linking the widgets to the layout
		bt_begin = (Button) findViewById(R.id.main_bt_begin);
		bt_exit = (Button) findViewById(R.id.main_bt_exit);

		bt_begin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.showLoadingMask();
				self.getRemoteUsers();
			}
		});

		bt_exit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED, new Intent());
				finish();
			}
		});

		this.restTemplate = new RestTemplate();
		this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
	}

	/**
	 * Login button click event. A Toast is set to alert when the Email and
	 * Password field is empty
	 **/
	public void login() {
		EditText txt_login_username = (EditText) findViewById(R.id.txt_login_username);
		EditText txt_login_password = (EditText) findViewById(R.id.txt_login_password);

		String login = txt_login_username.getText().toString();
		String password = txt_login_password.getText().toString();

		if ((!login.equals("")) && (!password.equals(""))) {
			String passHash = Utility.generateHashMD5(password);
			String userHash = Utility.generateHashMD5(login + passHash);

			if(this.isValidHash(userHash)) {
				session.createLoginSession(login, userHash);
				Intent i = new Intent(this, GeoMap.class);
				startActivity(i);
				finish();
			} else {
				Toast.makeText(getApplicationContext(), "Usuário ou senha inválidos", Toast.LENGTH_LONG).show();
			}
		} else if ((!login.equals(""))) {
			Toast.makeText(getApplicationContext(), "Preencha a senha!", Toast.LENGTH_SHORT).show();
		} else if ((!password.equals(""))) {
			Toast.makeText(getApplicationContext(), "Preencha o nome de usuário!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), "Preencha Nome de usuário e Senhas!", Toast.LENGTH_SHORT).show();
		}
		
		self.hideLoadMask();
	}


	/**
	 * Verify in the database if exists any users with that hash
	 * 
	 * @author Paulo Luan
	 * @param hash
	 *            The Hash of user, this hash is the hash of (name +
	 *            (hash(password)))
	 * 
	 */
	public boolean isValidHash(String hash){
		boolean userExists = false;
		// verify hash at local database.
		DatabaseAdapter db = DatabaseHelper.getDatabase();
		Dao<User, Integer> dao = db.getUserDao();

		try {
			List<User> users = dao.queryForAll();
			User user = dao.queryBuilder().where().eq("hash", hash).queryForFirst();

			if(user != null){
				userExists = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return userExists;
	}

	/**
	 * Creates the object that send the rest calls to server.
	 * 
	 * @author Paulo Luan
	 * 
	 */
	public void getRemoteUsers() {
		if(Utility.isNetworkAvailable(this)){
			String url = "http://192.168.5.60:8080/bauru-server/rest/users";
			DownloadUsers remote = new DownloadUsers();
			remote.execute(new String[] { url });
		} else {
			self.login();
			Log.i(this.LOG_TAG, "Sem conexão com a internet.");
		}
	}

	/**
	 * Save a list of users into local database.
	 * 
	 * @author Paulo Luan
	 * 
	 * @param List<User> 
	 *            Users that will be saved into database.
	 */
	public void saveUsersIntoLocalSqlite(List<User> users) {
		DatabaseAdapter db = DatabaseHelper.getDatabase();
		Dao<User, Integer> dao = db.getUserDao();

		if(users != null) {
			try {
				db.resetUserTable(); // clear all registers
				for (User user : users) {
					dao.create(user);
				}				
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
			self.login();
		}
	}

	public void showLoadingMask() {
		dialog = ProgressDialog.show(this, "", "Carregando, aguarde...", true);
	}

	public void showLoadingMask(String message) {
		dialog = ProgressDialog.show(this, "", message, true);
	}

	public void hideLoadMask() {
		dialog.hide();
	}
}
