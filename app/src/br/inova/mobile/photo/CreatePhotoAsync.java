package br.inova.mobile.photo;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.support.Base64;

import android.location.Location;
import android.os.AsyncTask;
import android.widget.Toast;
import br.inova.mobile.Utility;
import br.inova.mobile.form.FormActivity;
import br.inova.mobile.location.LocationProvider;

public class CreatePhotoAsync extends AsyncTask<String, String, String> {
        
        private FormActivity formActivity;
        private String       photoPath;
        
        public CreatePhotoAsync(String photoPath, FormActivity formActivity) {
                this.photoPath = photoPath;
                this.formActivity = formActivity;
                
                this.execute();
        }
        
        @Override
        protected String doInBackground(String... arg0) {
                String blob = getBytesFromImage(photoPath);
                return blob;
        }
        
        /**
         * 
         * Returns Base64 String from filePath of a photo.
         * 
         * @param String
         *                filePath The path of the image that you want to get
         *                the base.
         * 
         * */
        public String getBytesFromImage(final String filePath) {
                String imgString = null;
                
                byte[] bytes;
                byte[] buffer = new byte[8192];
                
                int bytesRead;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                
                try {
                        System.gc();
                        InputStream inputStream = new FileInputStream(filePath);//You can get an inputStream using any IO API
                        
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                        }
                        
                        bytes = output.toByteArray();
                        imgString = Base64.encodeBytes(bytes);
                }
                catch (IOException e) {
                        e.printStackTrace();
                }
                catch (OutOfMemoryError e) {
                        e.printStackTrace();
                }
                
                return imgString;
        }
        
        @Override
        protected void onPostExecute(String blob) {
                Photo photo = new Photo();
                photo.setBase64(blob);
                
                photo.setPath(photoPath);
                photo.setForm(formActivity.currentTask.getForm());
                
                LocationProvider locationProvider = LocationProvider.getInstance(formActivity);
                Location location = locationProvider.getLocation();
                
                if (location != null) {
                        formActivity.lat.setText("" + location.getLatitude());
                        formActivity.lon.setText("" + location.getLongitude());
                }
                else {
                        if (!locationProvider.isGpsEnabled()) {
                                Utility.showToast("Seu GPS está desabilitado, ligue-o para capturar sua posição.", Toast.LENGTH_LONG, formActivity);
                        }
                        
                        formActivity.lat.setText("0.0");
                        formActivity.lon.setText("0.0");
                }
                
                formActivity.photos.add(photo);
                formActivity.showPictures(formActivity.photos);
                
        }
}
