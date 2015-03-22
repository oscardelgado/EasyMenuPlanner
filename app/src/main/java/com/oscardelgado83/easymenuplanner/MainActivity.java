package com.oscardelgado83.easymenuplanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.oscardelgado83.easymenuplanner.model.Course;

//import hugo.weaving.DebugLog;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED;
import static com.google.android.gms.common.ConnectionResult.SERVICE_MISSING;
import static com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
import static com.google.android.gms.common.ConnectionResult.SUCCESS;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String DB_STARTED = "dbStarted";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
//    @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        boolean dbStarted = settings.getBoolean(DB_STARTED, false);
        if (!dbStarted) {
            prePopulateDB();
        }
    }

    //    @DebugLog
    private void prePopulateDB() {
        ActiveAndroid.beginTransaction();
        try {
            String initialCourseNames[] = {
                    "Sopa",
                    "Filetes",
                    "San jacobos",
                    "Esárragos",
                    "Tallarines",
                    "Ensalada",
                    "Espinacas",
                    "Alubias verdes",
                    "Macarrones",
                    "Arroz",
                    "Puré",
                    "Calamares",
                    "Pizza",
                    "Bakalao",
                    "Gazpacho",
                    "Patatas",
                    "Lentejas",
                    "Pimientos rellenos",
                    "Pechugas de pollo",
                    "Trucha",
                    "Hamburguesa",
                    "Pollo asado",
                    "Tortilla",
                    "Chuletas sajonia",
                    "Salchichas",
                    "Alitas de pollo",
                    "Garbanzos"
            };
            for (String courseName : initialCourseNames) {
                Course course = new Course();
                course.name = courseName;
                course.save();
            }
            ActiveAndroid.setTransactionSuccessful();

            // We need an Editor object to make preference changes.
            // All objects are from android.context.Context
            SharedPreferences settings = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(DB_STARTED, true);

            editor.commit();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
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
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        @InjectView(R.id.row1)
        TableRow tableRow1;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_main, container, false);
            ButterKnife.inject(this, view);

            Course course = new Select()
                    .from(Course.class)
//                    .where("Category = ?", category.getId())
                    .orderBy("RANDOM()")
                    .executeSingle();

            TextView tv1 = (TextView) tableRow1.findViewById(R.id.textView1A);
            tv1.setText(course.name);

            return view;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            ButterKnife.reset(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ensureGooglePlayServices();
    }

    // https://developer.android.com/google/play-services/setup.html
    private void ensureGooglePlayServices() {
        int returnCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        switch (returnCode) {
            case SUCCESS:
                // TODO: then the Google Play services APK is up-to-date and you can continue to make a connection
                break;
            case SERVICE_MISSING:
            case SERVICE_VERSION_UPDATE_REQUIRED:
            case SERVICE_DISABLED:
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(returnCode, this, 1);
                errorDialog.show();
                break;
            default:
                break;
        }
    }
}
