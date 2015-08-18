package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.ui.HelpActivity;
import com.oscardelgado83.easymenuplanner.util.GA;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by oscar on 16/08/15.
 */
public class HelpFragment extends Fragment {

    @Bind(R.id.tutorial_image)
    ImageView tutorialIV;

    // Instead of class.getSimpleName() to avoid proGuard changing it.
    private static final String FRAGMENT_NAME = "HelpFragment";
    private static final String LOG_TAG = FRAGMENT_NAME;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_page, container, false);
        ButterKnife.bind(this, rootView);

        int drawable = getArguments().getInt(HelpActivity.HELP_DRAWABLE_KEY);
        tutorialIV.setImageResource(drawable);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        GA.sendScreenHit(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                FRAGMENT_NAME);
    }
}
