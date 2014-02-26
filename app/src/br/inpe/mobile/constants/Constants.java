package br.inpe.mobile.constants;

public interface Constants {
        //public static String       HOST               = "http://192.168.0.181";                //local URL
        //public static String       HOST               = "http://localhost";                //Debug URL
        public static final String HOST               = "http://institutosoma.dyndns.org";
        public static final String PORT               = "8000";
        public static final String SERVER             = "terramobile-server";
        public static final String HOST_URL           = HOST + ":" + PORT + "/" + SERVER + "/";
        public static final String USER_REST          = "rest/users";
        public static final String TASKS_REST         = "rest/tasks?user={user_hash}";
        public static final String PHOTOS_REST        = "rest/photos?user={user_hash}";
        public static final String ZIP_REST           = "rest/tiles/zip";
        public static final String LEVEL_QUERY_STRING = "?level=";
}
