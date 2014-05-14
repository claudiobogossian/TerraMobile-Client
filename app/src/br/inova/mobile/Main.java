package br.inova.mobile;

import java.util.Iterator;

import com.j256.ormlite.dao.CloseableIterator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.map.GeoMap;
import br.inova.mobile.photo.Photo;
import br.inova.mobile.photo.PhotoDao;
import br.inova.mobile.user.LoginActivity;
import br.inova.mobile.user.SessionManager;

public class Main extends Activity {
        
        private Main               self   = this;
        
        public static final String TAG    = "#MAIN";
        
        // other activities
        private static final int   GEOMAP = 100;
        
        // Session Manager Class
        SessionManager             session;
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
                /**
                 * Defines the default exception handler to log unexpected
                 * android errors
                 */
                
                //Utility.getDistanceFromPoints();        
                
                //Utility.disableStrictMode();
                //String json = "[{\"address\":{\"name\":\"RUA JOAO ABO ARRAGE\",\"id\":18294,\"state\":\"SP\",\"number\":\"3-0\",\"extra\":\"LA Q9 G AZEV\",\"coordx\":-49.0637205940752,\"coordy\":-22.3368180451296,\"postalCode\":\"17012350\",\"city\":\"Bauru\",\"featureId\":\"020226001001\",\"neighborhood\":\"VILA GUEDES DE AZEVEDO\"},\"id\":18436,\"done\":false,\"form\":{\"id\":18294,\"date\":null,\"coordx\":null,\"coordy\":null,\"info1\":null,\"info2\":null,\"numberConfirmation\":null,\"variance\":null,\"otherNumbers\":null,\"primaryUse\":null,\"secondaryUse\":null,\"pavimentation\":null,\"asphaltGuide\":null,\"publicIlumination\":null,\"energy\":null,\"pluvialGallery\":null},\"user\":{\"name\":\"RAUL\",\"id\":1021,\"password\":\"81dc9bdb52d04dc20036dbd8313ed055\",\"hash\":\"b3d48c9573660968438e4a7c12daac4c\",\"login\":\"RAUL\"}}]";
                //String getUrl = "http://179.184.164.144:8000/terramobile-server/rest/tasks?user=b3d48c9573660968438e4a7c12daac4c";
                //HttpClient.get(getUrl);
                //String postUrl = "http://192.168.1.102:8080/bauru-server/rest/tasks?user=b3d48c9573660968438e4a7c12daac4c";
                //HttpClient.post(postUrl , json);
                
                //startActivity(new Intent(this, ListTasks.class));
                                
                session = SessionManager.getInstance();
                this.checkLogin();
        }
        
        /**
         * Check login method wil check user login status If false it will
         * redirect user to login page Else won't do anything
         * */
        public void checkLogin() {
                
                boolean isLoggedIn = session.isLoggedIn();
                Intent intent = null;
                
                if (isLoggedIn) {
                        intent = new Intent(this, GeoMap.class);
                }
                else {
                        // user is not logged in redirect him to Login Activity
                        intent = new Intent(this, LoginActivity.class);
                }
                
                // Closing all the Activities
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                // Add new Flag to start new Activity
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                // Staring Login Activity
                this.startActivity(intent);
                finish();
        }
        
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                return true;
        }
        
        // @Override
        // public void onActivityResult(int requestCode, int resultCode, Intent
        // data) {
        // if (requestCode == GEOMAP) {
        // if (resultCode == RESULT_OK) {
        // } else if (resultCode == RESULT_CANCELED) {
        // }
        // }
        // }
        
}
