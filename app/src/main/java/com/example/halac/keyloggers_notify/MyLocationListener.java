package com.example.halac.keyloggers_notify;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
/*
Getting the location of the user (longitude and latitude)
 */
class MyLocationListener implements LocationListener
{
    public static final int duration = 20; //collect data after each 20 seconds
    private double[] lastMeasuredValues;

    @Override
    public void onLocationChanged(Location loc)
    {
        lastMeasuredValues = new double[] { loc.getLatitude(), loc.getLongitude()};
    }

    public double[] getLastMeasuredValues()
    {
        return lastMeasuredValues;
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}
