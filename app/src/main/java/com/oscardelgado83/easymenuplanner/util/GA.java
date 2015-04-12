package com.oscardelgado83.easymenuplanner.util;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by oscar on 12/04/15.
 */
public class GA {

    public static void sendScreenHit(Tracker t, String screenName) {
        // Set screen name.
        t.setScreenName(screenName);

        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void sendEvent(Tracker t, String category, String action, String label) {
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }
}
