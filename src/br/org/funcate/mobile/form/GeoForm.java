package br.org.funcate.mobile.form;

import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import br.org.funcate.mobile.AddressAdapter;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.data.Provider;
import br.org.funcate.mobile.data.ProviderAddress;
import br.org.funcate.mobile.photo.Photo;

public class GeoForm extends Activity {

	private static final String TAG = "#GEOFORM";

	// other activities
	private static final int PHOTO = 102;

	// widgets
	private AutoCompleteTextView log;
	private EditText cep, num, cit, est, if1, if2;
	private TextView lat, lon;
	private ImageButton bt_clear;
	private Button bt_cancel, bt_ok, bt_photo;
	private String fot;
	private Date dat = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_geoform);

		Location currentLocation = getIntent().getExtras().getParcelable(
				"CURRENT_LOCATION");

		// iniciate providers
		Cursor c_ini2 = getContentResolver().query(
				ProviderAddress.Lograd.CONTENT_URI,
				new String[] { "count(*) AS count" }, null, null, null);
		c_ini2.moveToFirst();
		c_ini2.close();
		c_ini2 = null;

		// linking widgets
		log = (AutoCompleteTextView) findViewById(R.id.cp_log);
		cep = (EditText) findViewById(R.id.cp_cep);
		num = (EditText) findViewById(R.id.cp_num);
		lat = (TextView) findViewById(R.id.cp_lat);
		lon = (TextView) findViewById(R.id.cp_lon);
		cit = (EditText) findViewById(R.id.cp_cit);
		est = (EditText) findViewById(R.id.cp_est);
		if1 = (EditText) findViewById(R.id.cp_if1);
		if2 = (EditText) findViewById(R.id.cp_if2);
		bt_clear = (ImageButton) findViewById(R.id.cp_button_clear);
		bt_cancel = (Button) findViewById(R.id.cp_button_cancel);
		bt_ok = (Button) findViewById(R.id.cp_button_ok);
		bt_photo = (Button) findViewById(R.id.cp_button_photo);

		lat.setText("" + currentLocation.getLatitude());
		lon.setText("" + currentLocation.getLongitude());

		log.setAdapter(new AddressAdapter(this, R.layout.item_list, null,
				new String[] { ProviderAddress.Lograd.LOG,
						ProviderAddress.Lograd.CEP }, new int[] { R.id.itemlog,
						R.id.itemcep }));
		log.setHint("pesquisar...");

		log.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				try {
					TextView t1 = (TextView) arg1.findViewById(R.id.itemlog);
					String logvalue = t1.getText().toString();
					log.setText(logvalue);
					TextView t2 = (TextView) arg1.findViewById(R.id.itemcep);
					String[] split1 = t2.getText().toString().split(" ");
					String cepvalue;
					if (split1[1].compareTo("---") == 0) {
						cepvalue = "";
					} else {
						cepvalue = split1[1];
					}
					cep.setText(cepvalue);

					log.setInputType(InputType.TYPE_NULL);
					cep.setInputType(InputType.TYPE_NULL);

					log.clearFocus();
					log.setEnabled(false);
					cep.setEnabled(false);

					InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					mgr.hideSoftInputFromWindow(log.getWindowToken(), 0);

				} catch (Exception ex) {
					Log.e(TAG, "Exception onItemClick: " + ex);
				}
			}
		});

		bt_clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				log.setText("");
				cep.setText("");
				log.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
				cep.setInputType(InputType.TYPE_NULL);
				log.setEnabled(true);
				cep.setEnabled(false);
				log.requestFocus();
			}
		});

		bt_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED,
						new Intent().putExtra("RESULT", "CANCEL"));
				finish();
			}
		});

		bt_photo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(GeoForm.this, Photo.class);
				startActivityForResult(i, PHOTO);
			}
		});

		bt_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (log.getText().toString().compareTo("") == 0) {
					log.requestFocus();
				} else {
					validate();
				}
			}

			private void validate() {
				// save database
				ContentValues row = new ContentValues();

				row.put(Provider.Dados.LOG, log.getText().toString());
				row.put(Provider.Dados.CEP, cep.getText().toString());
				row.put(Provider.Dados.NUM, num.getText().toString());
				row.put(Provider.Dados.CID, cit.getText().toString());
				row.put(Provider.Dados.EST, est.getText().toString());
				row.put(Provider.Dados.IF1, if1.getText().toString());
				row.put(Provider.Dados.IF2, if2.getText().toString());
				row.put(Provider.Dados.FOT, fot);
				row.put(Provider.Dados.LAT, lat.getText().toString());
				row.put(Provider.Dados.LON, lon.getText().toString());
				if (dat != null) {
					row.put(Provider.Dados.DAT, dat.toString());
				}

				getContentResolver().insert(Provider.Dados.CONTENT_URI, row);

				Intent data = new Intent();
				data.putExtra("RESULT", "Registro concluído!");
				setResult(RESULT_OK, data);

				finish();
			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO) {
			if (resultCode == RESULT_OK) {
				fot = getIntent().getExtras().getString("RESULT");
				dat = new Date();
			} else if (resultCode == RESULT_CANCELED) {
			}
		}
	}
}
