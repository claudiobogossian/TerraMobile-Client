package br.inpe.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import br.inpe.mobile.exception.ExceptionHandler;
import br.inpe.mobile.map.GeoMap;
import br.inpe.mobile.user.LoginActivity;
import br.inpe.mobile.user.SessionManager;

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
