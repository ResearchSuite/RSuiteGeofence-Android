package edu.cornell.tech.foundry.geofence;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import edu.cornell.tech.foundry.researchsuitetaskbuilder.RSTBStateHelper;

public class MainActivity extends RSGeofenceActivity {

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

    public static final int REQUEST_CODE_SIGN_IN  = 31473;
    private RSuiteGeofenceManager.PendingGeofenceTask mPendingGeofenceTask = RSuiteGeofenceManager.PendingGeofenceTask.NONE;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stateHelper = RSGeofenceTaskBuilderManager.getBuilder().getStepBuilderHelper().getStateHelper();

        Log.d("testing order: ","at main");

        RSGeofenceApplication app = (RSGeofenceApplication) getApplication();
        app.initializeGeofenceManager();


        startMonitoringGeofences();

//        double homeLat,homeLng,workLat,workLng = 0;
//
//        byte[] homeLatByte = stateHelper.valueInState(this,"latitude_home");
//        byte[] homeLngByte = stateHelper.valueInState(this,"longitude_home");
//        byte[] workLatByte = stateHelper.valueInState(this,"latitude_work");
//        byte[] workLngByte = stateHelper.valueInState(this,"longitude_work");
//
//        try {
//            String homeLatString = new String(homeLatByte, "UTF-8");
//            String homeLngString = new String(homeLngByte, "UTF-8");
//            String workLatString = new String(workLatByte, "UTF-8");
//            String workLngString = new String(workLngByte, "UTF-8");
//
//            homeLat = Double.parseDouble(homeLatString);
//            homeLng = Double.parseDouble(homeLngString);
//            workLat = Double.parseDouble(workLatString);
//            workLng = Double.parseDouble(workLngString);
//
//            RSuiteGeofenceManager.getInstance().setHomeCoords(homeLat,homeLng);
//            RSuiteGeofenceManager.getInstance().setWorkCoords(workLat,workLng);
//
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }


        // startMontioringGefoences(); //TODO: put this somewhere

        //createGoogleApi();
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startGeofence();
             //   startMonitoringGeofences();
            }
        });

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

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            performPendingGeofenceTask();
        }
    }

    private void startMonitoringGeofences() {
        if (!checkPermissions()) {
            mPendingGeofenceTask = RSuiteGeofenceManager.PendingGeofenceTask.ADD;
            requestPermissions();
        } else {
            RSuiteGeofenceManager.getInstance().startMonitoringGeofences(this);
        }
    }

    private void stopMonitoringGeofences() {
        if (!checkPermissions()) {
            mPendingGeofenceTask = RSuiteGeofenceManager.PendingGeofenceTask.REMOVE;
            requestPermissions();
        } else {
            RSuiteGeofenceManager.getInstance().stopMonitoringGeofences(this);
        }
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private void performPendingGeofenceTask() {
        if (mPendingGeofenceTask == RSuiteGeofenceManager.PendingGeofenceTask.ADD) {
            RSuiteGeofenceManager.getInstance().startMonitoringGeofences(this);
        } else if (mPendingGeofenceTask == RSuiteGeofenceManager.PendingGeofenceTask.REMOVE) {
            RSuiteGeofenceManager.getInstance().stopMonitoringGeofences(this);
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Log.i(TAG, "Requesting permission");
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
                performPendingGeofenceTask();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                mPendingGeofenceTask = RSuiteGeofenceManager.PendingGeofenceTask.NONE;
            }
        }
    }


//    private void createGoogleApi() {
//        Log.d(TAG, "createGoogleApi()");
//        if (googleApiClient == null ) {
//            googleApiClient = new GoogleApiClient.Builder( this )
//                    .addConnectionCallbacks( this )
//                    .addOnConnectionFailedListener( this )
//                    .addApi( LocationServices.API )
//                    .build();
//        }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_SETTINGS) {
                Boolean signedOut = (Boolean) data.getSerializableExtra(SettingsActivity.EXTRA_DID_SIGN_OUT);
                if (signedOut) {
                    startActivity(new Intent(this, RSGeofenceSplashActivity.class));
                    finish();
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    public void onRestart(){
        super.onRestart();


        RSGeofenceApplication app = (RSGeofenceApplication) getApplication();
        app.initializeGeofenceManager();


        startMonitoringGeofences();
    }

    @Override
    public void onResume(){
        super.onResume();

//
//        RSGeofenceApplication app = (RSGeofenceApplication) getApplication();
//        app.initializeGeofenceManager();
//
//
//        startMonitoringGeofences();

    }
}
