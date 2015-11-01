package com.oscardelgado83.easymenuplanner;

import android.app.Application;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;

import java.util.Calendar;

import static com.oscardelgado83.easymenuplanner.util.Cons.DEBUGGING;

/**
 * Created by oscar on 12/04/15.
 */
public class EMPApplication extends Application {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    private static final String LOG_TAG = EMPApplication.class.getSimpleName();

    //TODO: remove constant and do settings
    public final static int USER_WEEK_START_DAY = Calendar.MONDAY;

    @Override
    public void onCreate() {
        super.onCreate();

        DEBUGGING = getResources().getBoolean(R.bool.debug_mode);

        ActiveAndroid.initialize(this);

        LeakCanary.install(this);
    }

    public synchronized Tracker getTracker() {
        if (tracker == null) {
            analytics = GoogleAnalytics.getInstance(this);

            analytics.setDryRun(DEBUGGING);
            if (DEBUGGING) Log.d(LOG_TAG, "GA DryRun enabled");

            analytics.setLocalDispatchPeriod(1800);

            tracker = analytics.newTracker(getString(R.string.ga_property_id));
            tracker.enableExceptionReporting(true);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableAutoActivityTracking(true);
        }
        return tracker;
    }
}
