package edu.cornell.tech.foundry.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

/**
 * Created by jameskizer on 4/20/17.
 */

public class RSGeofenceFirstRun {
    private static final String PREFERENCES_FILE  = "firstRun";
    private static final String KEY_FIRST_RUN = "firstRun";

    protected final SharedPreferences sharedPreferences;

    private Gson gson;

    public RSGeofenceFirstRun(Context applicationContext) {

        this.gson = new Gson();
        sharedPreferences = applicationContext
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);

    }

    protected <T> T getValue(String key, Class<? extends T> klass) {
        String json = sharedPreferences.getString(key, null);
        return this.gson.fromJson(json, klass);
    }

    protected <T> void setValue(String key, T value, Class<? super T> klass) {
        String json = this.gson.toJson(value, klass);
        sharedPreferences.edit().putString(key, json).apply();
    }

    @Nullable
    public Boolean getFirstRun() {
        return this.getValue(KEY_FIRST_RUN, Boolean.class);
    }

    public void setFirstRun(Boolean firstRun) {
        this.setValue(KEY_FIRST_RUN, firstRun, Boolean.class);
    }

}
