package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;

import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import butterknife.InjectView;
import hugo.weaving.DebugLog;

import static butterknife.ButterKnife.findById;
import static butterknife.ButterKnife.inject;
import static butterknife.ButterKnife.reset;

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

    private List<Course> allCourses;

    private boolean dirty;

    private Random rand;

    private static final String LOG_TAG = WeekFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);
        inject(this, view);

        List<Day> week = ((MainActivity) getActivity()).getWeek();

        allCourses = new Select()
                .from(Course.class)
                .orderBy("name")
                .execute();

        allTableRows = new TableRow[]{tableRow1, tableRow2, tableRow3, tableRow4, tableRow5, tableRow6, tableRow7};
        for(int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];
            tr.setTag(i);

            TextView tvA = findById(tr, R.id.textViewA);
            TextView tvB = findById(tr, R.id.textViewB);

            if (week.isEmpty()) {
                Log.w(LOG_TAG, "The week has not been initialized.");
            } else {
                if (week.get(i).firstCourse != null) tvA.setText(week.get(i).firstCourse.name);
                if (week.get(i).secondCourse != null) tvB.setText(week.get(i).secondCourse.name);
            }

            setOnClickListener(tr, tvA, R.id.buttonLeftA);
            setOnClickListener(tr, tvA, R.id.buttonRightA);
            setOnClickListener(tr, tvB, R.id.buttonLeftB);
            setOnClickListener(tr, tvB, R.id.buttonRightB);
        }
        return view;
    }

    private void setOnClickListener(TableRow tr, TextView tv, int btnId) {
        Button btn = (Button) tr.findViewById(btnId);

        int row = (Integer) tr.getTag();
        int col = (tv.getId() == R.id.textViewA)? 0 : 1;
        btn.setOnClickListener(courseBtnClickListener(tv, row, col));
    }

    private View.OnClickListener courseBtnClickListener(final TextView tv, final int row, final int col) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Day> week = ((MainActivity) getActivity()).getWeek();
                Day selectedDay = week.get(row);
                Course currentCourse = (col == 0)? selectedDay.firstCourse : selectedDay.secondCourse;
                Course newCourse = null;
                ListIterator<Course> it = allCourses.listIterator(allCourses.indexOf(currentCourse));
                switch (v.getId()) {
                    case R.id.buttonLeftA:
                    case R.id.buttonLeftB:
                        do {
                            if (it.hasPrevious()) {
                                newCourse = it.previous();
                            } else {
                                newCourse = allCourses.get(allCourses.size() - 1);
                            }
                        } while ( newCourse == currentCourse);
                        break;
                    case R.id.buttonRightA:
                    case R.id.buttonRightB:
                        do {
                            if (it.hasNext()) {
                                newCourse = it.next();
                            } else {
                                newCourse = allCourses.get(0);
                            }
                        } while (newCourse == currentCourse);
                        break;
                    default:
                        break;
                }
                if (col == 0) {
                    selectedDay.firstCourse = newCourse;
                } else if (col == 1) {
                    selectedDay.secondCourse = newCourse;
                }
                selectedDay.save();
                tv.setText(newCourse.name);
                dirty = true;
            }
        };
    }

    private Course getRandomCourse() {
        rand = new Random();
        int randomInt = rand.nextInt(allCourses.size());
        return allCourses.get(randomInt);
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

    @DebugLog
    @Override
    public void onPause() {
        super.onPause();

        //TODO: decide if store to DB in onPause() or onStop() (http://stackoverflow.com/q/14936281/1464013)
        if (dirty) {
            try {
                ActiveAndroid.beginTransaction();
                List<Day> week = ((MainActivity) getActivity()).getWeek();
                for(int i = 0; i < allTableRows.length; i++) {
                    Day day = week.get(i);
                    day.save(); // TODO: optimize (update only if dirty)
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    public void clearAllCourses() {
        for(TableRow tr : allTableRows) {
            TextView tvA = (TextView) tr.findViewById(R.id.textViewA);
            tvA.setText("");

            TextView tvB = (TextView) tr.findViewById(R.id.textViewB);
            tvB.setText("");
        }
        dirty = true;
    }

    public void randomFillAllCourses() {
        Course course = null;
        List<Day> week = ((MainActivity) getActivity()).getWeek();
        for(int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];
            TextView tvA = (TextView) tr.findViewById(R.id.textViewA);
            if (tvA.getText().equals("")) {
                course = getRandomCourse();
                week.get(i).firstCourse = course;
                tvA.setText(course.name);
            }

            TextView tvB = (TextView) tr.findViewById(R.id.textViewB);
            if (tvB.getText().equals("")) {
                course = getRandomCourse();
                week.get(i).secondCourse = course;
                tvB.setText(getRandomCourse().name);
            }
        }
        dirty = true;
    }
}
