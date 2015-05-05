package com.oscardelgado83.easymenuplanner.ui;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.InjectView;
import hugo.weaving.DebugLog;

import static butterknife.ButterKnife.inject;
import static com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED;
import static com.google.android.gms.common.ConnectionResult.SERVICE_MISSING;
import static com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
import static com.google.android.gms.common.ConnectionResult.SUCCESS;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.FIRST;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.NONE;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.SECOND;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        CourseFragment.OnFragmentInteractionListener,
        ShoppingListFragment.OnFragmentInteractionListener {

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

        if (isTabletDevice()) {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_main);

        inject(this);

        adView.loadAd(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // Emulator
                .addTestDevice(getString(R.string.nexus_7_device_id)) // Nexus 7
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

        if (!dbStarted) {
            prePopulateDB();
        }

        week = Day.findAll();
        Log.d(LOG_TAG, "week: " + week);
    }

    @DebugLog
    public void prePopulateDB() {
        ActiveAndroid.beginTransaction();
        try {
            TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter('\n');

            splitter.setString(loadInitialCoursesFromFile());
            for (String line : splitter) {
                Course course = new Course();
                StringTokenizer tknzr = new StringTokenizer(line, ",");
                course.name = tknzr.nextToken().trim();
                Course existingCourse = new Select().from(Course.class).where("UPPER(name) = ?",
                        course.name.toUpperCase()).executeSingle();
                if (existingCourse != null) existingCourse.delete();
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
                Method mIsLayoutSizeAtLeast = con.getClass().getMethod("isLayoutSizeAtLeast");
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
            } else if (id == R.id.action_show_completed) {
                ((ShoppingListFragment) currentFrg).showCompletedItems();
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

    @Override
    //http://stackoverflow.com/a/24778951/1464013
    public void onFragmentInteraction(Long courseId) {
    }

    public List<Day> getWeek() {
        return week;
    }
}
