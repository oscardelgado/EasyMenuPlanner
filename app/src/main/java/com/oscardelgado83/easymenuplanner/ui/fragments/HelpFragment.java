package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.ui.transformers.DepthPageTransformer;
import com.oscardelgado83.easymenuplanner.util.Cons;
import com.oscardelgado83.easymenuplanner.util.GA;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

import static butterknife.ButterKnife.findById;
import static butterknife.ButterKnife.unbind;

/**
 * Created by oscar on 23/03/15.
 */
public class HelpFragment extends Fragment {

    // Instead of class.getSimpleName() to avoid proGuard changing it.
    private static final String FRAGMENT_NAME = "HelpFragment";
    private static final String LOG_TAG = FRAGMENT_NAME;
    public static final String HELP_DRAWABLE_KEY = "help_drawable_key";
    public static final String HELP_TUTORIAL_PAGE = "help_tutorial_page";

    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private Button exitBTN;
    private CheckBox checkBox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_help, container, false);
        ButterKnife.bind(this, view);

        final ActionBar supportActionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) supportActionBar.hide();

        checkBox = findById(view, R.id.tutorial_checkBox);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = ButterKnife.findById(view, R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(false, new DepthPageTransformer());
//        mPager.setPageTransformer(false, new ZoomOutPageTransformer());

        //Bind the title indicator to the adapter
        CirclePageIndicator indicator = findById(view, R.id.indicator);
        indicator.setViewPager(mPager);

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == mPagerAdapter.getCount() - 1) {
                    tutorialCompleted();
                }
            }
        });

        exitBTN = findById(view, R.id.exit_tutorial);
        exitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GA.sendEvent(
                        ((EMPApplication) getActivity().getApplication()).getTracker(),
                        "tutorial",
                        "button clicked",
                        "exit tutorial");

                NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                        getActivity().getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

                navigationDrawerFragment.selectItem(NavigationDrawerFragment.Section.WEEK_MENU.ordinal());
                // TODO: the last section (for example, courses), remains in the title.

                //TODO: keep drawer opened. Try with back button handling.
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity) getActivity()).hideAds();

        GA.sendScreenHit(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                FRAGMENT_NAME);

        checkBox.setChecked(! PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(Cons.FIRST_TIME_HELP_VIEWED, false));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveHelpViewedPreference(! isChecked);
            }
        });
    }

    @Override
    public void onPause() {
        ((MainActivity) getActivity()).showAds();

        ActionBar supportActionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) supportActionBar.show();

        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbind(this);
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

        @DebugLog
        @Override
        public Fragment getItem(int position) {
            HelpSlideFragment helpSlideFragment = new HelpSlideFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(HELP_DRAWABLE_KEY, tutorialDrawables[position]);
            bundle.putInt(HELP_TUTORIAL_PAGE, position);
            helpSlideFragment.setArguments(bundle);

            return helpSlideFragment;
        }

        @Override
        public int getCount() {
            return tutorialDrawables.length;
        }
    }

    @DebugLog
    private void tutorialCompleted() {
        saveHelpViewedPreference(true);

        exitBTN.setText(R.string.go_to_the_app);

        GA.sendEvent(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                "tutorial",
                "last slide viewed",
                "tutorial completed");

        checkBox.setChecked(false);
    }

    private void saveHelpViewedPreference(boolean value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Cons.FIRST_TIME_HELP_VIEWED, value);
        editor.apply();

        Log.i(LOG_TAG, "FIRST_TIME_HELP_VIEWED set to: " + value);
    }
}
