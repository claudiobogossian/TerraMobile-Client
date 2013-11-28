package br.org.funcate.mobile.form;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Base64;
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
import android.widget.Spinner;
import android.widget.TextView;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.address.AddressAdapter;
import br.org.funcate.mobile.database.DatabaseAdapter;
import br.org.funcate.mobile.database.DatabaseHelper;
import br.org.funcate.mobile.photo.Photo;
import br.org.funcate.mobile.photo.PhotoActivity;
import br.org.funcate.mobile.photo.PhotoDao;
import br.org.funcate.mobile.task.Task;
import br.org.funcate.mobile.task.TaskDao;

import com.j256.ormlite.dao.Dao;


public class GeoForm extends Activity implements LocationListener{

	// tag used to debug
	private final String LOG_TAG = "#" + getClass().getSimpleName();

	// other activities
	private static final int PHOTO = 102;

	//private Task task;

	private Location currentLocation = null;

	// widgets
	private AutoCompleteTextView address;

	private EditText 
		edtNeighborhood, 
		edtPostalCode, 
		edtNumber, 
		edtCity, 
		edtState, 
		edtInformation1, 
		edtInformation2, 
		edtOtherNumbers;

	private Spinner 
		spnNumberConfirmation, 
		spnVariance, 
		spnPrimaryUse,
		spnSecondaryUse, 
		spnPavimentation, 
		spnAsphaltGuide, 
		spnPublicIlumination, 
		spnEnergy, 
		spnPluvialGallery;

	private TextView lat, lon;
	private ImageButton button_clear;
	private Button buttonCancel, buttonOk, buttonPhoto;
	private LocationManager locationManager;

	private GeoForm self = this;

	private Task task; 
	private List<Photo> photos;

