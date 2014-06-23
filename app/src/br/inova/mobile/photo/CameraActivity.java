package br.inova.mobile.photo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ZoomControls;
import br.inova.mobile.Utility;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.form.FormActivity;
import br.inova.mobile.task.Task;
import br.inova.mobile.user.SessionManager;
import br.inpe.mobile.R;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {
        
        private static String   TAG              = "CAMERA_ACTIVITY";
        
        private Camera          mCamera;
        
        public static final int MEDIA_TYPE_IMAGE = 1;
        public static final int MEDIA_TYPE_VIDEO = 2;
        
        private static Integer  zoom             = 0;
        private SessionManager  session;
        
        private File            pictureFile      = null;
        
        private SurfaceHolder   mSurfaceHolder;
        
        LayoutInflater          controlInflater  = null;
        
        //Layouts
        private LinearLayout    takePictureLayout, confirmPictureLayout;
        
        // Buttons
        private ImageButton     btnBack;
        private ImageButton     btnTakePicture;
        private ImageButton     btnCancel;
        private ImageButton     btnOk;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                
                Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this)); //Defines the default exception handler to log unexpected android errors
                session = SessionManager.getInstance();
                
                mCamera = getCameraInstance();
                
                this.initializeLayout();
                this.initializeButtons();
        }
        
        public void initializeLayout() {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                setContentView(R.layout.activity_photo);
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                getWindow().setFormat(PixelFormat.UNKNOWN);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                
                controlInflater = LayoutInflater.from(getBaseContext());
                View viewControl = controlInflater.inflate(R.layout.photo_control, null);
                LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
                this.addContentView(viewControl, layoutParamsControl);
                
                // widgets and layouts
                takePictureLayout = (LinearLayout) findViewById(R.id.foto_control_lay3);
                confirmPictureLayout = (LinearLayout) findViewById(R.id.foto_control_lay4);
                takePictureLayout.setVisibility(LinearLayout.VISIBLE);
                confirmPictureLayout.setVisibility(LinearLayout.INVISIBLE);
                
                // create a SurfaceView
                SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.foto_surface1);
                mSurfaceHolder = mSurfaceView.getHolder();
                mSurfaceHolder.addCallback(this);
                mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        
        public void initializeButtons() {
                btnBack = (ImageButton) findViewById(R.id.foto_control_bt_voltar);
                btnTakePicture = (ImageButton) findViewById(R.id.foto_control_bt_fotografar);
                btnCancel = (ImageButton) findViewById(R.id.foto_control_bt_cancel);
                btnOk = (ImageButton) findViewById(R.id.foto_control_bt_ok);
                
                setButtonListeners();
        }
        
        public void setButtonListeners() {
                setButtonOkListener();
                setButtonBackListener();
                setButtonCancelListener();
                setButtonTakePictureListener();
        }
        
        public void setButtonBackListener() { // button listeners
                btnBack.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                                setResult(RESULT_CANCELED, new Intent());
                                finish();
                        }
                });
        }
        
        public void setButtonOkListener() {
                btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                setResult(RESULT_OK, new Intent().putExtra("RESULT", pictureFile.getPath()));
                                finish();
                        }
                });
        }
        
        public void setButtonCancelListener() {
                btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                pictureFile.delete();
                                takePictureLayout.setVisibility(View.VISIBLE);
                                confirmPictureLayout.setVisibility(View.INVISIBLE);
                                btnTakePicture.setEnabled(true);
                                mCamera.startPreview();
                        }
                });
        }
        
        public void setButtonTakePictureListener() {
                btnTakePicture.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                                takePicture();
                        }
                });
                
                // AutoFocus when long click
                btnTakePicture.setOnLongClickListener(new OnLongClickListener() {
                        
                        @Override
                        public boolean onLongClick(View arg0) {
                                mCamera.autoFocus(new AutoFocusCallback() {
                                        
                                        @Override
                                        public void onAutoFocus(
                                                                boolean arg0,
                                                                Camera arg1) { //
                                                takePicture();
                                        }
                                });
                                return true;
                        }
                });
        }
        
        public void setZoomListener() {
                ZoomControls zooming = (ZoomControls) findViewById(R.id.zooming);
                
                Camera.Parameters p = mCamera.getParameters();
                final int maxZoom = p.getMaxZoom();
                
                String zoomString = session.getSavedValue("zoom");
                
                if (zoomString != null) {
                        zoom = Integer.parseInt(zoomString);
                        setZoom(zoom);
                }
                
                zooming.setOnZoomInClickListener(new OnClickListener() {
                        public void onClick(View v) {
                                
                                if (zoom > maxZoom) zoom = maxZoom;
                                
                                if (zoom < maxZoom) {
                                        zoom++;
                                }
                                
                                setZoom(zoom);
                        }
                });
                
                zooming.setOnZoomOutClickListener(new OnClickListener() {
                        public void onClick(View v) {
                                
                                if (zoom < 0) zoom = 0;
                                
                                if (zoom != 0) {
                                        zoom--;
                                }
                                
                                setZoom(zoom);
                        }
                });
        }
        
        public void setZoom(int zoom) {
                Camera.Parameters p = mCamera.getParameters();
                
                if (p.isZoomSupported()) {
                        int maxZoom = p.getMaxZoom();
                        
                        if (zoom <= maxZoom && zoom >= 0) {
                                p.setZoom(zoom);
                                session.saveKeyAndValue("zoom", "" + zoom);
                        }
                }
                
                mCamera.setParameters(p);
        }
        
        /** Check if this device has a camera */
        private boolean checkCameraHardware(Context context) {
                if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                        // this device has a camera
                        return true;
                }
                else {
                        // no camera on this device
                        return false;
                }
        }
        
        /** A safe way to get an instance of the Camera object. */
        public static Camera getCameraInstance() {
                Camera c = null;
                
                try {
                        c = Camera.open(); // attempt to get a Camera instance
                }
                catch (Exception e) {
                        // Camera is not available (in use or does not exist)
                }
                return c; // returns null if camera is unavailable
        }
        
        private void takePicture() {
                System.gc(); //Garbage Collector to improve more ram memory to activity
                
                if (btnTakePicture.isEnabled()) {
                        btnTakePicture.setEnabled(false);
                        mCamera.takePicture(null, null, jpegCallback);
                }
        }
        
        Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
                                                    @Override
                                                    public void onPictureTaken(
                                                                               byte[] _data,
                                                                               Camera _camera) {
                                                            
                                                            pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                                                            
                                                            if (pictureFile == null) {
                                                                    //Log.i(TAG, "Error creating media file, check storage permissions: " + e.getMessage());
                                                                    Log.i(TAG, "Error creating media file, check storage permissions: ");
                                                                    return;
                                                            }
                                                            
                                                            try {
                                                                    FileOutputStream fos = new FileOutputStream(pictureFile);
                                                                    fos.write(_data);
                                                                    fos.close();
                                                                    
                                                                    setResult(RESULT_OK);
                                                                    confirmPicture();
                                                            }
                                                            catch (FileNotFoundException e) {
                                                                    Log.i(TAG, "File not found: " + e.getMessage());
                                                                    setResult(RESULT_CANCELED, new Intent().putExtra("RESULT", "Erro na obtenção da foto!"));
                                                                    finish();
                                                            }
                                                            catch (IOException e) {
                                                                    Log.i(TAG, "Error accessing file: " + e.getMessage());
                                                                    setResult(RESULT_CANCELED, new Intent().putExtra("RESULT", "Erro na obtenção da foto!"));
                                                                    finish();
                                                            }
                                                            
                                                    }
                                            };
        
        private void confirmPicture() {
                mCamera.stopPreview();
                
                takePictureLayout.setVisibility(View.INVISIBLE);
                confirmPictureLayout.setVisibility(View.VISIBLE);
        }
        
        /**
         * Maps the camera button and take the picture when that button was
         * clicked..
         */
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_CAMERA) {
                        this.takePicture();
                }
                return super.onKeyDown(keyCode, event);
        }
        
        /** Create a file Uri for saving an image or video */
        private static Uri getOutputMediaFileUri(int type) {
                return Uri.fromFile(getOutputMediaFile(type));
        }
        
        /** Create a File for saving an image or video */
        private static File getOutputMediaFile(int type) {
                // To be safe, you should check that the SDCard is mounted
                // using Environment.getExternalStorageState() before doing this.
                
                String photosPath = "/inova/" + "/dados" + "/fotos/";
                File mediaStorageDir = new File(Utility.getExternalSdCardPath() + photosPath);
                // This location works best if you want the created images to be shared
                // between applications and persist after your app has been uninstalled.
                
                // Create the storage directory if it does not exist
                if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                                mediaStorageDir = new File(Environment.getExternalStorageDirectory() + photosPath);
                                mediaStorageDir.mkdirs();
                        }
                }
                
                // Create a media file name
                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                File mediaFile;
                
                if (type == MEDIA_TYPE_IMAGE) {
                        Task currentTask = FormActivity.currentTask;
                        
                        if (currentTask != null) {
                                String featureId = currentTask.getAddress().getFeatureId();
                                
                                String fileName = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + "_" + featureId + ".jpg";
                                
                                if (featureId != null) {
                                        mediaFile = new File(fileName);
                                }
                        }
                }
                else if (type == MEDIA_TYPE_VIDEO) {
                        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
                }
                else {
                        return null;
                }
                
                return mediaFile;
        }
        
        public void setCameraQuality() {
                Parameters cameraParameters = mCamera.getParameters();
                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO); // set
                // auto-focus
                
                List<Camera.Size> mList = cameraParameters.getSupportedPictureSizes();
                
                for (Size size : mList) {
                        if (size.width < 2048) {
                                cameraParameters.setPictureSize(size.width, size.height);
                                break;
                        }
                }
                
                /*
                 * 
                 * TODO: analisar antes de colocar em produção!!
                 * 
                 * Camera.Size maxPictureSize = mList.get(0);
                 * 
                 * if (maxPictureSize.width <= 2048 && maxPictureSize.height <=
                 * 1536) { cameraParameters.setPictureSize(maxPictureSize.width,
                 * maxPictureSize.height); } else {
                 * //cameraParameters.setPictureSize(2048, 1536);
                 * cameraParameters.setPictureSize(1984, 1488); //mais próximo
                 * de 3 megapixels... }
                 */
                
                cameraParameters.setPictureFormat(ImageFormat.JPEG);
                cameraParameters.set("jpeg-quality", 50);
                
                try {
                        mCamera.setParameters(cameraParameters);
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
        }
        
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
                try {
                        if (mCamera != null) {
                                setCameraQuality();
                                
                                mCamera.setPreviewDisplay(holder);
                                mCamera.startPreview();
                                
                                setZoomListener();
                                
                        }
                        else {
                                setResult(RESULT_CANCELED, new Intent().putExtra("RESULT", "Erro na obtenção da câmera!"));
                                finish();
                        }
                }
                catch (IOException e) {
                        ExceptionHandler.saveLogFile(e);
                }
        }
        
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
        }
        
        @Override
        public void surfaceChanged(
                                   SurfaceHolder holder,
                                   int format,
                                   int width,
                                   int height) {
                Log.i(TAG, format + " --- " + width + " --- " + height);
        }
        
        @Override
        public void onStart() {
                super.onStart();
        }
        
        @Override
        public void onResume() {
                super.onResume();
        }
        
        @Override
        public void onPause() {
                super.onPause();
        }
        
        @Override
        public void onStop() {
                super.onStop();
        }
        
        @Override
        public void onRestart() {
                super.onRestart();
        }
        
        @Override
        public void onDestroy() {
                super.onDestroy();
        }
}
