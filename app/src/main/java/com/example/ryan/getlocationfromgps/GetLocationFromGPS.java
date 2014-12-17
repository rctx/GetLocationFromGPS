package com.example.ryan.getlocationfromgps;

import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.example.ryan.getlocationfromgps.R;
//import android.R;

public class GetLocationFromGPS extends Activity implements SensorEventListener {

    TextView testViewStatus, textViewLatitude, textViewLongitude, textViewDebug, textViewSavedLat, textViewSavedLon, textViewBearing;
    Location savedLocation;
    Location lastLocation;
    Boolean saveLoc = false;
    float lastBearing;
    GeomagneticField geoField;
    SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private ImageView mPointer;
    int googlePlayServicesAvaliable;

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
        textViewSavedLat = (TextView)findViewById(R.id.savedLat);
        textViewSavedLon = (TextView)findViewById(R.id.savedLon);
        textViewBearing = (TextView)findViewById(R.id.bearing);

        myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.pointer);

        //get last known location, if available
        Location location = myLocationManager.getLastKnownLocation(PROVIDER);
        showMyLocation(location);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        myLocationManager.removeUpdates(myLocationListener);
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub

        googlePlayServicesAvaliable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        super.onResume();
        myLocationManager.requestLocationUpdates(
                PROVIDER,     //provider
                0,       //minTime
                0,       //minDistance
                myLocationListener); //LocationListener

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

    }

    private void showMyLocation(Location l){
        if(l == null){
            testViewStatus.setText("No Location!");
        }else{
            textViewLatitude.setText("Latitude: " + l.getLatitude());
            textViewLongitude.setText("Longitude: " + l.getLongitude());
        }

    }

    public void saveCurrentLocation(View view){
        saveLoc = true;
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;

            // Now get this to point to saved location rather than North
            //azimuthInDegress = lastBearing - (lastBearing + azimuthInDegress);
            if(lastLocation != null) {
                geoField = new GeomagneticField(
                        Double.valueOf(lastLocation.getLatitude()).floatValue(),
                        Double.valueOf(lastLocation.getLongitude()).floatValue(),
                        Double.valueOf(lastLocation.getAltitude()).floatValue(),
                        System.currentTimeMillis()
                );
                azimuthInDegress -= geoField.getDeclination();
            }

            azimuthInDegress = lastBearing +  azimuthInDegress;
            // If the direction is smaller than 0, add 360 to get the rotation clockwise.
            if (azimuthInDegress < 0) {
                azimuthInDegress = azimuthInDegress + 360;
            }
            //Math.round(-azimuthInDegress / 360 + 180);
            //azimuthInDegress = 180 + (180 + azimuthInDegress);
            //azimuthInDegress = (azimuthInDegress + 360) % 360;
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    private LocationListener myLocationListener
            = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            showMyLocation(location);
            geoField = new GeomagneticField(
                    Double.valueOf(location.getLatitude()).floatValue(),
                    Double.valueOf(location.getLongitude()).floatValue(),
                    Double.valueOf(location.getAltitude()).floatValue(),
                    System.currentTimeMillis()
            );
            if(saveLoc == true){
                savedLocation = location;
                textViewSavedLat.setText("Latitude: " + location.getLatitude());
                textViewSavedLon.setText("Longitude: " + location.getLongitude());
                saveLoc = false;
            }
            if(savedLocation != null){
                lastBearing = location.bearingTo(savedLocation);
                if(lastBearing < 0) lastBearing = lastBearing + 360;
                //lastBearing += geoField.getDeclination();
                textViewBearing.setText("Bearing: " + lastBearing);

            }
            lastLocation = location;
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