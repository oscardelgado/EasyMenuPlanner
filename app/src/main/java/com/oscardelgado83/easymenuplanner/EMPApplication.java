package com.oscardelgado83.easymenuplanner;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by oscar on 12/04/15.
 */
public class EMPApplication extends Application {

    Tracker tracker;

    public synchronized Tracker getTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(getString(R.string.ga_property_id));
        }
        return tracker;
    }
}
