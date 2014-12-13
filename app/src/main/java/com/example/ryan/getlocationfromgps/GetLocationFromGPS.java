package com.example.ryan.getlocationfromgps;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
//import com.example.ryan.getlocationfromgps.R;
//import android.R;

public class GetLocationFromGPS extends Activity {

    TextView testViewStatus, textViewLatitude, textViewLongitude, textViewDebug;

    LocationManager myLocationManager;
    String PROVIDER = LocationManager.GPS_PROVIDER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location_from_gps_new);
        testViewStatus = (TextView)findViewById(R.id.status);
        textViewLatitude = (TextView)findViewById(R.id.latitude);
        textViewLongitude = (TextView)findViewById(R.id.longitude);
        textViewDebug = (TextView)findViewById(R.id.debug);

        myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        //get last known location, if available
        Location location = myLocationManager.getLastKnownLocation(PROVIDER);
        showMyLocation(location);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        myLocationManager.removeUpdates(myLocationListener);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        myLocationManager.requestLocationUpdates(
                PROVIDER,     //provider
                0,       //minTime
                0,       //minDistance
                myLocationListener); //LocationListener
    }

    private void showMyLocation(Location l){
        if(l == null){
            testViewStatus.setText("No Location!");
        }else{
            textViewLatitude.setText("Latitude: " + l.getLatitude());
            textViewLongitude.setText("Longitude: " + l.getLongitude());
        }

    }
    public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            // Enable location updates
            textViewDebug.setText("on");
            myLocationManager.requestLocationUpdates(
                    PROVIDER,     //provider
                    0,       //minTime
                    0,       //minDistance
                    myLocationListener); //LocationListener
        } else {
            // Disable location updates
            textViewDebug.setText("off");
            myLocationManager.removeUpdates(myLocationListener);
        }
    }

    private LocationListener myLocationListener
            = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            showMyLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }};
}