package br.inova.mobile.form;

import java.io.File;
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
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import br.inova.mobile.Utility;
import br.inova.mobile.address.AddressAdapter;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.map.LandmarksManager;
import br.inova.mobile.photo.BitmapRendererTask;
import br.inova.mobile.photo.CameraActivity;
import br.inova.mobile.photo.CreatePhotoAsync;
import br.inova.mobile.photo.Photo;
import br.inova.mobile.photo.PhotoDao;
import br.inova.mobile.task.Task;
import br.inova.mobile.task.TaskDao;
import br.inpe.mobile.R;
import br.inpe.mobile.R.string;

public class FormActivity extends Activity {
        
        // tag used to debug
        private final String         LOG_TAG = "#" + getClass().getSimpleName();
        
        // other activities
        private static final int     PHOTO   = 102;
        
        // widgets
        private AutoCompleteTextView address;
        
        private EditText             edtNeighborhood, edtPostalCode, edtNumber,
                        edtOtherNumbers, edtObservations;
        
        private Spinner              spnNumberConfirmation, spnVariance,
                        spnPrimaryUse, spnSecondaryUse, spnPavimentation,
                        spnAsphaltGuide, spnPublicIlumination, spnEnergy,
                        spnPluvialGallery;
        
        public static TextView       lat, lon;
        
        private Button               buttonClearAddressFields;
        
        private Button               buttonCancel, buttonOk, buttonPhoto,
                        buttonClearSpinners;
        
        private FormActivity         self    = this;
        
        public static Task           currentTask;
        
        public static Task           lastTask;
        
        public static List<Photo>    photos;
        
        private ProgressDialog       dialog;
        
        private Resources            resources;
        
        /**
         * 
         * 
         * @author Paulo Luan
         * */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
                /**
                 * Defines the default exception handler to log unexpected
                 * android errors
                 */
                
                resources = getResources();
                
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                setContentView(R.layout.activity_form);
                
                self.mapFieldsToObjects();
                
                photos = new ArrayList<Photo>();
                currentTask = (Task) getIntent().getSerializableExtra("task");
                
                if (currentTask != null) {
                        this.setFieldsWithTaskProperties(currentTask);
                        self.checkInfrastructureFill();
                }
                else {
                        buttonPhoto.setEnabled(false);
                        buttonOk.setEnabled(false);
                }
                
