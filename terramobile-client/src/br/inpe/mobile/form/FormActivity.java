package br.inpe.mobile.form;

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
import android.app.AlertDialog.Builder;
import android.app.Dialog;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import br.inpe.mobile.R;
import br.inpe.mobile.R.string;
import br.inpe.mobile.Utility;
import br.inpe.mobile.address.AddressAdapter;
import br.inpe.mobile.location.LocationProvider;
import br.inpe.mobile.photo.Photo;
import br.inpe.mobile.photo.PhotoActivity;
import br.inpe.mobile.photo.PhotoDao;
import br.inpe.mobile.task.Task;
import br.inpe.mobile.task.TaskDao;

public class FormActivity extends Activity {
    
    // tag used to debug
    private final String         LOG_TAG = "#" + getClass().getSimpleName();
    
    // other activities
    private static final int     PHOTO   = 102;
    
    // widgets
    private AutoCompleteTextView address;
    
    private EditText             edtNeighborhood, edtPostalCode, edtNumber,
    edtOtherNumbers;
    
    private Spinner              spnNumberConfirmation, spnVariance,
    spnPrimaryUse, spnSecondaryUse,
    spnPavimentation, spnAsphaltGuide,
    spnPublicIlumination, spnEnergy,
    spnPluvialGallery;
    
    private TextView             lat, lon;
    
    private Button               buttonClearAddressFields;
    
    private Button               buttonCancel, buttonOk, buttonPhoto,
    buttonClearSpinners;
    
    private FormActivity         self    = this;
    
    private static Task          currentTask;
    
    public static Task           lastTask;
    
    private List<Photo>          photos;
    
    private ProgressDialog       dialog;
    
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
        currentTask = (Task) getIntent().getSerializableExtra("task");
        
        if (currentTask != null) {
            this.setFieldsWithTaskProperties(currentTask);
        }
        else {
            buttonPhoto.setEnabled(false);
            buttonOk.setEnabled(false);
        }
        
