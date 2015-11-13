package com.oscardelgado83.easymenuplanner.ui.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.util.GA;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    // Instead of class.getSimpleName() to avoid proGuard changing it.
    public static final String FRAGMENT_NAME = "SettingsFragment";
    private static final String LOG_TAG = FRAGMENT_NAME;

    private SharedPreferences settings;

    @Bind(R.id.week_start_day_spinner)
    Spinner weekStartDaySpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore preferences
        settings = getActivity().getPreferences(Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        List<String> weekDaysList = Arrays.asList(new DateFormatSymbols().getWeekdays()).subList(1, 8);
        Collections.rotate(weekDaysList, -(Calendar.getInstance().getFirstDayOfWeek() - 1));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, weekDaysList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekStartDaySpinner.setAdapter(adapter);
        weekStartDaySpinner.setSelection(EMPApplication.USER_WEEK_START_DAY - (Calendar.getInstance().getFirstDayOfWeek() - 1) - 1);
        weekStartDaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int day = position + Calendar.getInstance().getFirstDayOfWeek();
                ((EMPApplication) getActivity().getApplication()).setUserWeekStartDay(day);
                ((MainActivity) getActivity()).updateWeek();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        GA.sendScreenHit(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                FRAGMENT_NAME);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity){
            ((MainActivity) context).onSectionAttached(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
