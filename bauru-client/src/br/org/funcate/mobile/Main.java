package br.org.funcate.mobile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import br.org.funcate.mobile.map.GeoMap;
import br.org.funcate.mobile.user.SessionManager;

public class Main extends Activity {

	public static final String TAG = "#MAIN";

	// other activities
	private static final int GEOMAP = 100;

	// widgets
	private Button bt_begin, bt_exit;
	
	// Session Manager Class
    SessionManager session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		 // Session class instance
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		// linking the widgets to the layout
		bt_begin = (Button) findViewById(R.id.main_bt_begin);
		bt_exit = (Button) findViewById(R.id.main_bt_exit);

		bt_begin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//self.login();
				Intent i = new Intent(Main.this, GeoMap.class);
				startActivityForResult(i, GEOMAP);
			}
		});

		bt_exit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED, new Intent());
				finish();
			}
		});

		config();
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
            session.createLoginSession(login, userHash);
			
			Intent i = new Intent(Main.this, GeoMap.class);
			startActivityForResult(i, GEOMAP);
			//fazer Try Catch pra verificar usuário ou senha inválidos...
		} else if ((!login.equals(""))) {
			Toast.makeText(getApplicationContext(), "Preencha a senha!", Toast.LENGTH_SHORT).show();
		} else if ((!password.equals(""))) {
			Toast.makeText(getApplicationContext(), "Preencha o nome de usuário!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), "Preencha Nome de usuário e Senhas!", Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressLint("SdCardPath")
	private void config() {
		try {
			InputStream is = getAssets().open("address.db");
			File archive = new File("/data/data/" + getPackageName() + "/files/databases/");
			archive.mkdirs();
			File outputFile = new File(archive, "address.db");
			@SuppressWarnings("resource")
			FileOutputStream fos = new FileOutputStream(outputFile);

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len1);
			}
		} catch (IOException ex) {
			Log.e(TAG, "Erro de configura��o de endere�o: " + ex);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GEOMAP) {
			if (resultCode == RESULT_OK) {
			} else if (resultCode == RESULT_CANCELED) {
			}
		}
	}

}
