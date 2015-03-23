package com.oscardelgado83.easymenuplanner;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.oscardelgado83.easymenuplanner.model.Course;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
* Created by oscar on 23/03/15.
*/
public class WeekFragment extends Fragment {

    @InjectView(R.id.row1)
    TableRow tableRow1;

    @InjectView(R.id.adView)
    AdView adView;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static WeekFragment newInstance(int sectionNumber) {
        WeekFragment fragment = new WeekFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public WeekFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);
        ButterKnife.inject(this, view);

        Course course = new Select()
                .from(Course.class)
//                    .where("Category = ?", category.getId())
                .orderBy("RANDOM()")
                .executeSingle();

        TextView tv1 = (TextView) tableRow1.findViewById(R.id.textView1A);
        tv1.setText(course.name);

        // Iniciar una solicitud genérica.
//            AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // Emulator
                .addTestDevice("980B7CBB4875D26814D3B29D1B669AEB") // Nexus 7
                .build();

        // Cargar adView con la solicitud de anuncio.
        adView.loadAd(adRequest);

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
