package br.inova.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osmdroid.util.GeoPoint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import br.inova.mobile.constants.Constants;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.task.Task;
import br.inova.mobile.task.TaskDao;

import com.j256.ormlite.dao.CloseableIterator;

public class Utility {
        
        private final static String TAG         = "#UTILITY";
        private static final int    TWO_MINUTES = 1000 * 60 * 2;
        
        /**
         * Calculate the distance between two geopoints (Latitude and Longitude)
         * in WGS84
         * 
         * @param latitudeOne
         * @param longituteOne
         * @param latituteTwo
         * @param longituteTwo
         * 
         * @return distance the distance in kilometers
         */
        public static double calculateDistance(
                                               double latitudeOne,
                                               double longituteOne,
                                               double latituteTwo,
                                               double longituteTwo) {
                double AVERAGE_RADIUS_OF_EARTH = 6371;
                
                double latDistance = Math.toRadians(latitudeOne - latituteTwo);
                double lngDistance = Math.toRadians(longituteOne - longituteTwo);
                
                /** Original **/
                double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) + (Math.cos(Math.toRadians(latitudeOne))) * (Math.cos(Math.toRadians(latituteTwo))) * (Math.sin(lngDistance / 2)) * (Math.sin(lngDistance / 2));
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double distance = AVERAGE_RADIUS_OF_EARTH * c;
                
