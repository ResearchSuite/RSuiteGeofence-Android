package edu.cornell.tech.foundry.geofence;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.researchstack.backbone.utils.LogExt;
import org.researchstack.skin.DataResponse;

import rx.SingleSubscriber;

/**
 * Created by Christina on 6/28/17.
 */

public class SettingsActivity extends RSGeofenceActivity{

    public static final String EXTRA_DID_SIGN_OUT = "YADLSettingsActivity.DidSignOut";
    private static final String SETTTINGS_FRAGMENT_TAG = "YADLSettingsActivity.YADLSettingsFragment";
    MainActivity mainActivity;

   SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(org.researchstack.skin.R.layout.rss_activity_fragment);

        Toolbar toolbar = (Toolbar) findViewById(org.researchstack.skin.R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(org.researchstack.skin.R.id.container, new SettingsFragment(), SETTTINGS_FRAGMENT_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        SettingsFragment settingsFragment = (SettingsFragment)getSupportFragmentManager().findFragmentByTag(SETTTINGS_FRAGMENT_TAG);
        if (settingsFragment != null) {
           //settingsFragment.updateUI();
        }
    }

    public void signOut() {
        LogExt.d(getClass(), "Signing Out");

        RSGeofenceApplication app = (RSGeofenceApplication)getApplication();
        app.signOut(this).subscribe(new SingleSubscriber<DataResponse>() {
            @Override
            public void onSuccess(DataResponse value) {

                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_DID_SIGN_OUT, true);
                setResult(RESULT_OK, resultIntent);

                finish();
            }

            @Override
            public void onError(Throwable error) {
                LogExt.e(getClass(), error);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_DID_SIGN_OUT, true);
                setResult(RESULT_OK, resultIntent);

                finish();
            }
        });

    }
}
