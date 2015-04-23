package com.oscardelgado83.easymenuplanner;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by oscar on 12/04/15.
 */
public class EMPApplication extends Application {

    public static boolean DEBUGGING;

    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
        DEBUGGING = getResources().getBoolean(R.bool.debug_mode);
    }

    public synchronized Tracker getTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setDryRun(DEBUGGING);
            tracker = analytics.newTracker(getString(R.string.ga_property_id));

            // Enable Display Features.
            tracker.enableAdvertisingIdCollection(true);
        }
        return tracker;
    }
}
