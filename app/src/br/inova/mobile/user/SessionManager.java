package br.inova.mobile.user;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import br.inova.mobile.Main;

public class SessionManager {
        // Shared Preferences
        private static SharedPreferences pref;
        
        // Editor for Shared preferences
        private static Editor            editor;
        
        // Context
        private static Context           _context;
        
        // Shared pref mode
        int                              PRIVATE_MODE  = 0;
        
        // Sharedpref file name
        private static final String      PREF_NAME     = "AndroidHivePref";
        
        // All Shared Preferences Keys
        private static final String      IS_LOGIN      = "IsLoggedIn";
        
        public static final String       KEY_NAME      = "name";
        
        public static final String       KEY_HASH      = "hash";
        
        public static final String       KEY_LAST_TASK = "last_task";
        
        public static final String       SESSION_TYPE  = "sessionType";
        
        private static final String      LOG_TAG       = "SESSION_MANAGER";
        
        private static SessionManager    self          = null;
        
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
                clearLoginInformations();
                
                // After logout redirect user to Login Activity
                Intent i = new Intent(_context, Main.class);
                
                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                // Add new Flag to start new Activity
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                // Staring Login Activity
                _context.startActivity(i);
        }
        
        /**
         * Clear all login data from Shared Preferences
         */
        private static void clearLoginInformations() {
                editor.remove(IS_LOGIN);
                editor.remove(KEY_NAME);
                editor.remove(KEY_HASH);
                editor.remove(SESSION_TYPE);
                
                editor.commit();
        }
        
        // Constructor
        private SessionManager(Context context) {
                this._context = context;
                pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                editor = pref.edit();
        }
        
        /**
         * Create login session
         * */
        public void createLoginSession(
                                       String name,
                                       String hash,
                                       String sessionType) {
                editor.putBoolean(IS_LOGIN, true);
                editor.putString(KEY_NAME, name);
                editor.putString(KEY_HASH, hash);
                editor.putString(SESSION_TYPE, sessionType);
                
                editor.commit();
        }
        
        /**
         * Get user hash
         * */
        public String getSessionType() {
                String sessionType = getValue(SESSION_TYPE);
                return sessionType;
        }
        
        public void saveLastTaskId(String lastAddressId) {
                saveKeyAndValue(KEY_LAST_TASK, lastAddressId);
        }
        
        /**
         * Get user hash
         * */
        public String getLastTaskId() {
                String lastAddress = getValue(KEY_LAST_TASK);
                return lastAddress;
        }
        
        /**
         * Get stored session data
         * */
        public HashMap<String, String> getUserDetails() {
                HashMap<String, String> user = new HashMap<String, String>();
                // user name
                user.put(KEY_NAME, getValue(KEY_NAME));
                
                // user hash id
                user.put(KEY_HASH, getValue(KEY_HASH));
                
                // return user
                return user;
        }
        
        /**
         * Get user hash
         * */
        public String getUserHash() {
                String hash = getValue(KEY_HASH);
                
                if (hash == null) {
                        logoutUser();
                }
                
                return hash;
        }
        
        /**
         * Get user name
         * */
        public String getUserName() {
                String name = getValue(KEY_NAME);
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
         * Search key in saved preferences and return the value.
         * */
        public String getValue(String key) {
                pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String value = pref.getString(key, null);
                
                Log.i(LOG_TAG, "Key: " + key + " Value: " + value);
                
                return value;
        }
        
        /**
         * Save map into configurations.
         * */
        public void saveKeyAndValue(String key, String value) {
                pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                editor = pref.edit();
                
                editor.putString(key, value);
                
                boolean isSaved = editor.commit();
                
                if (isSaved) {
                        Log.e("SAVED!!", "KEY: " + key + " Value : " + value);
                }
                else {
                        Log.e("ERROR!!", "KEY: " + key + " Value : " + value);
                }
                
        }
}
