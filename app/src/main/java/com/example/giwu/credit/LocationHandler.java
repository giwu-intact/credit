package com.example.giwu.credit;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by giwu on 2016-06-16.
 */
public class LocationHandler {

    LocationManager locationManager;
    TextView locView;
    Context context;
    SharedPreferences old_location;
    SharedPreferences.Editor editor;

    public LocationHandler(final Context context, TextView textView) throws IOException {
        this.locView = textView;
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        old_location = context.getSharedPreferences("location",Context.MODE_PRIVATE);
        editor = old_location.edit();
        editor.putFloat("distance",0);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(context, Locale.getDefault());

                Double old_latitude = Double.valueOf(old_location.getFloat("la",0));
                Double old_longitude = Double.valueOf(old_location.getFloat("long",0));
                Double old_distance = Double.valueOf(old_location.getFloat("distance",0));

                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();

                // calculate the distance
                Double theta = longitude - old_longitude;
                Double dist = Math.sin(deg2rad(latitude)) * Math.sin(deg2rad(old_latitude)) + Math.cos(deg2rad(latitude)) * Math.cos(deg2rad(old_latitude)) * Math.cos(deg2rad(theta));
                dist = Math.acos(dist);
                dist = rad2deg(dist);
                dist = dist * 60 * 1.1515 + old_distance;

                editor.putFloat("la",latitude.floatValue());
                editor.putFloat("long",longitude.floatValue());
                editor.putFloat("distance",dist.floatValue());
                editor.commit();

                String loc = "Address (Only shows when connected to network): \n";
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                    loc += address + ", " + city + ", " + state + ", " + country + ", " + postalCode + ", " + knownName + "\n";
                } catch (IOException e) {
                    e.printStackTrace();
                }

                displayLocation("Displaying location... \n"+"Latitude: "+latitude.toString()+'\n'+"Longitude: "+longitude.toString()+'\n'+
                        loc+'\n'+"Traveled distance: "+ dist.toString()+" meters"+'\n');
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }

            private double deg2rad(double deg) {
                return (deg * Math.PI / 180.0);
            }

            private double rad2deg(double rad) {
                return (rad / Math.PI * 180.0);
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public void displayLocation(String loc){
        this.locView.setText(loc);
    }

}