                return distance;
        }
        
        /**
         * Calculate the distance between two geopoints (Latitude and Longitude)
         * in WGS84
         * 
         * @param latitudeOne
         * @param longituteOne
         * @param latituteTwo
         * @param longituteTwo
         * 
         * @return distance the distance in kilometers
         */
        public static double calculateDistanceBira(
                                                   double latitudeOne,
                                                   double longituteOne,
                                                   double latituteTwo,
                                                   double longituteTwo) {
                double AVERAGE_RADIUS_OF_EARTH = 6371;
                
                double latDistance = Math.toRadians(latitudeOne - latituteTwo);
                double lngDistance = Math.toRadians(longituteOne - longituteTwo);
                
                /** Bira **/
                Double distance = Math.sqrt(AVERAGE_RADIUS_OF_EARTH * AVERAGE_RADIUS_OF_EARTH * (Math.tan(latDistance) * Math.tan(latDistance) + Math.tan(lngDistance) * Math.tan(lngDistance)));
                
                return distance;
        }
        
        /**
         * 
         * Move a file from a path to another.
         * 
         * @param inputPath
         *                the input file path.
         * @param outputPath
         *                the destination folder.
         * @param fileName
         *                the name of the file,
         * 
         * */
        public static void copyFile(
                                    String inputPath,
                                    String outputPath,
                                    String fileName) {
                InputStream in = null;
                OutputStream out = null;
                
                Boolean inputFileExists = new File(inputPath).exists();
                
                if (inputFileExists) {
                        try {
                                //create output directory if it doesn't exist
                                File dir = new File(outputPath);
                                
                                if (!dir.exists()) {
                                        dir.mkdirs();
                                }
                                
                                in = new FileInputStream(inputPath);
                                out = new FileOutputStream(outputPath + fileName);
                                
                                byte[] buffer = new byte[1024];
                                int read;
                                
                                while ((read = in.read(buffer)) != -1) {
                                        out.write(buffer, 0, read);
                                }
                                
                                in.close();
                                in = null;
                                
                                // write the output file
                                out.flush();
                                out.close();
                                out = null;
                        }
                        catch (Exception e) {
                                ExceptionHandler.saveLogFile(e);
                        }
                }
        }
        
        /**
         * Get an String and change their value if it's null.
         * 
         * @param correct
         * @return String the modified string.
         */
        public static String correctNull(String correct) {
                if (correct == null || correct.compareTo("") == 0) {
                        correct = "---";
                }
                return correct;
        }
        
        /**
         * Count characters in a string
         * 
         * @param str
         *                string to count characters
         * @param cha
         *                character you want to counted
         * @param idx
         *                index for iniciate the search
         * 
         */
        public static int countCharacterString(String str, String cha, int idx) {
                int idxfound = str.indexOf(cha, idx);
                if (idxfound != -1) { return 1 + countCharacterString(str, cha, idxfound + 1); }
                return 0;
        }
        
        /**
         * Create one GeoPoint for location
         * 
         * @param loc
         *                location for creating GeoPoint
         * 
         */
        public static GeoPoint createGeoPoint(Location loc) {
                int lat = (int) (loc.getLatitude() * 1E6);
                int lng = (int) (loc.getLongitude() * 1E6);
                return new GeoPoint(lat, lng);
        }
        
        /**
         * Create directory if he doesn't exists.
         * 
         * @param String
         *                location The path of the folder.
         */
        public static void dirChecker(String location) {
                File f = new File(location);
                
                if (!f.isDirectory()) {
                        f.mkdirs();
                }
        }
        
        /**
         * Disable the Strict mode to use Network on the main thread.
         * */
        public static void disableStrictMode() {
                if (android.os.Build.VERSION.SDK_INT > 9) {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                }
        }
        
        /**
         * 
         * 
         * @param imgUrl
         * @return
         */
        public static Bitmap downloadRemoteImage(String imgUrl) {
                try {
                        URL myFileUrl = new URL(imgUrl);
                        
                        HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        return BitmapFactory.decodeStream(is);
                        
                }
                catch (Exception ex) {
                        Log.e(TAG, "Error downloadRemoteFile: " + ex);
                }
                return null;
        }
        
        /**
         * Calling GPS Configuration
         * 
         * @param context
         *                context of application
         */
        public static void enableGPS(Context context) {
                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                Utility.showToast("Pressione [back] para voltar", Toast.LENGTH_SHORT, context);
                context.startActivity(gpsOptionsIntent);
        }
        
        /**
         * Calling Network Configuration
         * 
         * @param context
         *                context of application
         */
        public static void enableNETWORK(Context context) {
                Intent wifiOptionsIntent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                Utility.showToast("Pressione [back] para voltar", Toast.LENGTH_SHORT, context);
                context.startActivity(wifiOptionsIntent);
        }
        
        /**
         * Calling Storage Configuration
         * 
         * @param context
         *                context of application
         */
        public static void enableSDCARD(Context context) {
                Intent sdcardOptionsIntent = new Intent(android.provider.Settings.ACTION_MEMORY_CARD_SETTINGS);
                Utility.showToast("Pressione [back] para voltar", Toast.LENGTH_SHORT, context);
                context.startActivity(sdcardOptionsIntent);
        }
        
        /**
         * 
         * 
         * @param searchAddress
         * @param context
         * @return
         * @throws IOException
         */
        public static List<Address> findGeocode(
                                                String searchAddress,
                                                Context context) throws IOException {
                List<Address> findGeocode = new Geocoder(context).getFromLocationName(searchAddress, 1);
                return findGeocode;
        }
        
        /**
         * Format a string postal code to better visible string.
         * 
         * @param cepvalue
         * @return
         */
        public static String formatCep(String cepvalue) {
                if (cepvalue == null || cepvalue.compareTo("") == 0) {
                        return cepvalue;
                }
                else if (cepvalue.length() == 8) { return cepvalue.substring(0, 5) + "-" + cepvalue.substring(5, 8); }
                return cepvalue;
        }
        
        /**
         * Formats the date in the form of inclusion in database
         * 
         * @param date
         *                date to format
         */
        @SuppressLint("SimpleDateFormat")
        public static String formatDate(Date date) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        }
        
        /**
         * 
         * 
         * @param scorevalue
         * @return
         */
        public static String formatScore(int scorevalue) {
                String result = "" + scorevalue;
                
                if (scorevalue > 9999) {
                        result = "0" + scorevalue;
                }
                else if (scorevalue > 999) {
                        result = "00" + scorevalue;
                }
                else if (scorevalue > 99) {
                        result = "000" + scorevalue;
                }
                else if (scorevalue > 9) {
                        result = "0000" + scorevalue;
                }
                else if (scorevalue <= 9) {
                        result = "00000" + scorevalue;
                }
                
                return result;
        }
        
        /**
         * 
         * Generate MD5 hash of a string.
         * 
         * @param String
         *                text
         * @return String result
         */
        public static String generateHashMD5(String text) {
                String result = "";
                MessageDigest md;
                try {
                        md = MessageDigest.getInstance("MD5");
                        md.update(text.getBytes());
                        byte[] hashMd5 = md.digest();
                        result = stringHexa(hashMd5);
                }
                catch (NoSuchAlgorithmException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                return result;
        }
        
        /**
         * 
         * Gets the device ID.
         * 
         * @return String the device id
         */
        public static String getDeviceId(Context ctx) {
                TelephonyManager tManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                return tManager.getDeviceId();
        }
        
        public static void getDistanceFromPoints() {
                CloseableIterator<Task> tasks = TaskDao.getIteratorForAllTasksForCurrentUser();
                
                /*
                 * Log.i("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA 94.75 ?",
                 * "Distance: " + calculateDistance(-23.234169, -45.847321,
                 * -23.56438, -46.632843));
                 * Log.i("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA 12.65 ?",
                 * "Distance: " + calculateDistance(-23.234169, -45.847321,
                 * -23.302098, -45.938644));
                 * 
                 * Log.i("LAT LONG: ", "LAT1 : " + -23.234169 + "LAT2 : " +
                 * -45.847321 + "LON1 : " + -23.56438 + "LON2 : " + -46.632843);
                 * Log.i("COMPARISON: ", "Distance: " +
                 * calculateDistance(-23.234169, -45.847321, -23.302098,
                 * -45.938644));
                 * 
                 * Log.i("\n\nLAT LONG: ", "LAT1 : " + latitude1 + "LAT2 : " +
                 * latitude2 + "LON1 : " + longitute1 + "LON2 : " + longitute2);
                 * Log.i("ORIGINAL: ", "" + calculateDistance(latitude1,
                 * longitute1, latitude2, longitute2)); Log.i("BIRA: ", "" +
                 * calculateDistanceBira(latitude1, longitute1, latitude2,
                 * longitute2));
                 */
                
                while (tasks.hasNext()) {
                        Task task = tasks.next();
                        
                        double latitude1 = task.getAddress().getCoordy();
                        double longitute1 = task.getAddress().getCoordx();
                        
                        if (tasks.hasNext()) {
                                Task task2 = tasks.next();
                                
                                double latitude2 = task2.getAddress().getCoordy();
                                double longitute2 = task2.getAddress().getCoordx();
                                
                                Log.i("\n\nLAT LONG: ", "LAT1 : " + latitude1 + "LAT2 : " + latitude2 + "LON1 : " + longitute1 + "LON2 : " + longitute2);
                                Log.i("ORIGINAL: ", "" + calculateDistance(latitude1, longitute1, latitude2, longitute2));
                                Log.i("BIRA: ", "" + calculateDistanceBira(latitude1, longitute1, latitude2, longitute2));
                        }
                }
        }
        
        public static String getExternalSdCardPath() {
                String path = null;
                
                File sdCardFile = null;
                List<String> sdCardPossiblePath = Arrays.asList("external_sd", "ext_sd", "external", "extSdCard");
                
                for (String sdPath : sdCardPossiblePath) {
                        File file = new File("/mnt/", sdPath);
                        
                        if (file.isDirectory() && file.canWrite()) {
                                path = file.getAbsolutePath();
                                
                                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                                File testWritable = new File(path, "test_" + timeStamp);
                                
                                if (testWritable.mkdirs()) {
                                        testWritable.delete();
                                }
                                else {
                                        path = null;
                                }
                        }
                }
                
                if (path != null) {
                        sdCardFile = new File(path);
                }
                else {
                        sdCardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                }
                
                return sdCardFile.getAbsolutePath();
        }
        
        /**
         * 
         * Ping to the server and verify if the service's (url) running.
         * 
         * @return the soma's server url
         */
        public static String getServerUrl() {
                String serverUrl = null;
                HttpURLConnection urlConnection = null;
                
                try {
                        disableStrictMode();
                        
                        URL url = new URL(Constants.INTERNAL_HOST_URL);
                        
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestProperty("Connection", "close");
                        urlConnection.setConnectTimeout(1000 * 2); // mTimeout is in seconds
                        urlConnection.connect();
                        
                        if (urlConnection.getResponseCode() == 200) {
                                serverUrl = Constants.INTERNAL_HOST_URL;
                        }
                        else {
                                serverUrl = Constants.EXTERNAL_HOST_URL;
                        }
                        
                }
                catch (Exception exception) {
                        serverUrl = Constants.EXTERNAL_HOST_URL;
                }
                finally {
                        if (urlConnection != null) {
                                urlConnection.disconnect();
                        }
                }
                
                return serverUrl;
        }
        
        /**
         * Determines whether one Location reading is better than the current
         * Location fix
         * 
         * @param location
         *                The new Location that you want to evaluate
         * @param currentBestLocation
         *                The current Location fix, to which you want to compare
         *                the new one
         */
        public static boolean isBetterLocation(
                                               Location location,
                                               Location currentBestLocation) {
                if (currentBestLocation == null) {
                        // A new location is always better than no location
                        return true;
                }
                
                // Check whether the new location fix is newer or older
                long timeDelta = location.getTime() - currentBestLocation.getTime();
                boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
                boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
                boolean isNewer = timeDelta > 0;
                
                // If it's been more than two minutes since the current location, use
                // the new location
                // because the user has likely moved
                if (isSignificantlyNewer) {
                        return true;
                }
                else if (isSignificantlyOlder) { return false; }
                
                // Check whether the new location fix is more or less accurate
                int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
                boolean isLessAccurate = accuracyDelta > 0;
                boolean isMoreAccurate = accuracyDelta < 0;
                boolean isSignificantlyLessAccurate = accuracyDelta > 200;
                
                // Check if the old and new location are from the same provider
                boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
                
                // Determine location quality using a combination of timeliness and
                // accuracy
                if (isMoreAccurate) {
                        return true;
                }
                else if (isNewer && !isLessAccurate) {
                        return true;
                }
                else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) { return true; }
                return false;
        }
        
        public static boolean isInDebug(Context context) {
                boolean isDebuggable = (0 != (context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
                return isDebuggable;
        }
        
        /**
         * Determines whether the Network is available
         * 
         * @param context
         *                context of the implementation
         */
        public static boolean isNetworkAvailable(Context context) {
                Boolean isInternetAvailable = false;
                
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                        isInternetAvailable = true;
                }
                
                return isInternetAvailable;
        }
        
        /**
         * Checks whether two providers are the same
         * 
         * @param provider1
         *                first provider comparison
         * @param provider2
         *                second provider comparison
         */
        private static boolean isSameProvider(String provider1, String provider2) {
                if (provider1 == null) { return provider2 == null; }
                return provider1.equals(provider2);
        }
        
        /**
         * Determines if the sdcard is available for reading and writing
         * 
         * @return
         */
        public static boolean isSdcardAvaliable() {
                String state = Environment.getExternalStorageState();
                
                if (Environment.MEDIA_MOUNTED.equals(state)) { return true; }
                return false;
        }
        
        /**
         * 
         * Move a file from a path to another.
         * 
         * @param inputPath
         *                the input file path.
         * @param outputPath
         *                the destination folder.
         * @param fileName
         *                the name of the file,
         * 
         * */
        public static void moveFile(
                                    String inputPath,
                                    String outputPath,
                                    String fileName) {
                InputStream in = null;
                OutputStream out = null;
                
                Boolean inputFileExists = new File(inputPath).exists();
                
                if (inputFileExists) {
                        try {
                                //create output directory if it doesn't exist
                                File dir = new File(outputPath);
                                
                                if (!dir.exists()) {
                                        dir.mkdirs();
                                }
                                
                                in = new FileInputStream(inputPath);
                                out = new FileOutputStream(outputPath + fileName);
                                
                                byte[] buffer = new byte[1024];
                                int read;
                                
                                while ((read = in.read(buffer)) != -1) {
                                        out.write(buffer, 0, read);
                                }
                                
                                in.close();
                                in = null;
                                
                                // write the output file
                                out.flush();
                                out.close();
                                out = null;
                                
                                // delete the original file
                                new File(inputPath).delete();
                        }
                        catch (Exception e) {
                                ExceptionHandler.saveLogFile(e);
                        }
                }
        }
        
        public static String parseAssetFileToString(
                                                    Context context,
                                                    String filename) {
                String fileText = null;
                
                try {
                        InputStream stream = context.getAssets().open(filename);
                        int size = stream.available();
                        
                        byte[] bytes = new byte[size];
                        stream.read(bytes);
                        stream.close();
                        
                        fileText = new String(bytes);
                }
                catch (IOException e) {
                        Log.i("MakeMachine", "IOException: " + e.getMessage());
                }
                
                return fileText;
        }
        
        /**
         * Message displays on the screen
         * 
         * @param text
         *                text to be displayed on the screen
         * @param duration
         *                duration of display of text on the screen
         * @param context
         *                context of the implementation
         */
        public static void showToast(
                                     CharSequence text,
                                     int duration,
                                     Context context) {
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
        }
        
        private static String stringHexa(byte[] bytes) {
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                        int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
                        int parteBaixa = bytes[i] & 0xf;
                        if (parteAlta == 0) {
                                s.append('0');
                        }
                        s.append(Integer.toHexString(parteAlta | parteBaixa));
                }
                return s.toString();
        }
        
        /**
         * Unzip files.
         * 
         * @param zipfile
         *                The path of the zip file
         * @param location
         *                The new LocationProvider that you want to unzip the
         *                file
         */
        public static void unzip(String zipFile, String location) {
                try {
                        FileInputStream fin = new FileInputStream(zipFile);
                        ZipInputStream zin = new ZipInputStream(fin);
                        ZipEntry ze = null;
                        while ((ze = zin.getNextEntry()) != null) {
                                Log.v("Decompress", "Unzipping " + ze.getName());
                                
                                if (ze.isDirectory()) {
                                        Utility.dirChecker(location + ze.getName());
                                }
                                else {
                                        FileOutputStream fout = new FileOutputStream(location + ze.getName());
                                        for (int c = zin.read(); c != -1; c = zin.read()) {
                                                fout.write(c);
                                        }
                                        zin.closeEntry();
                                        fout.close();
                                }
                        }
                        zin.close();
                }
                catch (Exception e) {
                        Log.e("Decompress", "unzip", e);
                }
        }
}