                self.createThreadToCursorAdapter();
                self.setButtonsListeners();
                self.setSpinnerListeners();
        }
        
        private void setSpinnerListeners() {
                setSpinnerNumberConfirmation();
                setSpinnerVariance();
        }
        
        /**
         * Maps the selection listener of NumbersConfirmation Spinner and change
         * the visibiity of the Edit Text "numbers confirmation".
         * 
         * @author Paulo Luan
         * 
         */
        private void setSpinnerNumberConfirmation() {
                spnNumberConfirmation.setOnItemSelectedListener(new OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(
                                                   AdapterView<?> parentView,
                                                   View selectedItemView,
                                                   int position,
                                                   long id) {
                                Object spnNumberConfirmationItem = spnNumberConfirmation.getSelectedItem();
                                String spnNumberConfirmationString = (spnNumberConfirmationItem == null) ? "" : spnNumberConfirmationItem.toString();
                                String notConfer = resources.getString(string.not_confer);
                                
                                if (spnNumberConfirmationString == notConfer) {
                                        findViewById(R.id.numbers_founded_layout).setVisibility(View.VISIBLE);
                                }
                                else {
                                        findViewById(R.id.numbers_founded_layout).setVisibility(View.GONE);
                                }
                        }
                        
                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {}
                });
        }
        
        private void setSpinnerVariance() {
                
                spnVariance.setOnItemSelectedListener(new OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(
                                                   AdapterView<?> parentView,
                                                   View selectedItemView,
                                                   int position,
                                                   long id) {
                                boolean isVarianceFreeOrNonconforming = isVarianceFreeOrNonConforming();
                                
                                if (isVarianceFreeOrNonconforming) {
                                        findViewById(R.id.ground_informations_layout).setVisibility(View.GONE);
                                }
                                else {
                                        findViewById(R.id.ground_informations_layout).setVisibility(View.VISIBLE);
                                }
                                
                                if (isVarianceNonConforming()) {
                                        findViewById(R.id.observationsForm).setVisibility(View.VISIBLE);
                                        TextView txtObservationTitle = (TextView) findViewById(R.id.txt_observation_title);
                                        txtObservationTitle.setText(resources.getString(R.string.tv_minus_observations));
                                }
                        }
                        
                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {}
                });
        }
        
        public void clearInformationsForWastelands() {
                try {
                        removePicturesIfWasNotPersisted();
                        
                        spnNumberConfirmation.setSelection(0);
                        spnPrimaryUse.setSelection(0);
                        spnSecondaryUse.setSelection(0);
                        
                        edtOtherNumbers.setText("");
                }
                catch (Exception e) {
                        e.printStackTrace();
                }
        }
        
        /**
         * Calls to several functions to register all listener events to the
         * buttons.
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
         * When clicked, clears the spinners and Addresses fields.
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
        
        /**
         * Removes the pictures and kill the current activity.
         * 
         * @author Paulo Luan
         * */
        public void setButtonCancelListener() {
                buttonCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                self.removePicturesIfWasNotPersisted();
                                setResult(RESULT_CANCELED, new Intent().putExtra("RESULT", "CANCEL"));
                                finish();
                        }
                });
        }
        
        /**
         * Override the functions "onBackPressed" to map another function to the
         * native button back, when it is clicked, a popup is displayed to the
         * user.
         * 
         * @author Paulo Luan
         * */
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
         * Creates new Intent to show the camera and take the picture.
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
                                        Intent i = new Intent(FormActivity.this, CameraActivity.class);
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
         * Database query can be a time consuming task, so its safe to call
         * database query in another thread
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
                                        ExceptionHandler.saveLogFile(e);
                                }
                        }
                });
        }
        
        /**
         * Clear all the address fields.
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
                
                address.setEnabled(true);
                address.setFocusable(true);
                address.setFocusableInTouchMode(true);
                address.requestFocus();
                address.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
                self.showKeyboard(address);
                
                buttonPhoto.setEnabled(false);
                buttonOk.setEnabled(false);
                
                photos.clear();
                this.showPictures(photos);
        }
        
        /**
         * Maps the XML Elements to the Java objects, to manipulate it in the
         * Java code.
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
                edtObservations = (EditText) findViewById(R.id.edt_observations);
                
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
         * Checks if the data filled in the form is null. This function don't
         * check the infrastructure fields.
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
         * @return boolean
         * @author Paulo Luan
         * */
        public boolean checkInfrastructureFieldsIsNull() {
                boolean isNull = false;
                
                Object spnPavimentationItem = spnPavimentation.getSelectedItem();
                String spnPavimentationString = (spnPavimentationItem == null) ? "" : spnPavimentationItem.toString();
                Object spnAsphaltGuideItem = spnAsphaltGuide.getSelectedItem();
                String spnAsphaltGuideString = (spnAsphaltGuideItem == null) ? "" : spnAsphaltGuideItem.toString();
                
                Object spnPublicIluminationItem = spnPublicIlumination.getSelectedItem();
                String spnPublicIluminationString = (spnPublicIluminationItem == null) ? "" : spnPublicIluminationItem.toString();
                
                Object spnEnergyItem = spnEnergy.getSelectedItem();
                String spnEnergyString = (spnEnergyItem == null) ? "" : spnEnergyItem.toString();
                
                Object spnPluvialGalleryItem = spnPluvialGallery.getSelectedItem();
                String spnPluvialGalleryString = (spnPluvialGalleryItem == null) ? "" : spnPluvialGalleryItem.toString();
                
                if (spnPavimentationString.equals("")) {
                        // message += "\nPavimentação";
                        isNull = true;
                }
                if (spnAsphaltGuideString.equals("")) {
                        // message += "\nGuias";
                        isNull = true;
                }
                if (spnPublicIluminationString.equals("")) {
                        // message += "\nIluminação pública.";
                        isNull = true;
                }
                if (spnEnergyString.equals("")) {
                        // message += "\nEnergia";
                        isNull = true;
                }
                if (spnPluvialGalleryString.equals("")) {
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
                
                if (form.getPavimentation().equals("") || form.getAsphaltGuide().equals("") || form.getPublicIlumination().equals("") || form.getEnergy().equals("") || form.getPluvialGallery().equals("")) {
                        
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
                        currentTask = taskParam;
                        
                        address.setText(taskParam.getAddress().getName());
                        edtNeighborhood.setText(taskParam.getAddress().getNeighborhood());
                        edtPostalCode.setText(taskParam.getAddress().getPostalCode());
                        edtNumber.setText(taskParam.getAddress().getNumber());
                        edtOtherNumbers.setText(taskParam.getForm().getOtherNumbers());
                        edtObservations.setText(taskParam.getForm().getInfo1());
                        
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
                                ExceptionHandler.saveLogFile(e);
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
         *                the task that the data will be obtained and placed on
         *                the form.
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
                        ExceptionHandler.saveLogFile(e);
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
                
                // gets the sector and block to the verification (0, 6).
                try {
                        lastFeatureId = lastTask.getAddress().getFeatureId().substring(0, 6);
                        currentFeatureId = currentTask.getAddress().getFeatureId().substring(0, 6);
                }
                catch (Exception e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                if (lastFeatureId != null && currentFeatureId != null && lastFeatureId.equals(currentFeatureId)) {
                        if (lastTask.getAddress().getName().equals(currentTask.getAddress().getName())) {
                                isSame = true;
                        }
                }
                
                return isSame;
        }
        
        /**
         * Checks if the last address is the same and verifies if the
         * infrastructure fields are filled at the last Task fill.
         * 
         * @author Paulo Luan
         * */
        public void checkInfrastructureFill() {
                View formInfra = findViewById(R.id.formInfra);
                TextView txtInfraTitle = (TextView) findViewById(R.id.txt_infra_title);
                
                if (lastTask != null && currentTask != null) {
                        boolean isSameAddress = self.isSameAddress(lastTask, currentTask);
                        
                        if (isSameAddress) {
                                
                                this.setInfrastructureFieldsWithTask(lastTask); // sets only
                                // infrastructure
                                
                                txtInfraTitle.setText(string.tv_plus_infra);
                                formInfra.setVisibility(View.GONE);
                        }
                        
                        // Location (Address) is changed.
                        else {
                                if (this.checkLastTaskInfrastructureFieldsIsNull(lastTask)) { // if
                                        // user
                                        // has
                                        // filled
                                        // the
                                        // last
                                        // infrastructure
                                        // don't
                                        // do
                                        // anything.
                                        this.setFieldsWithTaskProperties(lastTask); // sets all
                                        // properties to
                                        // fill the form
                                        // again of the
                                        // last task.
                                        
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
         * Toggle the visibility of the infrastructure form to Gone os visible.
         * 
         * @author PauloLuan
         */
        public void toggleInfrastructureFields(View v) {
                View formInfra = findViewById(R.id.formInfra);
                TextView txtTitle = (TextView) findViewById(R.id.txt_infra_title);
                
                if (formInfra.isShown()) {
                        formInfra.setVisibility(View.GONE);
                        txtTitle.setText(resources.getString(R.string.tv_plus_infra));
                }
                else {
                        formInfra.setVisibility(View.VISIBLE);
                        txtTitle.setText(resources.getString(R.string.tv_minus_infra));
                }
        }
        
        /**
         * Toggle the visibility of the infrastructure form to Gone os visible.
         * 
         * @author PauloLuan
         */
        public void toggleObservationField(View v) {
                View observationsForm = findViewById(R.id.observationsForm);
                TextView txtTitle = (TextView) findViewById(R.id.txt_observation_title);
                
                if (observationsForm.isShown()) {
                        observationsForm.setVisibility(View.GONE);
                        txtTitle.setText(resources.getString(R.string.tv_plus_observations));
                }
                else {
                        observationsForm.setVisibility(View.VISIBLE);
                        txtTitle.setText(resources.getString(R.string.tv_minus_observations));
                }
        }
        
        /**
         * 
         * Map all fields to a Form object.
         * 
         * @param from
         * @return Form the form object with all filled informations in the
         *         fields.
         * 
         * @author Paulo Luan
         * */
        public Form makeFormInformationsToObject(Form form) {
                Double latitude = 0.0;
                Double longitude = 0.0;
                
                try {
                        String stringLat = lat.getText().toString();
                        latitude = Double.valueOf(stringLat);
                }
                catch (Exception e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                try {
                        String stringLong = lon.getText().toString();
                        longitude = Double.valueOf(stringLong);
                }
                catch (Exception e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                form.setCoordx(latitude);
                form.setCoordy(longitude);
                
                form.setDate(new Date());
                form.setOtherNumbers(edtOtherNumbers.getText().toString());
                form.setInfo1(edtObservations.getText().toString());
                
                Object spnNumberConfirmationItem = spnNumberConfirmation.getSelectedItem();
                String spnNumberConfirmationString = (spnNumberConfirmationItem == null) ? "" : spnNumberConfirmationItem.toString();
                form.setNumberConfirmation(spnNumberConfirmationString);
                
                Object spnVarianceItem = spnVariance.getSelectedItem();
                String spnVarianceString = (spnVarianceItem == null) ? "" : spnVarianceItem.toString();
                form.setVariance(spnVarianceString);
                
                Object spnPrimaryUseItem = spnPrimaryUse.getSelectedItem();
                String spnPrimaryUseString = (spnPrimaryUseItem == null) ? "" : spnPrimaryUseItem.toString();
                form.setPrimaryUse(spnPrimaryUseString);
                
                Object spnSecondaryUseItem = spnSecondaryUse.getSelectedItem();
                String spnSecondaryUseString = (spnSecondaryUseItem == null) ? "" : spnSecondaryUseItem.toString();
                form.setSecondaryUse(spnSecondaryUseString);
                
                Object spnPavimentationItem = spnPavimentation.getSelectedItem();
                String spnPavimentationString = (spnPavimentationItem == null) ? "" : spnPavimentationItem.toString();
                form.setPavimentation(spnPavimentationString);
                
                Object spnAsphaltGuideItem = spnAsphaltGuide.getSelectedItem();
                String spnAsphaltGuideString = (spnAsphaltGuideItem == null) ? "" : spnAsphaltGuideItem.toString();
                form.setAsphaltGuide(spnAsphaltGuideString);
                
                Object spnPublicIluminationItem = spnPublicIlumination.getSelectedItem();
                String spnPublicIluminationString = (spnPublicIluminationItem == null) ? "" : spnPublicIluminationItem.toString();
                form.setPublicIlumination(spnPublicIluminationString);
                
                Object spnEnergyItem = spnEnergy.getSelectedItem();
                String spnEnergyString = (spnEnergyItem == null) ? "" : spnEnergyItem.toString();
                form.setEnergy(spnEnergyString);
                
                Object spnPluvialGalleryItem = spnPluvialGallery.getSelectedItem();
                String spnPluvialGalleryString = (spnPluvialGalleryItem == null) ? "" : spnPluvialGalleryItem.toString();
                form.setPluvialGallery(spnPluvialGalleryString);
                
                return form;
        }
        
        /**
         * 
         * Creates the auto Complete Text View to user search the addresses.
         * 
         * @author Paulo Luan
         * */
        public void setAutoCompleteAdapterPropertiers(Cursor cursor) {
                
                AddressAdapter addressAdapter = new AddressAdapter(FormActivity.this, R.layout.item_list, cursor, new String[] { "name", "number", "lote" }, new int[] { R.id.item_log, R.id.item_number, R.id.item_lote });
                
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
         * Load pictures metadata from sqlite database and put these images into
         * the ImageView.
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
         * Clear the focus from all the Edit Text widgets, of this activity.
         * 
         * @author Paulo Luan
         */
        public void clearAddressFocus() {
                edtPostalCode.clearFocus();
                edtNeighborhood.clearFocus();
                edtNumber.clearFocus();
                edtOtherNumbers.clearFocus();
                edtObservations.clearFocus();
                address.clearFocus();
                
                edtPostalCode.setFocusable(false);
                edtNeighborhood.setFocusable(false);
                edtNumber.setFocusable(false);
                edtOtherNumbers.setFocusable(false);
                address.setFocusable(false);
                
                edtOtherNumbers.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                self.showKeyboard(edtOtherNumbers);
                                edtOtherNumbers.setFocusable(true);
                                edtOtherNumbers.setFocusableInTouchMode(true);
                                edtOtherNumbers.requestFocus();
                        }
                });
        }
        
        /**
         * 
         * Clear all fields, and select the null fields of Spinners.
         * 
         * @param String
         *                filePath The path of the image that you want to get
         *                the base.
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
         * Callback of the PhotoActivity
         * 
         * */
        @Override
        public void onActivityResult(
                                     int requestCode,
                                     int resultCode,
                                     final Intent data) {
                if (requestCode == PHOTO) {
                        if (resultCode == RESULT_OK) {
                                String photoPath = data.getExtras().getString("RESULT");
                                new CreatePhotoAsync(photoPath, this); //TODO: modificar
                        }
                        else if (resultCode == RESULT_CANCELED) {}
                }
        }
        
        // Quando não é detectado nenhuma desconformidade ou vago, então não é obrigatório o preenchimento das informações.
        
        /**
         * 
         * Verifies if the selection of the spinner variance is either
         * Nonconforming or Free.
         * 
         * */
        public boolean isVarianceFreeOrNonConforming() {
                boolean isNonConforming = isVarianceNonConforming();
                boolean isFree = isVarianceFree();
                
                boolean isSelected = false;
                
                if (isNonConforming || isFree) {
                        isSelected = true;
                }
                
                return isSelected;
        }
        
        /**
         * 
         * Verifies if the selection of the spinner variance is nonconforming.
         * 
         * */
        public boolean isVarianceNonConforming() {
                String nonConforming = resources.getString(R.string.nonconforming);
                String spnVarianceString = spnVariance.getSelectedItem().toString();
                boolean isNonConforming = spnVarianceString.equals(nonConforming);
                return isNonConforming;
        }
        
        /**
         * 
         * Verifies if the selection of the spinner variance is Free.
         * 
         * */
        public boolean isVarianceFree() {
                String free = resources.getString(R.string.free);
                String spnVarianceString = spnVariance.getSelectedItem().toString();
                boolean isFree = spnVarianceString.equals(free);
                return isFree;
        }
        
        public void validateFields() {
                String message = null;
                
                if (!isVarianceFree()) {
                        
                        message = self.checkNull();
                        
                        if (photos.isEmpty()) {
                                if (message == null) {
                                        message = "\n\n Você precisa tirar ao menos uma foto.";
                                }
                                else {
                                        message += "\n\n Você precisa tirar ao menos uma foto.";
                                }
                        }
                }
                
                if (isVarianceNonConforming()) {
                        message = self.checkNonconformingNull();
                }
                
                if (message != null) {
                        Utility.showToast(message, Toast.LENGTH_LONG, self);
                }
                else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormActivity.this);
                        alertDialogBuilder.setTitle("Atenção");
                        alertDialogBuilder.setMessage("Deseja salvar?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                        dialog.cancel();
                                        self.saveTaskIntoLocalDatabase();
                                }
                        }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                        dialog.cancel();
                                }
                        });
                        
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                }
        }
        
        /**
         * Verifies if the Observations field is filled.
         * */
        private String checkNonconformingNull() {
                String message = null;
                
                if (edtObservations.getText().toString().equals("")) {
                        message = "\nCampo de observação.";
                }
                
                return message;
        }
        
        protected void saveTaskIntoLocalDatabase() {
                boolean isSaved = false;
                
                self.showLoadingMask();
                
                if (isVarianceFreeOrNonConforming()) {
                        clearInformationsForWastelands();
                }
                else if (!photos.isEmpty()) {
                        isSaved = PhotoDao.savePhotos(photos);
                }
                
                Form filledForm = self.makeFormInformationsToObject(currentTask.getForm());
                currentTask.setForm(filledForm);
                currentTask.setDone(true);
                
                isSaved = TaskDao.updateTask(currentTask);
                
                if (!this.checkInfrastructureFieldsIsNull()) {
                        TaskDao.updateInfrastructureDataFromAllForms(currentTask);
                }
                
                Intent data = new Intent();
                
                if (isSaved) {
                        FormActivity.lastTask = currentTask;
                        LandmarksManager.setTask(currentTask);
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
        public void removePicturesIfWasNotPersisted() {
                for (Photo photo : photos) {
                        if (photo.getId() == null) { // remove file if it not persisted on database (exists only on filesystem).
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
                                
                                File pic = new File(picture.getPath());
                                
                                if (!pic.exists()) {
                                        self.deletePicture(picture);
                                }
                                else {
                                        final ImageView imageView = self.generateImageFromFilePath(picture);
                                        linearLayout.addView(imageView);
                                }
                        }
                        catch (Exception e) {
                                ExceptionHandler.saveLogFile(e);
                        }
                }
        }
        
        public ImageView generateImageFromFilePath(final Photo picture) {
                final File imgFile = new File(picture.getPath());
                final ImageView imageView = new ImageView(this);
                
                //Bitmap myBitmap = Utility.decodeSampledBitmapFromFile(imgFile, 100, 100);
                
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
                layoutParams.setMargins(5, 5, 5, 5);
                
                imageView.setLayoutParams(layoutParams);
                //imageView.setImageBitmap(myBitmap);
                
                imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                self.showConfirmDeleteImageDialog(picture, imgFile);
                        }
                });
                
                new BitmapRendererTask(imgFile, imageView, 100, 100);
                
                return imageView;
        }
        
        public void showConfirmDeleteImageDialog(
                                                 final Photo picture,
                                                 final File file) {
                final Dialog imageDialog = new Dialog(this);
                
                final ImageView imageView = new ImageView(this);
                final File imageFile = new File(picture.getPath());
                
                new BitmapRendererTask(imageFile, imageView, 640, 480);
                
                imageDialog.setContentView(imageView);
                imageDialog.setCancelable(true);
                imageDialog.setCanceledOnTouchOutside(true);
                imageDialog.show();
                
                imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormActivity.this);
                                alertDialogBuilder.setTitle("Atenção");
                                alertDialogBuilder.setMessage("Deseja Excluir esta foto?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                dialog.cancel();
                                                self.deletePicture(picture);
                                                imageDialog.dismiss();
                                        }
                                }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                dialog.cancel();
                                        }
                                });
                                
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                        }
                });
        }
        
        /**
         * 
         * Delete a photo from database and filesystem.
         * 
         * @param Photo
         *                the picture that will be removed.
         * @author Paulo Luan
         */
        public void deletePicture(Photo picture) {
                PhotoDao.deletePhoto(picture);
                photos.remove(picture);
                self.showPictures(photos);
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
        
        private void showKeyboard(EditText editText) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
        }
}
