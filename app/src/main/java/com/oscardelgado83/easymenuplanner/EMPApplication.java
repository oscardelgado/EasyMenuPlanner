package com.oscardelgado83.easymenuplanner;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by oscar on 12/04/15.
 */
public class EMPApplication extends Application {

    public static boolean DEBUGGING;

    private Tracker tracker;

    private static final String LOG_TAG = EMPApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        DEBUGGING = getResources().getBoolean(R.bool.debug_mode);
        LeakCanary.install(this);
    }

    public synchronized Tracker getTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setDryRun(DEBUGGING);
            Log.d(LOG_TAG, "GA DryRun has benn set to: " + DEBUGGING);
            tracker = analytics.newTracker(getString(R.string.ga_property_id));

            // Enable Display Features.
            tracker.enableAdvertisingIdCollection(true);
        }
        return tracker;
    }
}
