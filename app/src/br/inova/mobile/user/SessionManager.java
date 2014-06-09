package br.inova.mobile.user;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import br.inova.mobile.Main;

public class SessionManager {
        // Shared Preferences
        private static SharedPreferences pref;
        
        // Editor for Shared preferences
        private static Editor            editor;
        
        // Context
        private static Context           _context;
        
        // Shared pref mode
        int                              PRIVATE_MODE = 0;
        
        // Sharedpref file name
        private static final String      PREF_NAME    = "AndroidHivePref";
        
        // All Shared Preferences Keys
        private static final String      IS_LOGIN     = "IsLoggedIn";
        
        public static final String       KEY_NAME     = "name";
        
        public static final String       KEY_HASH     = "hash";
        
        public static final String       SESSION_TYPE = "sessionType";
        
        private static SessionManager    self         = null;
        
        /**
         * Application Memory class creates this Object.
         * */
        public static SessionManager createSession(Context applicationContext) {
                if (self == null) {
                        self = new SessionManager(applicationContext);
                }
                
                return getInstance();
        }
        
        public static SessionManager getInstance() {
                return self;
        }
        
        /**
         * Clear session details
         * */
        public static void logoutUser() {
                // Clearing all data from Shared Preferences
                editor.clear();
                editor.commit();
                
                // After logout redirect user to Login Activity
                Intent i = new Intent(_context, Main.class);
                
                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                // Add new Flag to start new Activity
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                // Staring Login Activity
                _context.startActivity(i);
        }
        
        // Constructor
        private SessionManager(Context context) {
                this._context = context;
                pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
                editor = pref.edit();
        }
        
        /**
         * Create login session
         * */
        public void createLoginSession(
                                       String name,
                                       String hash,
                                       String sessionType) {
                // Storing login value as TRUE
                editor.putBoolean(IS_LOGIN, true);
                
                // Storing name in pref
                editor.putString(KEY_NAME, name);
                
                // Storing hash in pref
                editor.putString(KEY_HASH, hash);
                
                editor.putString(SESSION_TYPE, sessionType);
                
                // commit changes
                editor.commit();
        }
        
        /**
         * Search key in saved preferences and return the value.
         * */
        public String getSavedValue(String key) {
                String result = pref.getString(key, null);
                return result;
        }
        
        /**
         * Get user hash
         * */
        public String getSessionType() {
                String sessionType = pref.getString(SESSION_TYPE, null);
                return sessionType;
        }
        
        /**
         * Get stored session data
         * */
        public HashMap<String, String> getUserDetails() {
                HashMap<String, String> user = new HashMap<String, String>();
                // user name
                user.put(KEY_NAME, pref.getString(KEY_NAME, null));
                
                // user hash id
                user.put(KEY_HASH, pref.getString(KEY_HASH, null));
                
                // return user
                return user;
        }
        
        /**
         * Get user hash
         * */
        public String getUserHash() {
                String hash = pref.getString(KEY_HASH, null);
                
                if (hash == null) {
                        logoutUser();
                }
                
                return hash;
        }
        
        /**
         * Get user name
         * */
        public String getUserName() {
                String name = pref.getString(KEY_NAME, null);
                return name;
        }
        
        /**
         * Quick check for login
         * **/
        // Get Login State
        public boolean isLoggedIn() {
                Boolean isLogged = pref.getBoolean(IS_LOGIN, false);
                return isLogged;
        }
        
        /**
         * Save map into configurations.
         * */
        public void saveKeyAndValue(String key, String value) {
                // Storing hash in pref
                editor.putString(key, value);
                
                // commit changes
                editor.commit();
        }
}
