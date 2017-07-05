package edu.cornell.tech.foundry.geofence;

import edu.cornell.tech.foundry.studyManagement.CTFActivityRun;

/**
 * Created by jameskizer on 4/21/17.
 */

public interface RSGeofenceActivityManagerDelegate {
    boolean tryToLaunchRSActivity(RSGeofenceActivityManager activityManager, CTFActivityRun activityRun);
}
