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
import android.view.View;
import android.widget.Button;

import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.ui.fragments.HelpFragment;
import com.oscardelgado83.easymenuplanner.ui.transformers.DepthPageTransformer;
import com.oscardelgado83.easymenuplanner.util.Cons;
import com.oscardelgado83.easymenuplanner.util.GA;
import com.viewpagerindicator.CirclePageIndicator;

public class HelpActivity extends AppCompatActivity {

    private static final String SCREEN_NAME = "HelpActivity";
    public static final String HELP_DRAWABLE_KEY = "help_drawable_key";
    public static final String HELP_TUTORIAL_PAGE = "help_tutorial_page";

    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private Button exitBTN;

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

        //Bind the title indicator to the adapter
        CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        exitBTN = (Button) findViewById(R.id.exit_tutorial);
        exitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GA.sendEvent(
                        ((EMPApplication) getApplication()).getTracker(),
                        "tutorial",
                        "button clicked",
                        "exit tutorial");
                finish();
            }
        });
    }

    /**
     * A simple pager adapter that represents some ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private int[] tutorialDrawables = new int[]{
                R.drawable.tutorial_01,
                R.drawable.tutorial_02,
                R.drawable.tutorial_03,
                R.drawable.tutorial_04,
                R.drawable.tutorial_05,
                R.drawable.tutorial_06,
                R.drawable.tutorial_07,
                R.drawable.tutorial_08,
        };

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            HelpFragment helpFragment = new HelpFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(HELP_DRAWABLE_KEY, tutorialDrawables[position]);
            bundle.putInt(HELP_TUTORIAL_PAGE, position);
            helpFragment.setArguments(bundle);

            if (position == getCount() - 1) {
                tutorialCompleted();
            }

            return helpFragment;
        }

        @Override
        public int getCount() {
            return tutorialDrawables.length;
        }
    }

    private void tutorialCompleted() {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Cons.FIRST_TIME_HELP_VIEWED, true);
        editor.apply();

        exitBTN.setText(R.string.go_to_the_app);

        GA.sendEvent(
                ((EMPApplication) getApplication()).getTracker(),
                "tutorial",
                "last slide viewed",
                "tutorial completed");
    }
}
