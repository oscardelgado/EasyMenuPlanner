package com.oscardelgado83.easymenuplanner.ui;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.oscardelgado83.easymenuplanner.ui.fragments.CourseFragment;
import com.oscardelgado83.easymenuplanner.ui.fragments.NavigationDrawerFragment;
import com.oscardelgado83.easymenuplanner.ui.fragments.ShoppingListFragment;
import com.oscardelgado83.easymenuplanner.ui.fragments.WeekFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import butterknife.InjectView;
import hugo.weaving.DebugLog;

import static butterknife.ButterKnife.inject;
import static com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED;
import static com.google.android.gms.common.ConnectionResult.SERVICE_MISSING;
import static com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
import static com.google.android.gms.common.ConnectionResult.SUCCESS;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, CourseFragment.OnFragmentInteractionListener {

    @InjectView(R.id.adView)
    AdView adView;

    public static final int WEEKDAYS = 7;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String PREFERENCE_DB_STARTED = "dbStarted";
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

    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inject(this);

        adView.loadAd(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // Emulator
                .addTestDevice("980B7CBB4875D26814D3B29D1B669AEB") // Nexus 7
                .build());

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        ActiveAndroid.initialize(this);

        // Restore preferences
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        boolean dbStarted = settings.getBoolean(PREFERENCE_DB_STARTED, false);

        //TODO: remove these lines
//        dbStarted = false;
//        new Delete().from(Day.class).execute();
//        new Delete().from(Course.class).execute();
//        new Delete().from(Ingredient.class).execute();
//        new Delete().from(CourseIngredient.class).execute();

        if (!dbStarted) {
            prePopulateDB();
        }

        week = Day.findAll();
        Log.d(LOG_TAG, "week: " + week);
    }

    @DebugLog
    private void prePopulateDB() {
        ActiveAndroid.beginTransaction();
        try {
            TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter('\n');

            splitter.setString(loadInitialCourseNames());//TODO: English
            for (String courseName : splitter) {
                Course course = new Course();
                course.name = courseName;
                course.save();
            }

            Ingredient ingr = new Ingredient();
            ingr.name = "Ingr. 1";
            ingr.save();
            ingr = new Ingredient();
            ingr.name = "Ingr. 2";
            ingr.save();
            ingr = new Ingredient();
            ingr.name = "Ingr. 3";
            ingr.save();

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

            editor.commit();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem loading initial configuration", e);
            Toast.makeText(this, getString(R.string.error_loadingInitialConfig), Toast.LENGTH_LONG);
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    @DebugLog
    public String loadInitialCourseNames() throws IOException {
        //Create a InputStream to read the file into
        InputStream iS;

        Resources resources = getResources();

        iS = resources.openRawResource(R.raw.initial_course_names);

        //create a buffer that has the same size as the InputStream
        byte[] buffer = new byte[iS.available()];
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
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        currentFrg = null;

        switch (position) {
            case 0:
                currentFrg = new WeekFragment();
                break;
            case 1:
                currentFrg = new ShoppingListFragment();
                break;
            case 2:
                currentFrg = new CourseFragment();
                break;
            default:
                break;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, currentFrg)
                .commit();
    }

    public void onSectionAttached(Fragment frg) {
        if (frg instanceof WeekFragment) {
            mTitle = getString(R.string.title_section_week);
        } else if (frg instanceof ShoppingListFragment) {
            mTitle = getString(R.string.title_section_shopping_list);
        } else if (frg instanceof CourseFragment) {
            mTitle = getString(R.string.title_section_dishes);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


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
            } else {
                getMenuInflater().inflate(R.menu.global, menu);
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

        if (currentFrg instanceof WeekFragment) {
            if (id == R.id.action_clear_all) {
                ((WeekFragment)currentFrg).clearAllCourses();
                return true;
            } else if (item.getItemId() == R.id.action_automatic_fill) {
                ((WeekFragment)currentFrg).randomFillAllCourses();
                return true;
            }
        } else if( currentFrg instanceof CourseFragment) {
            if (id == R.id.action_add) {
                ((CourseFragment) currentFrg).addCourseClicked();
                return true;
            }
        }
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Sample action", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ensureGooglePlayServices()) {
            adView.resume();
        }
    }

    @Override
    public void onPause() {
        adView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }

    // https://developer.android.com/google/play-services/setup.html
    private boolean ensureGooglePlayServices() {
        int returnCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        switch (returnCode) {
            case SUCCESS:
                // TODO: then the Google Play services APK is up-to-date and you can continue to make a connection
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

    @Override
    public void onFragmentInteraction(Long courseId) {
        //TODO
    }

    public List<Day> getWeek() {
        return week;
    }
}