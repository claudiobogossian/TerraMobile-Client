package br.inpe.mobile.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationProvider implements LocationListener {
    
    private static LocationProvider instance = null;
    
    private static LocationManager  mLocationManager;
    
    private static Location         myLocation;
    
    private LocationProvider(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        myLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        if (myLocation != null) {
            // Do something with the recent location fix otherwise wait for the update below
        }
        else {
            this.updateLocation();
        }
    }
    
    public static LocationProvider getInstance(Context context) {
        if (instance == null) {
            instance = new LocationProvider(context);
        }
        
        return instance;
    }
    
    public void onLocationChanged(Location location) {
        if (location != null) {
            //Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            myLocation = location;
            mLocationManager.removeUpdates(this);
        }
    }
    
    public Location getLocation() {
        this.updateLocation();
        return myLocation;
    }
    
    private void updateLocation() {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
    
    // Required functions    
    public void onProviderDisabled(String arg0) {}
    
    public void onProviderEnabled(String arg0) {}
    
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
    
}
