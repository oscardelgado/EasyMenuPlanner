package com.oscardelgado83.easymenuplanner;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

    private static final String PREFERENCE_WEEK_START_DAY = "weekStartDay";
    public static int USER_WEEK_START_DAY;

    @Override
    public void onCreate() {
        super.onCreate();

        DEBUGGING = getResources().getBoolean(R.bool.debug_mode);

        // Restore preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        USER_WEEK_START_DAY = prefs.getInt(PREFERENCE_WEEK_START_DAY, Calendar.getInstance().getFirstDayOfWeek());

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

    public void setUserWeekStartDay(int weekDay) {
        Log.i(LOG_TAG, "User week start day will change to " + weekDay);
        USER_WEEK_START_DAY = weekDay; //Sunday = 1, Monday = 7

        // We need an Editor object to make preference changes.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_WEEK_START_DAY, weekDay);
        editor.apply();
    }
}
