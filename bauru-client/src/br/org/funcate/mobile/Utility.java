package br.org.funcate.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osmdroid.util.GeoPoint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class Utility {

    private final static String TAG         = "#UTILITY";

    private static final int    TWO_MINUTES = 1000 * 60 * 2;

    public static final String  hostUrl     = "http://192.168.5.102:8080/";

    //public static final String  hostUrl     = "http://200.144.100.34:8080/";

    /**
     * Determines whether one Location reading is better than the current
     * Location fix
     * 
     * @param location
     *            The new Location that you want to evaluate
     * @param currentBestLocation
     *            The current Location fix, to which you want to compare the new
     *            one
     */
    public static boolean isBetterLocation(Location location,
            Location currentBestLocation) {
        if (currentBestLocation == null)
            // A new location is always better than no location
            return true;

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer)
            return true;
        // If the new location is more than two minutes older, it must be
        // worse
        else if (isSignificantlyOlder)
            return false;

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate)
            return true;
        else if (isNewer && !isLessAccurate)
            return true;
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
            return true;
        return false;
    }

    /**
     * Checks whether two providers are the same
     * 
     * @param provider1
     *            first provider comparison
     * @param provider2
     *            second provider comparison
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null)
            return provider2 == null;
        return provider1.equals(provider2);
    }

    /**
     * Determines whether the Network is available
     * 
     * @param context
     *            context of the implementation
     */
    public static boolean isNetworkAvailable(Context context) {
        Boolean isAvailable = false;
        ConnectivityManager connect_mng = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connect_mng.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED || connect_mng.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
            isAvailable = true;
        return isAvailable;
    }

    /**
     * Determines if the sdcard is available for reading and writing
     * 
     * @return
     */
    public static boolean isSdcardAvaliable() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state))
            return true;
        return false;
    }

    /**
     * Message displays on the screen
     * 
     * @param text
     *            text to be displayed on the screen
     * @param duration
     *            duration of display of text on the screen
     * @param context
     *            context of the implementation
     */
    public static void showToast(CharSequence text, int duration, Context context) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * Calling Network Configuration
     * 
     * @param context
     *            context of application
     */
    public static void enableNETWORK(Context context) {
        Intent wifiOptionsIntent = new Intent(
                android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        Utility.showToast("Pressione [back] para voltar", Toast.LENGTH_SHORT,
                context);
        context.startActivity(wifiOptionsIntent);
    }

    /**
     * Calling Storage Configuration
     * 
     * @param context
     *            context of application
     */
    public static void enableSDCARD(Context context) {
        Intent sdcardOptionsIntent = new Intent(
                android.provider.Settings.ACTION_MEMORY_CARD_SETTINGS);
        Utility.showToast("Pressione [back] para voltar", Toast.LENGTH_SHORT,
                context);
        context.startActivity(sdcardOptionsIntent);
    }

    /**
     * Calling GPS Configuration
     * 
     * @param context
     *            context of application
     */
    public static void enableGPS(Context context) {
        Intent gpsOptionsIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        Utility.showToast("Pressione [back] para voltar", Toast.LENGTH_SHORT,
                context);
        context.startActivity(gpsOptionsIntent);
    }

    /**
     * Formats the date in the form of inclusion in database
     * 
     * @param date
     *            date to format
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * Create one GeoPoint for location
     * 
     * @param loc
     *            location for creating GeoPoint
     * 
     */
    public static GeoPoint createGeoPoint(Location loc) {
        int lat = (int) (loc.getLatitude() * 1E6);
        int lng = (int) (loc.getLongitude() * 1E6);
        return new GeoPoint(lat, lng);
    }

    /**
     * Count characters in a string
     * 
     * @param str
     *            string to count characters
     * @param cha
     *            character you want to counted
     * @param idx
     *            index for iniciate the search
     * 
     */
    public static int countCharacterString(String str, String cha, int idx) {
        int idxfound = str.indexOf(cha, idx);
        if (idxfound != -1)
            return 1 + countCharacterString(str, cha, idxfound + 1);
        return 0;
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

            HttpURLConnection conn = (HttpURLConnection) myFileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            return BitmapFactory.decodeStream(is);

        } catch (Exception ex) {
            Log.e(TAG, "Error downloadRemoteFile: " + ex);
        }
        return null;
    }

    /**
     * 
     * 
     * @param cepvalue
     * @return
     */
    public static String formatCep(String cepvalue) {
        if (cepvalue == null || cepvalue.compareTo("") == 0)
            return cepvalue;
        else if (cepvalue.length() == 8)
            return cepvalue.substring(0, 5) + "-" + cepvalue.substring(5, 8);
        return cepvalue;
    }

    /**
     * 
     * 
     * @param correct
     * @return
     */
    public static String correctNull(String correct) {
        if (correct == null || correct.compareTo("") == 0) {
            correct = "---";
        }
        return correct;
    }

    /**
     * 
     * 
     * @param searchAddress
     * @param context
     * @return
     * @throws IOException
     */
    public static List<Address> findGeocode(String searchAddress,
            Context context) throws IOException {
        List<Address> findGeocode = new Geocoder(context).getFromLocationName(
                searchAddress, 1);
        return findGeocode;
    }

    /**
     * 
     * Generate MD5 hash of a string.
     * 
     * @param String
     *            text
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String stringHexa(byte[] bytes) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
            int parteBaixa = bytes[i] & 0xf;
            if (parteAlta == 0)
                s.append('0');
            s.append(Integer.toHexString(parteAlta | parteBaixa));
        }
        return s.toString();
    }

    /**
     * Unzip files.
     * 
     * @param zipfile
     *            The path of the zip file
     * @param location
     *            The new Location that you want to unzip the file
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
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
        }
    }

    /**
     * Create directory if he doesn't exists.
     * 
     * @param String
     *            location
     *            The path of the folder.
     */
    public static void dirChecker(String location) {
        File f = new File(location);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
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
     * @return
     */

    public static String getDeviceId(Context ctx) {
        TelephonyManager tManager = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tManager.getDeviceId();
    }
}
