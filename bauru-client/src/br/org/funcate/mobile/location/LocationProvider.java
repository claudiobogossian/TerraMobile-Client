package br.org.funcate.mobile.location;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationProvider extends Activity implements LocationListener {

    private static LocationProvider instance        = null;
    private static LocationManager  locationManager = null;
    private static String           bestProvider    = null;

    private LocationProvider() {
        if (instance != null) {
            instance = new LocationProvider();
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            bestProvider = locationManager.getBestProvider(new Criteria(), false);
        }
    }

    private Location getLocation() {
        return locationManager.getLastKnownLocation(bestProvider);
    }

    public static Location getBestLocation() {
        return new LocationProvider().getLocation();
    }

    @Override
    public void onLocationChanged(android.location.Location arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub
    }

}
