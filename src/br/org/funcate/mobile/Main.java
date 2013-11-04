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
import br.org.funcate.mobile.map.GeoMap;

public class Main extends Activity {

	public static final String TAG = "#MAIN";

	// other activities
	private static final int GEOMAP = 100;

	// widgets
	private Button bt_begin, bt_exit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		// linking the widgets to the layout
		bt_begin = (Button) findViewById(R.id.main_bt_begin);
		bt_exit = (Button) findViewById(R.id.main_bt_exit);

		bt_begin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
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

	@SuppressLint("SdCardPath")
	private void config() {
		try {
			InputStream is = getAssets().open("address.db");
			File archive = new File("/data/data/" + getPackageName()
					+ "/files/databases/");
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
			Log.e(TAG, "Erro de configuração de endereço: " + ex);
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
