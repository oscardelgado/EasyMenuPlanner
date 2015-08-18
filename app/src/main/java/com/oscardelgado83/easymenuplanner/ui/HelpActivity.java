package com.oscardelgado83.easymenuplanner.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.ui.fragments.HelpFragment;
import com.oscardelgado83.easymenuplanner.ui.transformers.DepthPageTransformer;
import com.oscardelgado83.easymenuplanner.util.Cons;
import com.oscardelgado83.easymenuplanner.util.GA;

public class HelpActivity extends AppCompatActivity {

    private static final String SCREEN_NAME = "HelpActivity";
    public static final String HELP_DRAWABLE_KEY = "help_drawable_key";


    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ActionBar sab;
        if((sab = getSupportActionBar()) != null) {
            sab.hide();
        }

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(false, new DepthPageTransformer());
//        mPager.setPageTransformer(false, new ZoomOutPageTransformer());

        //TODO: make the user confirm with a button or something
        firstTimeHelpFinished();
    }

    /**
     * A simple pager adapter that represents some ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private int[] tutorialDrawables = new int[]{
                R.drawable.tutorial_example_1,
                R.drawable.tutorial_example_2,
                R.drawable.tutorial_example_3
        };

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            HelpFragment helpFragment = new HelpFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(HELP_DRAWABLE_KEY, tutorialDrawables[position]);
            helpFragment.setArguments(bundle);
            return helpFragment;
        }

        @Override
        public int getCount() {
            return tutorialDrawables.length;
        }
    }

    public void firstTimeHelpFinished() {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Cons.FIRST_TIME_HELP_VIEWED, true);
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();

        GA.sendScreenHit(
                ((EMPApplication) getApplication()).getTracker(),
                SCREEN_NAME);
    }
}
