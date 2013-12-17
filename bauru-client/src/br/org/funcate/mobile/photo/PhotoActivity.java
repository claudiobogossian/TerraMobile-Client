package br.org.funcate.mobile.photo;

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
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import br.org.funcate.mobile.R;

public class PhotoActivity extends Activity implements SurfaceHolder.Callback {

    public static final String TAG             = "#FOTO";

    private Camera             mCamera;
    private LinearLayout       lay3, lay4;
    private ImageButton        bt_fotografar;
    private File               pathfullapp;
    private String             photo_name;
    protected LocationManager  locationManager;

    boolean                    previewing      = false;
    LayoutInflater             controlInflater = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // obtain the path for stored photos
        // String pathapp = "/";
        // Cursor c = getContentResolver().query(Provider.Info.CONTENT_URI,
        // new String[] { Provider.Info.PATH }, Provider.Info.CFG1 + "=1",
        // null, null);
        // if (c.moveToFirst()) {
        // pathapp = c.getString(c.getColumnIndex(Provider.Info.PATH));
        // c.close();
        // c = null;
        // } else {
        // setResult(RESULT_CANCELED, new Intent().putExtra("RESULT",
        // "Erro de configuração no aplicativo!"));
        // finish();
        // }
        // pathfullapp = new File(Environment.getExternalStorageDirectory()
        // + pathapp + getString(R.string.path_app_dados)
        // + getString(R.string.path_app_fotos));

        pathfullapp = new File(Environment.getExternalStorageDirectory() + "/funcate/" + "/dados" + "/fotos/");

        if (!pathfullapp.exists()) {
            pathfullapp.mkdirs();
        }
        Log.i(TAG, pathfullapp.toString());

        // create a SurfaceView
        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.foto_surface1);
        SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
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
                bt_fotografar.setEnabled(false);
                takePicture();
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
    }

    private String getPhotoActivityName() {
        return this.photo_name;
    }

    private void setPhotoActivityName(String name) {
        this.photo_name = name;
    }

    private void takePicture() {
        mCamera.takePicture(null, null, jpegCallback);
    }

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
                                            @Override
                                            public void onPictureTaken(byte[] _data, Camera _camera) {
                                                if (_data != null) {
                                                    if (StoreByteImage(PhotoActivity.this, _data, 90)) {
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

    private boolean StoreByteImage(Context mContext, byte[] imageData,
            int quality) {
        String name_file = null;
        try {
            name_file = UUID.randomUUID().toString() + ".jpg";
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
            File fotofile = new File(pathfullapp, name_file);
            FileOutputStream fileOutputStream = new FileOutputStream(fotofile, false);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            myImage.compress(CompressFormat.JPEG, quality, bos);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        setPhotoActivityName(name_file);
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera = Camera.open();
            Parameters cameraParameters = mCamera.getParameters();

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
        } catch (IOException e) {
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
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
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
