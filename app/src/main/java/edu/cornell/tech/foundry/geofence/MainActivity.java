package edu.cornell.tech.foundry.geofence;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;

import edu.cornell.tech.foundry.researchsuitetaskbuilder.RSTBStateHelper;

public class MainActivity extends RSGeofenceActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private static final String TAG = "MainActivity";
    public static final int SHOW_AS_ACTION_ALWAYS = 2;

    private static final int REQUEST_SETTINGS = 0xff31;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private final int UPDATE_INTERVAL =  1000; // in milli
    private final int FASTEST_INTERVAL = 900;
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private Button startButton;
    RSTBStateHelper stateHelper;
    private TextView geofenceText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stateHelper = RSGeofenceTaskBuilderManager.getBuilder().getStepBuilderHelper().getStateHelper();
        createGoogleApi();

        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGeofence();
            }
        });

        geofenceText = (TextView) findViewById(R.id.geofenceText);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuItem menuItem = menu.add("settings");
        menuItem.setIcon(android.R.drawable.ic_menu_preferences);
        menuItem.setShowAsAction(SHOW_AS_ACTION_ALWAYS);
        menuItem.setIntent(new Intent(this, SettingsActivity.class));
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, MainActivity.REQUEST_SETTINGS);

                return true;
            }
        });



        return super.onCreateOptionsMenu(menu);

    }


    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
        }


    }

    private void getLastKnownLocation() {
        if ( checkPermission() ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                writeLastLocation();
                startLocationUpdates();
            } else {
                startLocationUpdates();
            }
        }
        else askPermission();
    }

    private void writeActualLocation(Location location) {
       Log.d("location: ", String.valueOf(location.getLatitude()));
//        location.getLongitude()
    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }

    // Start location Updates
    private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius ) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( GEO_DURATION )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent(this, RSuiteGeofenceTransitionsIntentService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    private void startGeofence() {
        Log.i(TAG, "startGeofence()");

        byte [] home_lat_string = stateHelper.valueInState(this,"latitude_home");
        byte [] home_long_string = stateHelper.valueInState(this,"longitude_home");

        try {

            String homeLat_string = new String(home_lat_string, "UTF-8");
            Double homeLat =  Double.parseDouble(homeLat_string);

            String homeLong_string = new String(home_long_string,"UTF-8");
            Double homeLong = Double.parseDouble(homeLong_string);
            LatLng latLngHome = new LatLng(homeLat,homeLong);

            Geofence geofenceHome = createGeofence(latLngHome, GEOFENCE_RADIUS );
            GeofencingRequest geofenceRequestHome = createGeofenceRequest( geofenceHome );
            addGeofence( geofenceRequestHome );

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte [] work_lat_string = stateHelper.valueInState(this,"latitude_work");
        byte [] work_long_string = stateHelper.valueInState(this,"longitude_work");

        try {

            String workLat_string = new String(work_lat_string, "UTF-8");
            Double workLat =  Double.parseDouble(workLat_string);

            String workLong_string = new String(work_long_string,"UTF-8");
            Double workLong = Double.parseDouble(workLong_string);
            LatLng latLngWork = new LatLng(workLat,workLong);

            Geofence geofenceWork = createGeofence(latLngWork, GEOFENCE_RADIUS );
            GeofencingRequest geofenceRequestWork = createGeofenceRequest( geofenceWork );
            addGeofence( geofenceRequestWork );

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();

    }

    @Override
    protected void onStop(){
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastKnownLocation();
        startGeofence();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_SETTINGS) {
                Boolean signedOut = (Boolean) data.getSerializableExtra(SettingsActivity.EXTRA_DID_SIGN_OUT);
                if (signedOut) {
                    startActivity(new Intent(this, OnboardingActivity.class));
                    finish();
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
