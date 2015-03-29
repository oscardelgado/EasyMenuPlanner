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
import com.oscardelgado83.easymenuplanner.model.Course;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
* Created by oscar on 23/03/15.
*/
public class WeekFragment extends Fragment {

    @InjectView(R.id.day1)
    TableRow tableRow1;

    @InjectView(R.id.day2)
    TableRow tableRow2;

    @InjectView(R.id.day3)
    TableRow tableRow3;

    @InjectView(R.id.day4)
    TableRow tableRow4;

    @InjectView(R.id.day5)
    TableRow tableRow5;

    @InjectView(R.id.day6)
    TableRow tableRow6;

    @InjectView(R.id.day7)
    TableRow tableRow7;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);
        ButterKnife.inject(this, view);

        TableRow[] allTableRows =  {tableRow1, tableRow2, tableRow3, tableRow4, tableRow5, tableRow6, tableRow7};
        for(TableRow tr : allTableRows) {
            Course course = getRandomCourse();

            TextView tvA = (TextView) tr.findViewById(R.id.textViewA);
            tvA.setText(course.name);

            Course course2 = getRandomCourse();

            TextView tvB = (TextView) tr.findViewById(R.id.textViewB);
            tvB.setText(course2.name);
        }

        return view;
    }

    private Course getRandomCourse() {
        return new Select()
                        .from(Course.class)
    //                    .where("Category = ?", category.getId())
                        .orderBy("RANDOM()")
                        .executeSingle();
    }

    @OnClick({R.id.buttonLeftA, R.id.buttonRightA, R.id.buttonLeftB, R.id.buttonRightB})
    public void changeCourse() {
        TextView tvA = (TextView) tableRow1.findViewById(R.id.textViewA);
        tvA.setText(getRandomCourse().name);
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
