package br.inova.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import br.inova.mobile.constants.Constants;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.map.GeoMap;
import br.inova.mobile.user.LoginActivity;
import br.inova.mobile.user.SessionManager;

public class Main extends Activity {
        
        // Session Manager Class
        SessionManager session;
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
                /**
                 * Defines the default exception handler to log unexpected
                 * android errors
                 */
                
                if (Utility.isInDebug(this)) {
                        Constants.changeToDebugMode();
                }
                
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
        
}
