package br.inova.mobile.constants;

import android.content.Context;
import android.util.Log;
import br.inova.mobile.Utility;
import br.inova.mobile.user.SessionManager;
import br.inpe.mobile.R.string;

public class Constants {
        
        /*** Production ***/
        public static String  EXTERNAL_HOST      = "http://179.184.164.144";
        public static String  INTERNAL_HOST      = "http://192.168.0.181";
        
        public static String  PORT               = "8000";
        public static String  SERVER             = "terramobile-server";
        
        public static String  EXTERNAL_HOST_URL  = EXTERNAL_HOST + ":" + PORT + "/" + SERVER + "/";
        public static String  INTERNAL_HOST_URL  = INTERNAL_HOST + ":" + PORT + "/" + SERVER + "/";
        
        public static String  USER_REST          = "rest/users";
        public static String  TASKS_REST         = "rest/tasks?user=";
        public static String  PHOTOS_REST        = "rest/photos?user=";
        public static String  ZIP_REST           = "rest/tiles/zip";
        public static String  LEVEL_QUERY_STRING = "?level=";
        
        public static Boolean ISPRODUCTION       = true;
        public static Boolean ISDEBUG            = false;
        
        private static void applyChanges() {
                EXTERNAL_HOST_URL = EXTERNAL_HOST + ":" + PORT + "/" + SERVER + "/";
                INTERNAL_HOST_URL = INTERNAL_HOST + ":" + PORT + "/" + SERVER + "/";
        }
        
        public static void changeServerMode(
                                            Context context,
                                            String spnLoginUrlString) {
                
                String productionString = context.getResources().getString(string.production);
                String homologString = context.getResources().getString(string.homolog);
                String presentationString = context.getResources().getString(string.presentation);
                String debugString = context.getResources().getString(string.debug);
                
                if (spnLoginUrlString != null) {
                        if (spnLoginUrlString.equals(productionString)) {
                                Constants.changeToProductionMode();
                        }
                        else if (spnLoginUrlString.equals(homologString)) {
                                Constants.changeToHomologMode();
                        }
                        else if (spnLoginUrlString.equals(presentationString)) {
                                Constants.changeToPresentationMode();
                        }
                        else if (spnLoginUrlString.equals(debugString)) {
                                Constants.changeToDebugMode();
                        }
                }
                
        }
        
        public static void changeToDebugMode() {
                /*** DEBUG ***/
                EXTERNAL_HOST = "http://192.168.0.171";
                INTERNAL_HOST = "http://192.168.0.171";
                SERVER = "terramobile-server";
                ISDEBUG = true;
                ISPRODUCTION = false;
                
                Log.i("CONSTANTS", "Entrando em modo debug.");
                
                applyChanges();
        }
        
        public static void changeToHomologMode() {
                /*** Semi-Production ***/
                
                EXTERNAL_HOST = "http://179.184.164.144";
                INTERNAL_HOST = "http://192.168.0.181";
                SERVER = "terramobile-homolog";
                ISPRODUCTION = false;
                
                Log.i("CONSTANTS", "Entrando em modo Homologação.");
                
                applyChanges();
        }
        
        public static void changeToPresentationMode() {
                /*** Semi-Production ***/
                EXTERNAL_HOST = "http://179.184.164.144";
                INTERNAL_HOST = "http://192.168.0.181";
                SERVER = "terramobile-presentation";
                ISPRODUCTION = false;
                
                Log.i("CONSTANTS", "Entrando em modo Apresentação.");
                
                applyChanges();
        }
        
        public static void changeToProductionMode() {
                /*** Production ***/
                EXTERNAL_HOST = "http://179.184.164.144";
                INTERNAL_HOST = "http://192.168.0.181";
                SERVER = "terramobile-server";
                ISPRODUCTION = true;
                
                Log.i("CONSTANTS", "Entrando em modo Produção.");
                
                applyChanges();
        }
        
        public static String getPhotosUrl() {
                String userHash = SessionManager.getInstance().getUserHash();
                String url = Utility.getServerUrl() + PHOTOS_REST + userHash;
                return url;
        }
        
        public static String getTasksUrl() {
                String userHash = SessionManager.getInstance().getUserHash();
                String url = Utility.getServerUrl() + TASKS_REST + userHash;
                return url;
        }
}
