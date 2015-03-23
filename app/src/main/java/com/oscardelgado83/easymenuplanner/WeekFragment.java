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

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