	private DatabaseAdapter db;
	private Dao<Task, Integer> taskDao;

	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_geoform);

		db = DatabaseHelper.getDatabase();
		taskDao = db.getTaskDao();

		photos = new ArrayList<Photo>();

		task = (Task) getIntent().getSerializableExtra("task");

		try {
			currentLocation = getIntent().getExtras().getParcelable("CURRENT_LOCATION");
		} catch (Exception e) {
			e.printStackTrace();
		}

		self.mapFieldsToObjects();

		buttonPhoto.setEnabled(false);
		buttonOk.setEnabled(false);

		if(task != null){
			lat.setText("" + task.getAddress().getCoordx());
			lon.setText("" + task.getAddress().getCoordy());
			address.setText(task.getAddress().getName());
			edtNeighborhood.setText(task.getAddress().getNeighborhood());
			edtPostalCode.setText(task.getAddress().getPostalCode());
			edtNumber.setText(task.getAddress().getNumber());
			edtCity.setText(task.getAddress().getCity());
			edtState.setText(task.getAddress().getState());
			edtInformation1.setText(task.getForm().getInfo1());
			edtInformation2.setText(task.getForm().getInfo2());

			if(task.getId() != null){
				buttonOk.setEnabled(true);
			}
		}

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

		self.setButtonsListeners();
	}


	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setButtonsListeners() {

		button_clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				address.setText("");
				edtNeighborhood.setText("");
				edtPostalCode.setText("");
				address.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
				edtPostalCode.setInputType(InputType.TYPE_NULL);
				address.setEnabled(true);
				edtPostalCode.setEnabled(false);
				address.requestFocus();

				buttonPhoto.setEnabled(false);
				buttonOk.setEnabled(false);
			}
		});

		buttonCancel.setOnClickListener(new View.OnClickListener() {
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
			buttonPhoto.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(GeoForm.this, PhotoActivity.class);
					startActivityForResult(i, PHOTO);
				}
			});
		} else {
			// no Camera
			buttonPhoto.setEnabled(false);
			Log.i("camera", "This device has no camera!");
		}

		buttonOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (address.getText().toString().compareTo("") == 0) {
					address.requestFocus();
				} else {
					validate();
				}
			}

			private void validate() {
				boolean isSaved = false;

				self.showLoadingMask();

				self.setFormPropertiesWithFields();		

				task.setDone(true);

				isSaved = TaskDao.saveTask(task);
				isSaved = PhotoDao.savePhotos(photos);

				Intent data = new Intent();

				if(isSaved) {
					data.putExtra("RESULT", "Registro salvo!");
				} else {
					data.putExtra("RESULT", "Registro não foi salvo!");
				}

				setResult(RESULT_OK, data);
				finish();
			}
		});
	}


	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	private void mapFieldsToObjects() {
		// EditTexts
		address = (AutoCompleteTextView) findViewById(R.id.cp_log);
		edtNeighborhood = (EditText) findViewById(R.id.cp_nh);
		edtPostalCode = (EditText) findViewById(R.id.cp_cep);
		edtNumber = (EditText) findViewById(R.id.cp_num);
		lat = (TextView) findViewById(R.id.cp_lat);
		lon = (TextView) findViewById(R.id.cp_lon);
		edtCity = (EditText) findViewById(R.id.cp_cit);
		edtState = (EditText) findViewById(R.id.cp_est);
		edtInformation1 = (EditText) findViewById(R.id.cp_if1);
		edtInformation2 = (EditText) findViewById(R.id.cp_if2);	
		edtOtherNumbers = (EditText) findViewById(R.id.edt_other_numbers);

		// Spinners
		spnNumberConfirmation = (Spinner) findViewById(R.id.spnNumberConfirmation);
		spnVariance = (Spinner) findViewById(R.id.spnVariance);
		spnPrimaryUse = (Spinner) findViewById(R.id.spnPrimaryUse);
		spnSecondaryUse = (Spinner) findViewById(R.id.spnSecundaryUse);
		spnPavimentation = (Spinner) findViewById(R.id.spnPavimentation);
		spnAsphaltGuide = (Spinner) findViewById(R.id.spnAsphaltGuides);
		spnPublicIlumination = (Spinner) findViewById(R.id.spnPublicIllumination);
		spnEnergy = (Spinner) findViewById(R.id.spnEletricEnergy);
		spnPluvialGallery = (Spinner) findViewById(R.id.spnPluvialGalery);

		// Buttons 
		button_clear = (ImageButton) findViewById(R.id.cp_button_clear);
		buttonCancel = (Button) findViewById(R.id.cp_button_cancel);
		buttonOk = (Button) findViewById(R.id.cp_button_ok);
		buttonPhoto = (Button) findViewById(R.id.cp_button_photo);
	}


	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setFormPropertiesWithFields(){
		Form form = task.getForm();

		Double coordx = null;
		Double coordy = null;

		if(currentLocation != null) {
			coordx = currentLocation.getLatitude();
			coordy = currentLocation.getLongitude();
		}

		form.setCoordx(coordx);
		form.setCoordy(coordy);

		form.setDate(new Date());

		form.setInfo1(edtInformation1.getText().toString()); 
		form.setInfo2(edtInformation2.getText().toString()); 
		form.setOtherNumbers(edtOtherNumbers.getText().toString());

		form.setNumberConfirmation(spnNumberConfirmation.getSelectedItem().toString());
		form.setVariance(spnVariance.getSelectedItem().toString());
		form.setPrimaryUse(spnPrimaryUse.getSelectedItem().toString());
		form.setSecondaryUse(spnSecondaryUse.getSelectedItem().toString());
		form.setPavimentation(spnPavimentation.getSelectedItem().toString());
		form.setAsphaltGuide(spnAsphaltGuide.getSelectedItem().toString());
		form.setPublicIlumination(spnPublicIlumination.getSelectedItem().toString());
		form.setEnergy(spnEnergy.getSelectedItem().toString());
		form.setPluvialGallery(spnPluvialGallery.getSelectedItem().toString());
	}
	
	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setAutoCompleteAdapterPropertiers(Cursor cursor){

		AddressAdapter addressAdapter = new AddressAdapter(GeoForm.this,
				R.layout.item_list, 
				cursor,
				new String[] { "name", "edtPostalCode", "edtNumber", "edtNeighborhood"},
				new int[] {R.id.item_log, R.id.item_cep, R.id.item_neighborhood }
				);

		address.setAdapter(addressAdapter);
		address.setHint("pesquisar...");
		address.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				try {

					task = taskDao.queryForId((int) id);

					if(task != null) {
						TextView t1 = (TextView) view.findViewById(R.id.item_log);
						String logvalue = t1.getText().toString();
						address.setText(logvalue);

						TextView t2 = (TextView) view.findViewById(R.id.item_cep);
						String[] split1 = t2.getText().toString().split(" ");
						String cepvalue;

						if (split1[1].compareTo("---") == 0) {
							cepvalue = "";
						} else {
							cepvalue = split1[1];
						}

						edtPostalCode.setText(cepvalue);

						TextView txt_edtNumber = (TextView) view.findViewById(R.id.item_number);
						edtNumber.setText(txt_edtNumber.getText().toString());

						TextView txt_neighborhood = (TextView) view.findViewById(R.id.item_neighborhood);
						edtNeighborhood.setText(txt_neighborhood.getText().toString());

						address.setInputType(InputType.TYPE_NULL);
						edtPostalCode.setInputType(InputType.TYPE_NULL);

						address.clearFocus();
						address.setEnabled(false);
						edtPostalCode.setEnabled(false);

						InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						mgr.hideSoftInputFromWindow(address.getWindowToken(), 0);

						buttonPhoto.setEnabled(true);
						buttonOk.setEnabled(true);
					}					
				} catch (Exception ex) {
					Log.e(LOG_TAG, "Exception onItemClick: " + ex);
				}
			}
		});
	}

	/**
	 * 
	 * Cleat all fields, and select the null fields of Spinners.
	 * 
	 * @param String filePath
	 * 		 The path of the image that you want to get the base.
	 * 
	 * */
	public void clearAllFields(){
		edtNeighborhood.setText("");
		edtPostalCode.setText("");
		edtNumber.setText(""); 
		edtCity.setText(""); 
		edtState.setText(""); 
		edtInformation1.setText(""); 
		edtInformation2.setText(""); 
		edtOtherNumbers.setText("");

		spnNumberConfirmation.setSelection(0); 
		spnVariance.setSelection(0); 
		spnPrimaryUse.setSelection(0);
		spnSecondaryUse.setSelection(0); 
		spnPavimentation.setSelection(0); 
		spnAsphaltGuide.setSelection(0); 
		spnPublicIlumination.setSelection(0); 
		spnEnergy.setSelection(0); 
		spnPluvialGallery.setSelection(0);
	}

	/**
	 * 
	 *  Populates the fields based on the last completed form.
	 * 
	 * */
	public void setFieldsWithLastForm(){

	}

	/**
	 * 
	 * Returns Base64 String from filePath of a photo.
	 * 
	 * @param String filePath
	 * 		 The path of the image that you want to get the base.
	 * 
	 * */
	public String getBytesFromImage(String filePath) {
		String imgString;

		File imagefile = new File(filePath);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(imagefile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		Bitmap bitmap = BitmapFactory.decodeStream(fis);
		bitmap.compress(CompressFormat.JPEG, 70, stream);

		byte[] imageBytes = stream.toByteArray();

		// get the base 64 string
		imgString = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

		return imgString;	
	}

	/**
	 * 
	 * Callback of the PhotoActivity
	 * 
	 * */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO) {
			if (resultCode == RESULT_OK) {
				Photo photo = new Photo();

				String photoPath = data.getExtras().getString("RESULT");
				String blob = self.getBytesFromImage(photoPath);

				photo.setPath(photoPath);
				photo.setBlob(blob);
				photo.setForm(task.getForm());

				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
				Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				if (location != null) {
					lat.setText("" + location.getLatitude());
					lon.setText("" + location.getLongitude());
				}
				else {
					lat.setText("Location not available");
					lon.setText("Location not available");
				}

				photos.add(photo);

			} else if (resultCode == RESULT_CANCELED) {
			}
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {}

	@Override
	public void onProviderDisabled(String arg0) {}

	@Override
	public void onProviderEnabled(String arg0) {}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

	public void showLoadingMask() {
		dialog = ProgressDialog.show(GeoForm.this, "", "Salvando, aguarde...", true);
	}

	public void showLoadingMask(String message) {
		dialog = ProgressDialog.show(GeoForm.this, "", message, true);
	}

	public void hideLoadMask() {
		dialog.hide();
	}
}
