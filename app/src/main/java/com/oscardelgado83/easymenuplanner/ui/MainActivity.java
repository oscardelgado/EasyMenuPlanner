package com.oscardelgado83.easymenuplanner.ui;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;
import com.oscardelgado83.easymenuplanner.model.CourseIngredient;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.oscardelgado83.easymenuplanner.ui.fragments.CourseFragment;
import com.oscardelgado83.easymenuplanner.ui.fragments.NavigationDrawerFragment;
import com.oscardelgado83.easymenuplanner.ui.fragments.NavigationDrawerFragment.Section;
import com.oscardelgado83.easymenuplanner.ui.fragments.ShoppingListFragment;
import com.oscardelgado83.easymenuplanner.ui.fragments.WeekFragment;
import com.oscardelgado83.easymenuplanner.ui.widgets.MenuWeekAppWidgetMedium;
import com.oscardelgado83.easymenuplanner.ui.widgets.MenuWeekAppWidgetSmall;
import com.oscardelgado83.easymenuplanner.ui.widgets.ShoppingListAppWidget;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

import static com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED;
import static com.google.android.gms.common.ConnectionResult.SERVICE_MISSING;
import static com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
import static com.google.android.gms.common.ConnectionResult.SUCCESS;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.FIRST;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.NONE;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.SECOND;
import static com.oscardelgado83.easymenuplanner.util.Cons.DEBUGGING;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    @Bind(R.id.adView)
    AdView adView;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int WEEKDAYS = 7;
    public static final String PREFERENCE_DB_STARTED = "dbStarted";
    public static final String PREFERENCE_LAUCH_COUNT = "launchCount";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Fragment currentFrg;

    private List<Day> week;
    private int exportDBMenuId;

    private FragmentManager fragmentManager;

    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore preferences
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        boolean dbStarted = settings.getBoolean(PREFERENCE_DB_STARTED, false);

        if (isTabletDevice()) {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loadAdMob();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if (!dbStarted) {
            prePopulateDB();
        }

        week = Day.findAll();
        if (DEBUGGING) Log.d(LOG_TAG, "week: " + week);

        int launchCount = settings.getInt(PREFERENCE_LAUCH_COUNT, 0);
        settings.edit().putInt(PREFERENCE_LAUCH_COUNT, ++launchCount).apply();

        if (! DEBUGGING) {

            // AdBuddiz: request to cache adds
            AdBuddiz.setPublisherKey(getResources().getString(R.string.ad_buddiz_publisher_key));
//            if (Cons.DEBUGGING) AdBuddiz.setTestModeActive();
            AdBuddiz.cacheAds(this); // this = current Activity
        }
    }

    @DebugLog
    public void prePopulateDB() {
        ActiveAndroid.beginTransaction();
        try {
            TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter('\n');

            splitter.setString(loadInitialCoursesFromFile());
            for (String line : splitter) {
                StringTokenizer tknzr = new StringTokenizer(line, ",");
                String courseName = tknzr.nextToken().trim();
                Course existingCourse = new Select().from(Course.class).where("UPPER(name) = ?",
                        courseName.toUpperCase()).executeSingle();
                if (existingCourse != null) {
                    // The initial idea was delete and re-create, but there is a
                    // bug in SQLite regarding Foreign Keys
                    // https://github.com/pardom/ActiveAndroid/issues/127
                    // So, simply ignore and maintain existing course.

//                    existingCourse.delete();
                } else {
                    Course course = new Course();
                    course.name = courseName;
                    switch (Integer.parseInt(tknzr.nextToken().trim())) {
                        case 1:
                            course.courseType = FIRST;
                            break;
                        case 2:
                            course.courseType = SECOND;
                            break;
                        default:
                            course.courseType = NONE;
                            break;
                    }
                    course.save();

                    while (tknzr.hasMoreElements()) {
                        String ingredientName = tknzr.nextToken().trim();
                        Ingredient ingr = new Select().from(Ingredient.class)
                                .where("UPPER(name) = ?", ingredientName.toUpperCase()).executeSingle();
                        if (ingr == null) {
                            ingr = new Ingredient();
                            ingr.name = ingredientName;
                            ingr.save();
                        }
                        CourseIngredient ci = new CourseIngredient(course, ingr);
                        ci.save();
                    }
                }
            }

            for (int i = 0; i < WEEKDAYS; i++) {
                Day day = new Day();
                day.date = new Date();
                day.save();
            }

            ActiveAndroid.setTransactionSuccessful();

            // We need an Editor object to make preference changes.
            // All objects are from android.context.Context
            SharedPreferences settings = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(PREFERENCE_DB_STARTED, true);

            editor.apply();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    @DebugLog
    private String loadInitialCoursesFromFile() {
        //Create a InputStream to read the file into
        InputStream iS;

        Resources resources = getResources();

        iS = resources.openRawResource(R.raw.initial_data);

        //create a buffer that has the same size as the InputStream
        byte[] buffer = new byte[0];
        try {
            buffer = new byte[iS.available()];
            //read the text file as a stream, into the buffer
            iS.read(buffer);
            //create a output stream to write the buffer into
            ByteArrayOutputStream oS = new ByteArrayOutputStream();
            //write this buffer to the output stream
            oS.write(buffer);
            //Close the Input and Output streams
            oS.close();
            iS.close();
            //return the output stream as a String
            return oS.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem loading initial configuration", e);
            Toast.makeText(this, getString(R.string.error_loadingInitialConfig), Toast.LENGTH_LONG);
        }
        return null;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        // update the main content by replacing fragments
        fragmentManager = getSupportFragmentManager();

        currentFrg = null;

        switch (Section.values()[position]) {
            case WEEK_MENU:
                currentFrg = new WeekFragment();
                break;
            case WEEK_SHOPPINGLIST:
                currentFrg = new ShoppingListFragment();
                break;
            case COURSES:
                currentFrg = new CourseFragment();
                break;
            default:
                break;
        }

        //http://stackoverflow.com/a/10261449/1464013
        fragmentManager.beginTransaction()
                .replace(R.id.container, currentFrg)
                .commitAllowingStateLoss();
    }

    @DebugLog
    private boolean isTabletDevice() {
        if (android.os.Build.VERSION.SDK_INT >= 11) { // honeycomb
            // test screen size, use reflection because isLayoutSizeAtLeast is only available since 11
            Configuration con = getResources().getConfiguration();
            try {
                Method mIsLayoutSizeAtLeast = con.getClass().getMethod("isLayoutSizeAtLeast", int.class);
                Boolean r = (Boolean) mIsLayoutSizeAtLeast.invoke(con, 0x00000004); // Configuration.SCREENLAYOUT_SIZE_XLARGE
                return r;
            } catch (Exception x) {
                return false;
            }
        }
        return false;
    }

    public void onSectionAttached(Fragment frg) {
        if (frg instanceof WeekFragment) {
            mTitle = getString(Section.WEEK_MENU.getTitleKey());
        } else if (frg instanceof ShoppingListFragment) {
            mTitle = getString(Section.WEEK_SHOPPINGLIST.getTitleKey());
        } else if (frg instanceof CourseFragment) {
            mTitle = getString(Section.COURSES.getTitleKey());
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @DebugLog
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            if (currentFrg instanceof WeekFragment) {
                getMenuInflater().inflate(R.menu.week_fragment, menu);
            } else if (currentFrg instanceof CourseFragment) {
                getMenuInflater().inflate(R.menu.courses_fragment, menu);
            } else if (currentFrg instanceof ShoppingListFragment) {
                getMenuInflater().inflate(R.menu.shoppinglist_fragment, menu);
                ShoppingListFragment sf = (ShoppingListFragment) currentFrg;
                sf.setHideCompleted(menu.findItem(R.id.action_hide_completed));
                sf.setShowAll(menu.findItem(R.id.action_show_all));
                sf.refreshMenu();
            } else {
                getMenuInflater().inflate(R.menu.global, menu);
            }

            exportDBMenuId = menu.size() + 1;
            if (DEBUGGING) {
                menu.add(0, exportDBMenuId, Menu.NONE, "Export DB");
            }

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @DebugLog
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == exportDBMenuId) {
            exportDB();
        } else if (currentFrg instanceof WeekFragment) {
            if (id == R.id.action_clear_all) {
                ((WeekFragment)currentFrg).clearAllCourses();
                return true;
            } else if (item.getItemId() == R.id.action_automatic_fill) {
                ((WeekFragment)currentFrg).randomFillAllCourses();
                return true;
            }
        } else if (currentFrg instanceof CourseFragment) {
            if (id == R.id.action_add) {
                ((CourseFragment) currentFrg).addCourseClicked();
                return true;
            } else if (id == R.id.action_restore_default_courses) {
                prePopulateDB();
                ((CourseFragment) currentFrg).refreshCourseList();
                return true;
            }
        } else if (currentFrg instanceof ShoppingListFragment) {
            if (id == R.id.action_hide_completed) {
                ((ShoppingListFragment) currentFrg).hideCompletedItems();
                return true;
            } else if (id == R.id.action_show_all) {
                ((ShoppingListFragment) currentFrg).showAllItems();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ensureGooglePlayServices()) {
            adView.resume();
        }

        Intent intent = getIntent(); //TODO: check if possible NPE
        Bundle extras = intent.getExtras();

        if (extras != null && extras.containsKey(ShoppingListAppWidget.EXTRA_ITEM)) {
            Log.d(LOG_TAG, "Changing to ShoppingListFragment");

            currentFrg = new ShoppingListFragment();

            //http://stackoverflow.com/a/10261449/1464013
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, currentFrg)
                    .commitAllowingStateLoss();
//        } else if() { //TODO: add new extra for week widget
//            Log.d(LOG_TAG, "Changing to WeekFragment");
//
//            currentFrg = new WeekFragment();
//
//            //http://stackoverflow.com/a/10261449/1464013
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, currentFrg)
//                    .commitAllowingStateLoss();
        }
    }

    @DebugLog
    @Override
    public void onPause() {
        adView.pause();

        updateWidgets(MenuWeekAppWidgetSmall.class);
        updateWidgets(MenuWeekAppWidgetMedium.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            updateShoppingListWidgets();
        }

        super.onPause();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void updateShoppingListWidgets() {
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ShoppingListAppWidget.class));
        AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(ids, R.id.ingredient_name);
    }

    private void updateWidgets(Class clazz) {
        // http://stackoverflow.com/a/7738687/1464013
        Intent intent = new Intent(this, clazz);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), clazz));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        adView.destroy();
        if (! DEBUGGING) AdBuddiz.onDestroy();
        super.onDestroy();
    }

    // https://developer.android.com/google/play-services/setup.html
    private boolean ensureGooglePlayServices() {
        int returnCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        switch (returnCode) {
            case SUCCESS:
                // Then the Google Play services APK is up-to-date and you can continue to make a connection
                return true;
            case SERVICE_MISSING:
            case SERVICE_VERSION_UPDATE_REQUIRED:
            case SERVICE_DISABLED:
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(returnCode, this, 1);
                errorDialog.show();
                break;
            default:
                break;
        }
        return false;
    }

    @DebugLog
    private void loadAdMob() {
        adView.loadAd(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(getString(R.string.nexus_7_device_id))
                .addTestDevice(getString(R.string.genymotion_2_3_7_device_id))
                .addTestDevice(getString(R.string.genymotion_4_4_4_device_id))
                .addTestDevice(getString(R.string.genymotion_tablet))

                .build());
    }

    public List<Day> getWeek() {
        return week;
    }

    @DebugLog
    public void refreshShoppinglistMenu() {
        ((ShoppingListFragment) currentFrg).refreshMenu();
    }

    @DebugLog
    private void exportDB(){
        String DB_NAME = "EasyMenuPlanner.db";
        String currentDBPath = "/data/com.oscardelgado83.easymenuplanner/databases/" + DB_NAME;
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        String backupDBPath = DB_NAME;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            FileChannel source = new FileInputStream(currentDB).getChannel();
            FileChannel destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
            if (DEBUGGING) Log.i(LOG_TAG, "DB Exported to: " + backupDB.getAbsolutePath());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
