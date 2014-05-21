package br.inova.mobile.constants;

public class Constants {
        
        /*** Production ***/
        public static String EXTERNAL_HOST      = "http://179.184.164.144";
        public static String INTERNAL_HOST      = "http://192.168.0.181";
        
        public static String PORT               = "8000";
        public static String SERVER             = "terramobile-server";
        
        public static String EXTERNAL_HOST_URL  = EXTERNAL_HOST + ":" + PORT + "/" + SERVER + "/";
        public static String INTERNAL_HOST_URL  = INTERNAL_HOST + ":" + PORT + "/" + SERVER + "/";
        
        public static String USER_REST          = "rest/users";
        public static String TASKS_REST         = "rest/tasks?user={user_hash}";
        public static String PHOTOS_REST        = "rest/photos?user={user_hash}";
        public static String ZIP_REST           = "rest/tiles/zip";
        public static String LEVEL_QUERY_STRING = "?level=";
        
        public static void changeToDebugMode() {
                /*** DEBUG ***/
                EXTERNAL_HOST = "http://192.168.0.171";
                INTERNAL_HOST = "http://192.168.0.171";
                applyChanges();
        }
        
        public static void changeToProductionMode() {
                /*** Production ***/
                EXTERNAL_HOST = "http://179.184.164.144";
                INTERNAL_HOST = "http://192.168.0.181";
                applyChanges();
        }
        
        public static void changeToHomologMode() {
                /*** Semi-Production ***/
                EXTERNAL_HOST = "http://192.168.0.171"; //TODO: mudar url de homologação.
                INTERNAL_HOST = "http://192.168.0.171";
                //PORT = "8000";
                //SERVER = "terramobile-homolog";
                applyChanges();
        }
        
        private static void applyChanges() {
                EXTERNAL_HOST_URL = EXTERNAL_HOST + ":" + PORT + "/" + SERVER + "/";
                INTERNAL_HOST_URL = INTERNAL_HOST + ":" + PORT + "/" + SERVER + "/";
        }
}
