package edu.cornell.tech.foundry.geofence;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import org.researchstack.backbone.storage.file.StorageAccessListener;
import org.researchstack.backbone.utils.LogExt;

import java.io.UnsupportedEncodingException;

import edu.cornell.tech.foundry.researchsuitetaskbuilder.RSTBStateHelper;

/**
 * Created by Christina on 6/28/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener,StorageAccessListener {

    public static final String KEY_SIGN_OUT = "sign_out";
    public static final String KEY_HOME_LOCATION = "user_input_home";
    public static final String KEY_WORK_LOCATION = "user_input_work";

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
       //updateUI();

    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        super.addPreferencesFromResource(R.xml.geofence_settings);

        RSTBStateHelper stateHelper = RSGeofenceTaskBuilderManager.getBuilder().getStepBuilderHelper().getStateHelper();

        PreferenceScreen screen = getPreferenceScreen();
        screen.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        Preference homePreference = (Preference) findPreference(KEY_HOME_LOCATION);

        byte[] homeLocation = stateHelper.valueInState(getContext(),"address_home");
        try {
            String homeLocationString = new String(homeLocation, "UTF-8");
            homePreference.setSummary(homeLocationString);
        } catch (UnsupportedEncodingException e) {
            homePreference.setSummary("unsaved location");
            e.printStackTrace();
        }


        Preference workPreference = (Preference) findPreference(KEY_WORK_LOCATION);

        byte[] workLocation = stateHelper.valueInState(getContext(),"address_work");
        try {
            String workLocationString = new String(workLocation, "UTF-8");
            workPreference.setSummary(workLocationString);
        } catch (UnsupportedEncodingException e) {
            workPreference.setSummary("unsaved location");
            e.printStackTrace();
        }


    }

    @Override
    public void onStart(){

        super.onStart();

        stopMonitoringGeofences();
        ((SettingsActivity)getActivity()).updateGeofences();
        updateUI();
    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen){

        super.onNavigateToScreen(preferenceScreen);

        stopMonitoringGeofences();

        ((SettingsActivity)getActivity()).updateGeofences();

       // updateUI();


    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference)
    {
        LogExt.i(getClass(), String.valueOf(preference.getTitle()));

        if(preference.hasKey()) {

            String key = preference.getKey();

            if (key.equals(KEY_SIGN_OUT)) {
                if(getActivity() instanceof SettingsActivity) {
                    ((SettingsActivity)getActivity()).signOut();
                }

                return true;
            }
            else if (key.equals(KEY_HOME_LOCATION)){
                RSGeofenceActivityManager.get().queueActivity(getActivity(), "homeLocation", true);

                return true;
            }
            else if (key.equals(KEY_WORK_LOCATION)){
                RSGeofenceActivityManager.get().queueActivity(getActivity(), "workLocation", true);
                return true;
            }

        }
       // this.updateUI();
        return super.onPreferenceTreeClick(preference);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //this.updateUI();
    }

    public void updateUI() {

        Log.d("testing updateUI: ","here");

        RSTBStateHelper stateHelper = RSGeofenceTaskBuilderManager.getBuilder().getStepBuilderHelper().getStateHelper();

        Preference homePreference = (Preference) findPreference(KEY_HOME_LOCATION);

        byte[] homeLocation = stateHelper.valueInState(getContext(),"address_home");
        try {
            String homeLocationString = new String(homeLocation, "UTF-8");
            homePreference.setSummary(homeLocationString);
        } catch (UnsupportedEncodingException e) {
            homePreference.setSummary("unsaved location");
            e.printStackTrace();
        }


        Preference workPreference = (Preference) findPreference(KEY_WORK_LOCATION);

        Log.d("testing updateUI: ","here");

        byte[] workLocation = stateHelper.valueInState(getContext(),"address_work");
        try {
            String workLocationString = new String(workLocation, "UTF-8");
            Log.d("testing updateUI(v): ",workLocationString);
            workPreference.setSummary(workLocationString);
        } catch (UnsupportedEncodingException e) {
            workPreference.setSummary("unsaved location");
            e.printStackTrace();
        }



    }


    @Override
    public void onDataReady() {

    }

    @Override
    public void onDataFailed() {

    }

    @Override
    public void onDataAuth() {

    }

    private void stopMonitoringGeofences() {
        RSuiteGeofenceManager.getInstance().stopMonitoringGeofences(getActivity());

    }

    private void startMonitoringGeofences() {
        RSuiteGeofenceManager.getInstance().startMonitoringGeofences(getActivity());

    }



}