        self.createThreadToCursorAdapter();
        self.setButtonsListeners();
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
                self.clearSpinnerFields();
                self.clearAddressFields();
            }
        });
    }
    
    public void setButtonCancelListener() {
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.removeImageIfWasNotPersisted();
                setResult(RESULT_CANCELED, new Intent().putExtra("RESULT", "CANCEL"));
                finish();
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormActivity.this);
        alertDialogBuilder.setTitle("Atenção");
        alertDialogBuilder.setMessage("Deseja realmente sair?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
                    /*
                     * //Native Intent cameraIntent = new
                     * Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                     * startActivityForResult(cameraIntent, PHOTO);
                     */
                    
                    Intent i = new Intent(FormActivity.this, PhotoActivity.class);
                    startActivityForResult(i, PHOTO);
                }
            });
        }
        else {
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
                }
                else {
                    self.validateFields();
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
     * Database query can be a time consuming task, so its safe to call database
     * query in another thread
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
                }
                catch (SQLException e) {
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
        
        photos.clear();
        this.showPictures(photos);
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
     * Checks if the data filled in the form is null. This function don't check
     * the infrastructure fields.
     * 
     * @author Paulo Luan
     * */
    private String checkNull() {
        String message = "Você esqueceu de preencher os campos: \n";
        boolean isNull = false;
        
        if (spnNumberConfirmation.getSelectedItem().toString().equals("")) {
            message += "\nConfirmação de número";
            isNull = true;
        }
        if (spnVariance.getSelectedItem().toString().equals("")) {
            message += "\nDesconformidade";
            isNull = true;
        }
        if (spnPrimaryUse.getSelectedItem().toString().equals("")) {
            message += "\nUso primário";
            isNull = true;
        }
        if (spnSecondaryUse.getSelectedItem().toString().equals("")) {
            message += "\nUso secundário";
            isNull = true;
        }
        
        if (!isNull) {
            message = null;
        }
        
        return message;
    }
    
    /**
     * Checks if the data filled in the infrastructure form is null.
     * 
     * @author Paulo Luan
     * */
    public boolean checkInfrastructureFieldsIsNull() {
        boolean isNull = false;
        
        // Infra
        if (spnPavimentation.getSelectedItem().toString().equals("")) {
            // message += "\nPavimentação";
            isNull = true;
        }
        if (spnAsphaltGuide.getSelectedItem().toString().equals("")) {
            // message += "\nGuias";
            isNull = true;
        }
        if (spnPublicIlumination.getSelectedItem().toString().equals("")) {
            // message += "\nIluminação pública.";
            isNull = true;
        }
        if (spnEnergy.getSelectedItem().toString().equals("")) {
            // message += "\nEnergia";
            isNull = true;
        }
        if (spnPluvialGallery.getSelectedItem().toString().equals("")) {
            // message += "\nGaleria Pluvial";
            isNull = true;
        }
        
        return isNull;
    }
    
    /**
     * Checks if the data of the last filled Task is null.
     * 
     * @author Paulo Luan
     * */
    public boolean checkLastTaskInfrastructureFieldsIsNull(Task lastTask) {
        boolean isNull = false;
        
        Form form = lastTask.getForm();
        
        if (form.getPavimentation().equals("") ||
                form.getAsphaltGuide().equals("") ||
                form.getPublicIlumination().equals("") ||
                form.getEnergy().equals("") ||
                form.getPluvialGallery().equals("")) {
            
            isNull = true;
        }
        
        return isNull;
    }
    
    /**
     * 
     * Sets all the form fields with the data of task param.
     * 
     * @author Paulo Luan
     * */
    public void setFieldsWithTaskProperties(Task taskParam) {
        if (taskParam != null) {
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
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            
            if (taskParam.getId() != null) {
                address.setEnabled(false);
                edtPostalCode.setEnabled(false);
                buttonPhoto.setEnabled(true);
                buttonOk.setEnabled(true);
                
                this.loadPictures(taskParam);
            }
            
            self.clearAddressFocus();
        }
    }
    
    /**
     * Sets the form fields with a Task object properties.
     * 
     * @param Task
     *            the task that the data will be obtained and placed on the
     *            form.
     * 
     * @author Paulo Luan
     * */
    public void setInfrastructureFieldsWithTask(Task task) {
        try {
            // Only infrastructure spinners.
            spnPavimentation.setSelection(((ArrayAdapter<String>) spnPavimentation.getAdapter()).getPosition(task.getForm().getPavimentation()));
            spnAsphaltGuide.setSelection(((ArrayAdapter<String>) spnAsphaltGuide.getAdapter()).getPosition(task.getForm().getAsphaltGuide()));
            spnPublicIlumination.setSelection(((ArrayAdapter<String>) spnPublicIlumination.getAdapter()).getPosition(task.getForm().getPublicIlumination()));
            spnEnergy.setSelection(((ArrayAdapter<String>) spnEnergy.getAdapter()).getPosition(task.getForm().getEnergy()));
            spnPluvialGallery.setSelection(((ArrayAdapter<String>) spnPluvialGallery.getAdapter()).getPosition(task.getForm().getPluvialGallery()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * Compare if the sector and block between two tasks are the same.
     * 
     * @param lastTask
     * @param currentTask
     * @author PauloLuan
     * */
    public boolean isSameAddress(Task lastTask, Task currentTask) {
        boolean isSame = false;
        
        String lastFeatureId = null;
        String currentFeatureId = null;
        
        // gets the sector and block to the verification (0, 8).
        try {
            lastFeatureId = lastTask.getAddress().getFeatureId().substring(0, 8);
            currentFeatureId = currentTask.getAddress().getFeatureId().substring(0, 8);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        if (lastFeatureId != null &&
                currentFeatureId != null &&
                lastFeatureId.equals(currentFeatureId)) {
            
            isSame = true;
        }
        
        return isSame;
    }
    
    /**
     * Checks if the last address is the same and verifies if the infrastructure
     * fields are filled at the last Task fill.
     * 
     * @author Paulo Luan
     * */
    public void checkInfrastructureFill() {
        View formInfra = findViewById(R.id.formInfra);
        TextView txtInfraTitle = (TextView) findViewById(R.id.txt_infra_title);
        
        if (lastTask != null && currentTask != null) {
            boolean isSameAddress = self.isSameAddress(lastTask, currentTask);
            
            if (isSameAddress) {
                
                this.setInfrastructureFieldsWithTask(lastTask); // sets only infrastructure
                
                txtInfraTitle.setText(string.tv_plus_infra);
                formInfra.setVisibility(View.GONE);
            }
            
            // Location (Address) is changed.
            else {
                if (this.checkLastTaskInfrastructureFieldsIsNull(lastTask)) { // if user has filled the last infrastructure don't do anything.
                    this.setFieldsWithTaskProperties(lastTask); // sets all properties to fill the form again of the last task.
                    
                    Builder alert = new AlertDialog.Builder(FormActivity.this);
                    alert.setTitle(string.caution);
                    alert.setMessage(string.infra_message);
                    alert.setPositiveButton(string.btn_ok, null);
                    alert.show();
                    
                    txtInfraTitle.setText(string.tv_minus_infra);
                    
                    formInfra.setVisibility(View.VISIBLE);
                    formInfra.requestFocus();
                }
            }
        }
    }
    
    /**
     * onClick handler
     */
    public void toggleInfrastructureFields(View v) {
        View formInfra = findViewById(R.id.formInfra);
        TextView txtTitle = (TextView) findViewById(R.id.txt_infra_title);
        String title = "Infraestrutura";
        
        if (formInfra.isShown()) {
            // this.slideUp(this, formInfra);
            formInfra.setVisibility(View.GONE);
            txtTitle.setText(" +  " + title);
        }
        else {
            formInfra.setVisibility(View.VISIBLE);
            txtTitle.setText(" -  " + title);
            // this.slideDown(this, formInfra);
        }
    }
    
    /**
     * 
     * Gets all the informations filled on the fields and put into the form
     * Object that will be saved in local database.
     * 
     * @param Task
     * @author Paulo Luan
     * */
    public void setFormPropertiesWithFields(Task taskParam) {
        Form form = taskParam.getForm();
        
        form.setCoordx(Double.valueOf(lat.getText().toString()));
        form.setCoordy(Double.valueOf(lon.getText().toString()));
        
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
    public void setAutoCompleteAdapterPropertiers(Cursor cursor) {
        
        AddressAdapter addressAdapter = new AddressAdapter(FormActivity.this, R.layout.item_list, cursor, new String[] { "name",
                "number",
        "lote" }, new int[] { R.id.item_log,
                R.id.item_number,
                R.id.item_lote });
        
        address.setAdapter(addressAdapter);
        address.setHint("pesquisar...");
        address.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> adapter,
                    View view,
                    int position,
                    long addressId) {
                try {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(address.getWindowToken(), 0);
                    
                    currentTask = TaskDao.getTaskByAddressId((int) addressId);
                    
                    if (currentTask != null) {
                        self.setFieldsWithTaskProperties(currentTask);
                        self.checkInfrastructureFill();
                    }
                    
                    self.clearAddressFocus();
                }
                catch (Exception ex) {
                    Log.e(LOG_TAG, "Exception onItemClick: " + ex);
                }
            }
        });
    }
    
    /**
     * 
     * Load pictures metadata from sqlite database and put these images into the
     * ImageView.
     * 
     * @param Task
     * @author Paulo Luan
     * */
    public void loadPictures(Task task) {
        photos = PhotoDao.getPhotosByForm(task.getForm());
        
        if (photos != null && !photos.isEmpty()) {
            self.showPictures(photos);
        }
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
        
        edtPostalCode.setFocusable(false);
        edtNeighborhood.setFocusable(false);
        edtNumber.setFocusable(false);
        edtOtherNumbers.setFocusable(false);
        address.setFocusable(false);
        
        // TODO: workaround.
        edtOtherNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtOtherNumbers.setFocusable(true);
                edtOtherNumbers.setFocusableInTouchMode(true);
                edtOtherNumbers.requestFocus();
            }
        });
    }
    
    /**
     * 
     * Cleat all fields, and select the null fields of Spinners.
     * 
     * @param String
     *            filePath The path of the image that you want to get the base.
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
     * Returns Base64 String from filePath of a photo.
     * 
     * @param String
     *            filePath The path of the image that you want to get the base.
     * 
     * */
    public String getBytesFromImage(String filePath) {
        String imgString;
        
        File imagefile = new File(filePath);
        FileInputStream fis = null;
        
        try {
            fis = new FileInputStream(imagefile);
        }
        catch (FileNotFoundException e) {
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
                
                // Uri uri = data.getData();
                // String photoPath = uri.getPath();
                
                String blob = self.getBytesFromImage(photoPath);
                
                photo.setPath(photoPath);
                photo.setBase64(blob);
                photo.setForm(currentTask.getForm());
                
                Location location = LocationProvider.getInstance(this).getLocation();
                
                if (location != null) {
                    lat.setText("" + location.getLatitude());
                    lon.setText("" + location.getLongitude());
                }
                else {
                    Utility.showToast("Seu GPS está desabilitado, ligue-o para capturar sua posição.", Toast.LENGTH_LONG, this);
                    lat.setText("0.0");
                    lon.setText("0.0");
                }
                
                photos.add(photo);
                showPictures(photos);
            }
            else if (resultCode == RESULT_CANCELED) {}
        }
    }
    
    public void validateFields() {
        if (photos.isEmpty()) {
            Utility.showToast("Você precisa tirar ao menos uma foto.", Toast.LENGTH_LONG, FormActivity.this);
        }
        else {
            String message = null;
            
            if (!spnVariance.getSelectedItem().toString().equals("Não Detectada")) {
                // Quando não é detectado nenhuma desconformidade, então não é
                // obrigatório o preenchimento das informações.
                message = self.checkNull();
            }
            
            if (message != null) {
                Utility.showToast(message, Toast.LENGTH_LONG, self);
            }
            else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormActivity.this);
                alertDialogBuilder.setTitle("Atenção");
                alertDialogBuilder.setMessage("Deseja salvar?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        self.saveTaskIntoLocalDatabase();
                    }
                }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }
    
    protected void saveTaskIntoLocalDatabase() {
        boolean isSaved = false;
        
        self.showLoadingMask();
        self.setFormPropertiesWithFields(currentTask);
        currentTask.setDone(true);
        
        isSaved = TaskDao.updateTask(currentTask);
        
        if (!this.checkInfrastructureFieldsIsNull()) {
            TaskDao.updateInfrastructureDataFromAllForms(currentTask);
        }
        
        isSaved = PhotoDao.savePhotos(photos);
        
        Intent data = new Intent();
        
        if (isSaved) {
            FormActivity.lastTask = currentTask;
            data.putExtra("RESULT", "Registro salvo!");
        }
        else {
            data.putExtra("RESULT", "Registro não foi salvo!");
        }
        
        setResult(RESULT_OK, data);
        hideLoadMask();
        finish();
    }
    
    /**
     * 
     * Verify if the photo was persisted, and remove it if the user was
     * cancelled the operation.
     * 
     * */
    public void removeImageIfWasNotPersisted() {
        for (Photo photo : photos) {
            if (photo.getId() == null) { // remove file if it not persisted on
                // database (exists only on
                // filesystem).
                File file = new File(photo.getPath());
                file.delete();
                photos.remove(photo);
            }
        }
    }
    
    public void showPictures(List<Photo> pictures) {
        
        // LinearLayOut Setup
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_pictures);
        linearLayout.removeAllViews();
        
        for (Photo picture : pictures) {
            try {
                final ImageView imageView = self.generateImageFromFilePath(picture);
                linearLayout.addView(imageView);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Decodes image and scales it to reduce memory consumption
     * 
     * @param File
     *            the picture file
     * @param Size
     *            The new size we want to scale to
     * 
     * @author Paulo Luan
     * */
    private Bitmap decodeFile(File f, int scaleSize) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            
            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= scaleSize && o.outHeight / scale / 2 >= scaleSize) scale *= 2;
            
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        }
        catch (FileNotFoundException e) {}
        return null;
    }
    
    public ImageView generateImageFromFilePath(final Photo picture) {
        final File imgFile = new File(picture.getPath());
        final ImageView imageView = new ImageView(this);
        
        Bitmap myBitmap = this.decodeFile(imgFile, 70);
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
        layoutParams.setMargins(5, 5, 5, 5);
        
        imageView.setLayoutParams(layoutParams);
        imageView.setImageBitmap(myBitmap);
        
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.showConfirmDeleteImageDialog(picture, imgFile);
            }
        });
        
        return imageView;
    }
    
    public void showConfirmDeleteImageDialog(
            final Photo picture,
            final File file) {
        
        final Dialog imageDialog = new Dialog(this);
        
        final ImageView image = new ImageView(this);
        final File imgFile = new File(picture.getPath());
        Bitmap myBitmap = this.decodeFile(imgFile, 700);
        image.setImageBitmap(myBitmap);
        
        imageDialog.setContentView(image);
        imageDialog.setCancelable(true);
        imageDialog.setCanceledOnTouchOutside(true);
        imageDialog.show();
        
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormActivity.this);
                alertDialogBuilder.setTitle("Atenção");
                alertDialogBuilder.setMessage("Deseja Excluir esta foto?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        PhotoDao.deletePhoto(picture);
                        photos.remove(picture);
                        self.showPictures(photos);
                        
                        imageDialog.dismiss();
                    }
                }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }
    
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
