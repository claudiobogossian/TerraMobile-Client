package br.inpe.mobile.photo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
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
import br.inpe.mobile.R;
import br.inpe.mobile.exception.ExceptionHandler;
import br.inpe.mobile.user.SessionManager;

public class PhotoActivity extends Activity implements SurfaceHolder.Callback {
    
    public static final String TAG             = "#FOTO";
    
    private Camera             mCamera;
    
    private LinearLayout       lay3, lay4;
    
    private ImageButton        bt_fotografar;
    
    private File               pathfullapp;
    
    private String             photo_name;
    
    private SurfaceHolder      mSurfaceHolder;
    
    private static Integer     zoom            = 0;
    
    private SessionManager     session;
    
    LayoutInflater             controlInflater = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_photo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFormat(PixelFormat.UNKNOWN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.photo_control, null);
        LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);
        
        // widgets and layouts
        lay3 = (LinearLayout) findViewById(R.id.foto_control_lay3);
        lay4 = (LinearLayout) findViewById(R.id.foto_control_lay4);
        lay3.setVisibility(LinearLayout.VISIBLE);
        lay4.setVisibility(LinearLayout.INVISIBLE);
        ImageButton bt_voltar = (ImageButton) findViewById(R.id.foto_control_bt_voltar);
        bt_fotografar = (ImageButton) findViewById(R.id.foto_control_bt_fotografar);
        ImageButton bt_cancel = (ImageButton) findViewById(R.id.foto_control_bt_cancel);
        ImageButton bt_ok = (ImageButton) findViewById(R.id.foto_control_bt_ok);
        
        pathfullapp = new File(Environment.getExternalStorageDirectory() + "/inpe/" + "/dados" + "/fotos/");
        
        if (!pathfullapp.exists()) {
            pathfullapp.mkdirs();
        }
        Log.i(TAG, pathfullapp.toString());
        
        // create a SurfaceView
        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.foto_surface1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        // button listeners
        bt_voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, new Intent());
                finish();
            }
        });
        
        bt_fotografar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        
        // AutoFocus when long click
        bt_fotografar.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                mCamera.autoFocus(new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean arg0, Camera arg1) {
                        //takePicture();
                    }
                });
                return true;
            }
        });
        
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(pathfullapp, getPhotoActivityName());
                file.delete();
                lay3.setVisibility(View.VISIBLE);
                lay4.setVisibility(View.INVISIBLE);
                bt_fotografar.setEnabled(true);
                mCamera.startPreview();
            }
        });
        
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent().putExtra("RESULT", pathfullapp + "/" + getPhotoActivityName()));
                finish();
            }
        });
        
        session = SessionManager.getInstance();
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
    
    private String getPhotoActivityName() {
        return this.photo_name;
    }
    
    private void setPhotoActivityName(String name) {
        this.photo_name = name;
    }
    
    private void takePicture() {
        if (bt_fotografar.isEnabled()) {
            bt_fotografar.setEnabled(false);
            mCamera.takePicture(null, null, jpegCallback);
        }
    }
    
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(
                                   byte[] _data,
                                   Camera _camera) {
            if (_data != null) {
                
                boolean isStored = storeByteImage(PhotoActivity.this, _data, 90);
                
                if (isStored) {
                    setResult(RESULT_OK);
                    confirmPicture();
                }
                else {
                    setResult(RESULT_CANCELED, new Intent().putExtra("RESULT", "Erro ao salvar foto!"));
                    finish();
                }
            }
            else {
                setResult(RESULT_CANCELED, new Intent().putExtra("RESULT", "Erro na obtenção da foto!"));
                finish();
            }
        }
    };
    
    private void confirmPicture() {
        lay3.setVisibility(View.INVISIBLE);
        lay4.setVisibility(View.VISIBLE);
    }
    
    // https://developer.android.com/training/displaying-bitmaps/index.html
    private boolean storeByteImage(
                                   Context mContext,
                                   byte[] imageData,
                                   int quality) {
        String fileName = null;
        boolean isStored = false;
        
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            
            fileName = UUID.randomUUID().toString() + ".jpg";
            File photoFile = new File(pathfullapp, fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(photoFile, false);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            
            Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
            bitmapImage.compress(CompressFormat.JPEG, quality, bufferedOutputStream);
            
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            
            setPhotoActivityName(fileName);
            
            isStored = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return isStored;
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
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera = Camera.open();
            Parameters cameraParameters = mCamera.getParameters();
            
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO); // set auto-focus
            
            List<Camera.Size> mList = cameraParameters.getSupportedPictureSizes();
            Camera.Size maxPictureSize = mList.get(mList.size() - 1);
            
            if (maxPictureSize.width <= 2048 && maxPictureSize.height <= 1536) {
                cameraParameters.setPictureSize(maxPictureSize.width, maxPictureSize.height);
            }
            else {
                cameraParameters.setPictureSize(2048, 1536);
            }
            
            cameraParameters.setPictureFormat(PixelFormat.JPEG);
            cameraParameters.set("jpeg-quality", 100);
            mCamera.setParameters(cameraParameters);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            
            setZoomListener();
        }
        catch (IOException e) {
            e.printStackTrace();
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
        lay3.setVisibility(View.VISIBLE);
        lay4.setVisibility(View.INVISIBLE);
        bt_fotografar.setEnabled(true);
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
