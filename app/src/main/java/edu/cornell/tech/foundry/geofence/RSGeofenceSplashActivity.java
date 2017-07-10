package edu.cornell.tech.foundry.geofence;

import android.content.Intent;
import android.util.Log;

import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.ui.PinCodeActivity;

import edu.cornell.tech.foundry.geofence.notification.TaskAlertReceiver;
import edu.cornell.tech.foundry.ohmageomhsdk.OhmageOMHManager;

/**
 * Created by jameskizer on 4/12/17.
 */

public class RSGeofenceSplashActivity extends PinCodeActivity {

    @Override
    public void onDataReady()
    {
        super.onDataReady();

        this.sendBroadcast(TaskAlertReceiver.createSetNotificationIntent());

        RSGeofenceFirstRun firstRun = new RSGeofenceFirstRun(this);
        if (firstRun.getFirstRun() == null || !firstRun.getFirstRun()) {
            RSGeofenceFileAccess.getInstance().clearFileAccess(this);
        }

        if(!OhmageOMHManager.getInstance().isSignedIn()) {
            Log.d("testing order: ","not signed in");
            this.launchOnboardingActivity();
        }

        else {
            firstRun.setFirstRun(true);
            Log.d("testing order: ","signed in??");
            this.launchMainActivity();
        }

//        if (/*firstRun.getFirstRun() != null &&
//                firstRun.getFirstRun() &&*/
//                OhmageOMHManager.getInstance().isSignedIn()) {
//             this.launchMainActivity();
//        }
//        else {
//            firstRun.setFirstRun(true);
//            //make sure we clear app state here
//            this.launchOnboardingActivity();
//        }

    }

    @Override
    public void onDataAuth()
    {
        if(StorageAccess.getInstance().hasPinCode(this))
        {
            super.onDataAuth();
        }
        else // allow them through to onboarding if no pincode
        {
            onDataReady();
        }
    }

    @Override
    public void onDataFailed()
    {
        super.onDataFailed();
        finish();
    }

    private void launchOnboardingActivity() {
        startActivity(new Intent(this, OnboardingActivity.class));
        Log.d("testing order: ","onboarding called");
        RSGeofenceActivityManager.get().queueActivity(this, "LocationOnboarding", true);


        finish();


    }



    private void launchMainActivity()
    {

        Log.d("testing order: ","main called");
        startActivity(new Intent(this, MainActivity.class));
        finish();


    }

}
