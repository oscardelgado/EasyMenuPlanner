package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static butterknife.ButterKnife.*;

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

    private TableRow[] allTableRows;

    private static final String LOG_TAG = WeekFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);
        inject(this, view);

        allTableRows = new TableRow[]{tableRow1, tableRow2, tableRow3, tableRow4, tableRow5, tableRow6, tableRow7};
        for(TableRow tr : allTableRows) {
            TextView tvA = (TextView) tr.findViewById(R.id.textViewA);
            TextView tvB = (TextView) tr.findViewById(R.id.textViewB);

            setOnClickListener(tr, tvA, R.id.buttonLeftA);
            setOnClickListener(tr, tvA, R.id.buttonRightA);
            setOnClickListener(tr, tvB, R.id.buttonLeftB);
            setOnClickListener(tr, tvB, R.id.buttonRightB);
        }

        //TODO: remove
        TextView tv = findById(tableRow3, R.id.textViewA);
        Day[] week = ((MainActivity) getActivity()).getWeek();
        tv.setText(week[2].firstCourse.name);

        return view;
    }

    private void setOnClickListener(TableRow tr, TextView tv, int btnId) {
        Button btn = (Button) tr.findViewById(btnId);
        btn.setOnClickListener(courseBtnClickListener(tv));
    }

    private View.OnClickListener courseBtnClickListener(final TextView tv) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText(getRandomCourse().name);
            }
        };
    }

    private Course getRandomCourse() {
        return new Select()
                        .from(Course.class)
    //                    .where("Category = ?", category.getId())
                        .orderBy("RANDOM()")
                        .executeSingle();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        reset(this);
    }

    public void clearAllCourses() {
        for(TableRow tr : allTableRows) {
            TextView tvA = (TextView) tr.findViewById(R.id.textViewA);
            tvA.setText("");

            TextView tvB = (TextView) tr.findViewById(R.id.textViewB);
            tvB.setText("");
        }
    }

    public void randomFillAllCourses() {
        for(TableRow tr : allTableRows) {
            TextView tvA = (TextView) tr.findViewById(R.id.textViewA);
            if (tvA.getText().equals("")) {
                tvA.setText(getRandomCourse().name);
            }

            TextView tvB = (TextView) tr.findViewById(R.id.textViewB);
            if (tvB.getText().equals("")) {
                tvB.setText(getRandomCourse().name);
            }
        }
    }
}
