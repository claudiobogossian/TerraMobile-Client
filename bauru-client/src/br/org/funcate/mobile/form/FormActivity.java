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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.Utility;
import br.org.funcate.mobile.address.AddressAdapter;
import br.org.funcate.mobile.photo.Photo;
import br.org.funcate.mobile.photo.PhotoActivity;
import br.org.funcate.mobile.photo.PhotoDao;
import br.org.funcate.mobile.task.Task;
import br.org.funcate.mobile.task.TaskDao;

public class FormActivity extends Activity implements LocationListener {

	// tag used to debug
	private final String LOG_TAG = "#" + getClass().getSimpleName();

	// other activities
	private static final int PHOTO = 102;

	// widgets
	private AutoCompleteTextView address;

	private EditText 
		edtNeighborhood, 
		edtPostalCode, 
		edtNumber, 
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
	private Button buttonClearAddressFields;
	
	private Button 
		buttonCancel, 
		buttonOk, 
		buttonPhoto, 
		buttonClearSpinners;
	
	private LocationManager locationManager;

	private FormActivity self = this;

	private static Task task;
	public static Task lastTask;
	
	private List<Photo> photos;

	private ProgressDialog dialog;


	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_form);

		self.mapFieldsToObjects();
		
		photos = new ArrayList<Photo>();
		task = (Task) getIntent().getSerializableExtra("task");
		
		if(task != null) {
			this.setFieldsWithTaskProperties(task);
		} else {
			buttonPhoto.setEnabled(false);
			buttonOk.setEnabled(false);
		}
		
		self.createThreadToCursorAdapter();
		self.setButtonsListeners();
		self.setFieldsWithLastTask();
	}
	
	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setButtonsListeners() {
		self.setButtonClearListener();
		self.setButtonCancelListener();
		self.setButtonPhotoListener();
		self.setButtonOkListener();
		self.setButtonClearSpinnersListener();
	}
	
	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setButtonClearListener() {
		buttonClearAddressFields.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.clearAddressFields();
			}
		});
	}
	
	public void setButtonCancelListener() {
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED, new Intent().putExtra("RESULT", "CANCEL"));
				finish();
			}
		});
	}
	

	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setButtonPhotoListener() {
		PackageManager packageManager = self.getPackageManager();

		// if device support camera?
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			// yes Camera
			buttonPhoto.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(FormActivity.this, PhotoActivity.class);
					startActivityForResult(i, PHOTO);
				}
			});
		} else {
			// no Camera
			buttonPhoto.setEnabled(false);
			Log.i("camera", "This device has no camera!");
		}
	}
	

	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setButtonOkListener() {
		buttonOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (address.getText().toString().compareTo("") == 0) {
					address.requestFocus();
				} else {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormActivity.this);
				    alertDialogBuilder.setTitle("Atenção");
				    alertDialogBuilder.setMessage("Deseja salvar?").setCancelable(false).setPositiveButton("Sim",
				        new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				                validateFields();
				            }
				        })
				        .setNegativeButton("Não",
				        new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				            }
				        });
				 
				    AlertDialog alertDialog = alertDialogBuilder.create();
				    alertDialog.show();
				}
			}
		});
	}
	

	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setButtonClearSpinnersListener() {
		buttonClearSpinners.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.clearSpinnerFields();
				self.clearAddressFocus();				
			}
		});
	}
	
	/**
	 * 
	 * Database query can be a time consuming task, so its safe to call database query in another thread
	 * 
	 * @author Paulo Luan
	 * */
	public void createThreadToCursorAdapter() {
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
	}
	

	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void clearAddressFields() {
		lat.setText("");
		lon.setText("");
		address.setText("");
		
		edtPostalCode.setText("");
		edtNeighborhood.setText("");
		edtNumber.setText("");
		
		edtPostalCode.setInputType(InputType.TYPE_NULL);
		edtNeighborhood.setInputType(InputType.TYPE_NULL);
		edtNumber.setInputType(InputType.TYPE_NULL);
		
		edtPostalCode.setEnabled(false);
		edtNeighborhood.setEnabled(false);
		edtNumber.setEnabled(false);
		
		address.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
		address.setEnabled(true);
		address.setFocusable(true);
		address.setFocusableInTouchMode(true);
		address.requestFocus();

		buttonPhoto.setEnabled(false);
		buttonOk.setEnabled(false);
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
		buttonClearAddressFields = (Button) findViewById(R.id.button_clear_address);
		buttonClearSpinners = (Button) findViewById(R.id.button_clear_spinners);
		buttonCancel = (Button) findViewById(R.id.cp_button_cancel);
		buttonOk = (Button) findViewById(R.id.cp_button_ok);
		buttonPhoto = (Button) findViewById(R.id.cp_button_photo);
	}
	

	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setFieldsWithTaskProperties(Task taskParam) {
		if(taskParam != null) {
			lat.setText("" + taskParam.getAddress().getCoordx());
			lon.setText("" + taskParam.getAddress().getCoordy());
			
			address.setText(taskParam.getAddress().getName());
			edtNeighborhood.setText(taskParam.getAddress().getNeighborhood());
			edtPostalCode.setText(taskParam.getAddress().getPostalCode());
			edtNumber.setText(taskParam.getAddress().getNumber());
			edtOtherNumbers.setText(taskParam.getForm().getOtherNumbers());
			
			try {
				spnNumberConfirmation.setSelection(((ArrayAdapter<String>) spnNumberConfirmation.getAdapter()).getPosition(taskParam.getForm().getNumberConfirmation()));
				spnVariance.setSelection(((ArrayAdapter<String>) spnVariance.getAdapter()).getPosition(taskParam.getForm().getVariance()));
				spnPrimaryUse.setSelection(((ArrayAdapter<String>) spnPrimaryUse.getAdapter()).getPosition(taskParam.getForm().getPrimaryUse()));
				spnSecondaryUse.setSelection(((ArrayAdapter<String>) spnSecondaryUse.getAdapter()).getPosition(taskParam.getForm().getSecondaryUse())); 
				spnPavimentation.setSelection(((ArrayAdapter<String>) spnPavimentation.getAdapter()).getPosition(taskParam.getForm().getPavimentation())); 
				spnAsphaltGuide.setSelection(((ArrayAdapter<String>) spnAsphaltGuide.getAdapter()).getPosition(taskParam.getForm().getAsphaltGuide())); 
				spnPublicIlumination.setSelection(((ArrayAdapter<String>) spnPublicIlumination.getAdapter()).getPosition(taskParam.getForm().getPublicIlumination())); 
				spnEnergy.setSelection(((ArrayAdapter<String>) spnEnergy.getAdapter()).getPosition(taskParam.getForm().getEnergy())); 
				spnPluvialGallery.setSelection(((ArrayAdapter<String>) spnPluvialGallery.getAdapter()).getPosition(taskParam.getForm().getPluvialGallery()));	
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(taskParam.getId() != null) {
				address.setEnabled(false);
				edtPostalCode.setEnabled(false);
				buttonPhoto.setEnabled(true);
				buttonOk.setEnabled(true);
			}
			
			self.clearAddressFocus();
		}	
	}


	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setFieldsWithLastTask() {
		if(lastTask != null) {
			try {
				// Only infrastructure spinners.
				spnPavimentation.setSelection(((ArrayAdapter<String>) spnPavimentation.getAdapter()).getPosition(lastTask.getForm().getPavimentation())); 
				spnAsphaltGuide.setSelection(((ArrayAdapter<String>) spnAsphaltGuide.getAdapter()).getPosition(lastTask.getForm().getAsphaltGuide())); 
				spnPublicIlumination.setSelection(((ArrayAdapter<String>) spnPublicIlumination.getAdapter()).getPosition(lastTask.getForm().getPublicIlumination())); 
				spnEnergy.setSelection(((ArrayAdapter<String>) spnEnergy.getAdapter()).getPosition(lastTask.getForm().getEnergy())); 
				spnPluvialGallery.setSelection(((ArrayAdapter<String>) spnPluvialGallery.getAdapter()).getPosition(lastTask.getForm().getPluvialGallery()));	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void setFormPropertiesWithFields(Task taskParam){
		Form form = taskParam.getForm();
		
		taskParam.getAddress().setCoordx(Double.valueOf(lat.getText().toString()));
		taskParam.getAddress().setCoordy(Double.valueOf(lon.getText().toString()));
		
		form.setDate(new Date());
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

		AddressAdapter addressAdapter = new AddressAdapter(
				FormActivity.this,
				R.layout.item_list, 
				cursor,
				new String[] { "name", "edtPostalCode", "edtNumber", "edtNeighborhood"},
				new int[] {R.id.item_log, R.id.item_cep, R.id.item_neighborhood }
		);

		address.setAdapter(addressAdapter);
		address.setHint("pesquisar...");
		address.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long addressId) {
				try {					
					address.clearFocus();
					address.setFocusableInTouchMode(false);
					address.setFocusable(false);
					edtPostalCode.clearFocus();
					edtNeighborhood.clearFocus();
					edtNumber.clearFocus();
					
					edtPostalCode.setFocusable(false);
					edtNeighborhood.setFocusable(false);
					edtNeighborhood.setFocusableInTouchMode(false);
					edtNumber.setFocusable(false);
					
					InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					mgr.hideSoftInputFromWindow(address.getWindowToken(), 0);
					
					task = TaskDao.getTaskByAddressId((int) addressId);
					
					if(task != null) {
						self.setFieldsWithTaskProperties(task);
					}
				} catch (Exception ex) {
					Log.e(LOG_TAG, "Exception onItemClick: " + ex);
				}
			}
		});
	}
	
	/**
	 * 
	 * 
	 * @author Paulo Luan
	 * */
	public void clearAddressFocus() {
		edtPostalCode.clearFocus();
		edtNeighborhood.clearFocus();
		edtNumber.clearFocus();
		edtOtherNumbers.clearFocus();
		address.clearFocus();
	}

	/**
	 * 
	 * Cleat all fields, and select the null fields of Spinners.
	 * 
	 * @param String filePath
	 * 		 The path of the image that you want to get the base.
	 * 
	 * */
	public void clearSpinnerFields() {
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
					lat.setText("0.0");
					lon.setText("0.0");
				}

				photos.add(photo);

			} else if (resultCode == RESULT_CANCELED) {
			}
		}
	}
	
	public void validateFields() {
		boolean isSaved = false;
		
		if(photos.isEmpty()) {
			Utility.showToast("Você precisa tirar ao menos uma foto.", Toast.LENGTH_LONG, FormActivity.this);
		} else {
			self.showLoadingMask();
			self.setFormPropertiesWithFields(task);		
			task.setDone(true);

			isSaved = TaskDao.updateTask(task);
			isSaved = PhotoDao.savePhotos(photos);

			Intent data = new Intent();

			if(isSaved) {
				FormActivity.lastTask = task;
				data.putExtra("RESULT", "Registro salvo!");
			} else {
				data.putExtra("RESULT", "Registro não foi salvo!");
			}

			setResult(RESULT_OK, data);
			hideLoadMask();
			finish();
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
		dialog = ProgressDialog.show(FormActivity.this, "", "Salvando, aguarde...", true);
	}

	public void showLoadingMask(String message) {
		dialog = ProgressDialog.show(FormActivity.this, "", message, true);
	}

	public void hideLoadMask() {
		dialog.hide();
		dialog.cancel();
	}
}
