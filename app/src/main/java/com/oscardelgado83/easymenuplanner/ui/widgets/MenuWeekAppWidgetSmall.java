package com.oscardelgado83.easymenuplanner.ui.widgets;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.util.GA;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hugo.weaving.DebugLog;


/**
 * Implementation of App Widget functionality.
 */
public class MenuWeekAppWidgetSmall extends AppWidgetProvider {

    // Instead of class.getSimpleName() to avoid proGuard changing it.
    public static String WIDGET_NAME = "MenuWeekAppWidget";
    protected static final String LOG_TAG = WIDGET_NAME;

    public static final int WEEKDAYS = 7;
    public static final int INITIAL = 80; // maxHeight for first tile
    public static final int JUMP = 120; // maxHeight increment for each tile

    private static List<Day> allWeek;
    private static Map<Integer, Integer> tilesByWidget = new HashMap<>();
    private static int firstPos;
    private static final List<Integer> daysCurrOrder = initDaysCurrOrder();
    private static int lastPos;

    private static List<Integer> initDaysCurrOrder() {
        List<Integer> daysCurrOrder = new ArrayList<>();

        /*
            If the widget will show 4 days, and today it's Saturday:
            English:
                  - - - =
            S M T X ThF Sat
            1 2 3 4 5 6 7

            Spanish:
                  - - = -
            L M MiJ V S D
            2 3 4 5 6 7 1
        */
        int firstDay = Calendar.getInstance().getFirstDayOfWeek();
        for (int i = 0; i < WEEKDAYS; i++) {
            daysCurrOrder.add((i + firstDay - 1) % WEEKDAYS + 1);
        }
        return daysCurrOrder;
    }

    @DebugLog
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
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
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        allWeek = Day.findAll();
        Log.d(LOG_TAG, "allWeek size: " + allWeek.size());
        List<Day> printedDays = null;

        int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Sunday is 1, Saturday is 7.

        int maxTiles = (WEEKDAYS + 2) / 3;
        int todayPos = daysCurrOrder.indexOf(currentDayOfWeek);
        int tiles;
        if (tilesByWidget.containsKey(appWidgetId)) {

            // The widget has been just resized.
            tiles = Math.min(maxTiles, tilesByWidget.get(appWidgetId));
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                // The widget may have been resized before.
                tiles = Math.min(maxTiles, getHeightCells(context, appWidgetManager, appWidgetId));
            } else {
                tiles = 1;
            }
        }

        /*
        1 tile: 1 row
            todayPos
        2 tiles: 4 rows
            todayPos and 3 more, or last 4. [todayPos, todayPos+4) or [last-4, last)
        3 tiles: 7 rows
            todayPos and 6 more, or last 7. [todayPos, last)
        n tiles: 3n - 2 rows
            todayPos and (3n-2)-1 = more, or last (3n-2). [todayPos, todayPos+(3n-2)) or [last-(3n-2), last)
         */
        lastPos = Math.min(todayPos + (3 * tiles - 2), WEEKDAYS);
        firstPos = lastPos - (3 * tiles - 2);
        Log.d(LOG_TAG, "It wil print from " + firstPos + " to " + lastPos);
        printedDays = allWeek.subList(firstPos, lastPos);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.menu_week_app_widget);
        RemoteViews[] subviews = new RemoteViews[printedDays.size()];

        String[] dayNames = new DateFormatSymbols().getShortWeekdays();

        views.removeAllViews(R.id.linear_layout);
        views.addView(R.id.linear_layout, new RemoteViews(context.getPackageName(), R.layout.menu_week_app_widget_header));
        for (int i = firstPos; i < lastPos; i++) {
            Course course = null;
            subviews[i - firstPos] = new RemoteViews(context.getPackageName(), R.layout.menu_week_app_widget_row);
            int indexWithCurrentOrder = daysCurrOrder.get(i % WEEKDAYS);
            if (printedDays.get(i - firstPos) != null) {
                subviews[i - firstPos].setTextViewText(R.id.week_day_name, dayNames[indexWithCurrentOrder]);
                course = printedDays.get(i - firstPos).firstCourse;
                if (course != null) subviews[i - firstPos].setTextViewText(R.id.left_text, course.name);
                course = printedDays.get(i - firstPos).secondCourse;
                if (course != null) subviews[i - firstPos].setTextViewText(R.id.right_text, course.name);
            }
            if (indexWithCurrentOrder == currentDayOfWeek) {
                subviews[i - firstPos].setTextColor(R.id.week_day_name, context.getResources().getColor(R.color.primary));
            }
            views.addView(R.id.linear_layout, subviews[i - firstPos]);
        }

        // Create an Intent to launch Activity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        views.setOnClickPendingIntent(R.id.linear_layout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        GA.sendEvent(
                ((EMPApplication) context.getApplicationContext()).getTracker(),
                WIDGET_NAME,
                "updateAppWidget",
                "It wil print from " + firstPos + " to " + lastPos);
    }

    @DebugLog
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        int heightCells = getHeightCells(context, appWidgetManager, appWidgetId);

        tilesByWidget.put(appWidgetId, heightCells);
        updateAppWidget(context, appWidgetManager, appWidgetId);

        GA.sendEvent(
                ((EMPApplication) context.getApplicationContext()).getTracker(),
                WIDGET_NAME,
                "widget callback called",
                "onAppWidgetOptionsChanged");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected static int getHeightCells(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle opt = appWidgetManager.getAppWidgetOptions(appWidgetId);

//        int minWidth = opt.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
//        int maxWidth = opt.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
//        int minHeith = opt.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeith = opt.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
//        Log.d(LOG_TAG, minWidth + ", " + maxWidth + ", " + minHeith + ", " + maxHeith);

        int heightCells = (((maxHeith - INITIAL) / JUMP) + 1);
        Log.d(LOG_TAG, "heightCells: " + heightCells);

        GA.sendEvent(
                ((EMPApplication) context.getApplicationContext()).getTracker(),
                WIDGET_NAME,
                "getHeightCells",
                "AppWidgetOptions: " + opt + ". heightCells: " + heightCells);

        return heightCells;
    }
}

