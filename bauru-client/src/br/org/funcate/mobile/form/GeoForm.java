package br.org.funcate.mobile.form;

import java.sql.SQLException;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.address.Address;
import br.org.funcate.mobile.data.AddressAdapter;
import br.org.funcate.mobile.data.DatabaseAdapter;
import br.org.funcate.mobile.data.DatabaseHelper;
import br.org.funcate.mobile.photo.PhotoActivity;
import br.org.funcate.mobile.task.Task;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class GeoForm extends Activity implements LocationListener{

	// tag used to debug
	private final String LOG_TAG = "#" + getClass().getSimpleName();

	// other activities
	private static final int PHOTO = 102;

	private Task task;
	
	private Location currentLocation = null;
	
	// widgets
	private AutoCompleteTextView log;
	private EditText cep, num, cit, est, if1, if2;
	private TextView lat, lon;
	private ImageButton bt_clear;
	private Button bt_cancel, bt_ok, bt_photo;
	private String photoPath;
	private Date dat = null;
	private LocationManager locationManager;

	private GeoForm self = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_geoform);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		
		task = (Task) getIntent().getSerializableExtra("task");

		try {
			currentLocation = getIntent().getExtras().getParcelable("CURRENT_LOCATION");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
		
		if(task != null){
			cep.setText(task.getAddress().getPostalCode());
			num.setText(task.getAddress().getNumber());
			cit.setText(task.getAddress().getCity());
			est.setText(task.getAddress().getState());
			if1.setText(task.getForm().getInfo1());
			if2.setText(task.getForm().getInfo2());
		}

		bt_clear = (ImageButton) findViewById(R.id.cp_button_clear);
		bt_cancel = (Button) findViewById(R.id.cp_button_cancel);
		bt_ok = (Button) findViewById(R.id.cp_button_ok);
		bt_photo = (Button) findViewById(R.id.cp_button_photo);
		
		// Database query can be a time consuming task, so its safe to call database query in another thread
        new Handler().post(new Runnable() {
            @Override
            public void run() {
            	Cursor cursor;
            	
				try {
					cursor = AddressAdapter.getAddressCursor(null);
					self.setAutoCompleteAdapterPropertiers(cursor);
				} catch (SQLException e) {
					Log.e(self.LOG_TAG, "ERRO AO CRIAR CURSOR!" + e.getMessage());
					e.printStackTrace();
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
				setResult(RESULT_CANCELED, new Intent().putExtra("RESULT", "CANCEL"));
				finish();
			}
		});

		PackageManager packageManager = self.getPackageManager();

		// if device support camera?
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			// yes Camera
			bt_photo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(GeoForm.this, PhotoActivity.class);
					startActivityForResult(i, PHOTO);
				}
			});
		} else {
			// no Camera
			bt_photo.setEnabled(false);
			Log.i("camera", "This device has no camera!");
		}

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
				
				DatabaseAdapter db = DatabaseHelper.getDatabase();
				
				try {
					Dao<Task, Integer> dao = db.getTaskDao();
					dao.createOrUpdate(task);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				if (currentLocation != null) {
					lat.setText("" + currentLocation.getLatitude());
					lon.setText("" + currentLocation.getLongitude());
				}

				log.getText().toString();
				cep.getText().toString();
				num.getText().toString();
				cit.getText().toString();
				est.getText().toString();
				if1.getText().toString();
				if2.getText().toString();
				// photoPath;
				lat.getText().toString();
				lon.getText().toString();
				
				Intent data = new Intent();
				data.putExtra("RESULT", "Registro concluído!");
				setResult(RESULT_OK, data);

				finish();
			}
		});
	}
	
	public void setAutoCompleteAdapterPropertiers(Cursor cursor){

		AddressAdapter addressAdapter = new AddressAdapter(GeoForm.this,
				R.layout.item_list, 
				cursor,
				new String[] { "name", "postalCode" },
				new int[] {R.id.itemlog, R.id.itemcep }
		);

        log.setAdapter(addressAdapter);
		log.setHint("pesquisar...");
		log.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
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
					Log.e(LOG_TAG, "Exception onItemClick: " + ex);
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO) {
			if (resultCode == RESULT_OK) {
				photoPath = getIntent().getExtras().getString("RESULT");
				dat = new Date();
				Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location != null) {
					lat.setText("" + location.getLatitude());
					lon.setText("" + location.getLongitude());
				}
				else {
					lat.setText("Location not available");
					lon.setText("Location not available");
				}
				
			} else if (resultCode == RESULT_CANCELED) {
			}
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
}
