package com.oscardelgado83.easymenuplanner.ui.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.util.GA;

import hugo.weaving.DebugLog;


/**
 * Implementation of App Widget functionality.
 */
public class ShoppingListWidget extends AppWidgetProvider {

    // Instead of class.getSimpleName() to avoid proGuard changing it.
    public static String WIDGET_NAME = "ShoppingListWidget";
    protected static final String LOG_TAG = WIDGET_NAME;

    @DebugLog
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(appWidgetManager, appWidgetIds[i]);
        }

        GA.sendEvent(
                ((EMPApplication) context.getApplicationContext()).getTracker(),
                WIDGET_NAME,
                "widget callback called",
                "onUpdate with " + appWidgetIds.length + " widgets");
    }

    @DebugLog
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

        GA.sendEvent(
                ((EMPApplication) context.getApplicationContext()).getTracker(),
                WIDGET_NAME,
                "widget callback called",
                "onEnabled");
    }

    @DebugLog
    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

        GA.sendEvent(
                ((EMPApplication) context.getApplicationContext()).getTracker(),
                WIDGET_NAME,
                "widget callback called",
                "onDisabled");
    }

    @DebugLog
    static void updateAppWidget(AppWidgetManager appWidgetManager,
                                int appWidgetId) {

    }
}

