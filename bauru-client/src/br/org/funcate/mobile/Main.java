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
import br.org.funcate.mobile.map.GeoMap;
import br.org.funcate.mobile.user.LoginActivity;
import br.org.funcate.mobile.user.SessionManager;

public class Main extends Activity {

	private Main self = this;
	public static final String TAG = "#MAIN";

	// other activities
	private static final int GEOMAP = 100;
	
	// Session Manager Class
    SessionManager session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        session = new SessionManager(getApplicationContext());
        this.checkLogin();
		super.onCreate(savedInstanceState);
		config();
	}	

	/**
	 * Check login method wil check user login status If false it will redirect
	 * user to login page Else won't do anything
	 * */
	public void checkLogin() {
		
		boolean isLoggedIn = session.isLoggedIn();
		Intent intent = null;
		
		if (isLoggedIn) {
			intent = new Intent(this, GeoMap.class);
		} else {
			// user is not logged in redirect him to Login Activity
			intent = new Intent(this, LoginActivity.class);
		}
		
		// Closing all the Activities
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// Add new Flag to start new Activity
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		// Staring Login Activity
		this.startActivity(intent);
		finish();
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
