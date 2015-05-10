package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.ui.adapters.CourseAdapter;
import com.oscardelgado83.easymenuplanner.util.GA;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import butterknife.InjectView;
import hugo.weaving.DebugLog;

import static butterknife.ButterKnife.findById;
import static butterknife.ButterKnife.inject;
import static butterknife.ButterKnife.reset;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.FIRST;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.NONE;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.SECOND;
import static com.oscardelgado83.easymenuplanner.ui.fragments.NavigationDrawerFragment.Section.COURSES;

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

    private List<Course> allFirstCourses;
    private List<Course> allSecondCourses;

    private boolean dirty;

    private Random rand;

    private static final String LOG_TAG = WeekFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);
        inject(this, view);

        List<Day> week = ((MainActivity) getActivity()).getWeek();

        allFirstCourses = new Select()
                .from(Course.class)
                .where("courseType in (?, ?)", FIRST, NONE)
                .orderBy("UPPER(name)")
                .execute();

        allSecondCourses = new Select()
                .from(Course.class)
                .where("courseType in (?, ?)", SECOND, NONE)
                .orderBy("UPPER(name)")
                .execute();

        if (allFirstCourses.size() < 2 || allSecondCourses.size() < 2) {
            String message = null;
            if (allFirstCourses.isEmpty()) {
                message = getString(R.string.first_courses_needed);
            } else if (allSecondCourses.isEmpty()) {
                message = getString(R.string.second_course_needed);
            }
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.not_enough_courses))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.go_to_courses), dialogClickListener)
                    .create().show();
        }

        allTableRows = new TableRow[]{tableRow1, tableRow2, tableRow3, tableRow4, tableRow5, tableRow6, tableRow7};
        for (int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];
            tr.setTag(i);

            TextView tvA = findById(tr, R.id.textViewA);
            TextView tvB = findById(tr, R.id.textViewB);
            Button btnDelA = findById(tr, R.id.buttonDelA);
            Button btnDelB = findById(tr, R.id.buttonDelB);

            if (week == null || week.isEmpty()) {
                Log.w(LOG_TAG, "The week has not been initialized.");
            } else {
                if (week.get(i).firstCourse != null) {
                    tvA.setText(week.get(i).firstCourse.name);
                } else {
                    btnDelA.setEnabled(false);
                }
                if (week.get(i).secondCourse != null) {
                    tvB.setText(week.get(i).secondCourse.name);
                } else {
                    btnDelB.setEnabled(false);
                }
            }
            findById(tr, R.id.buttonLeftA).setOnClickListener(courseBtnClickListener(tvA, i, 0));
            findById(tr, R.id.buttonRightA).setOnClickListener(courseBtnClickListener(tvA, i, 0));
            findById(tr, R.id.buttonSearchA).setOnClickListener(courseBtnClickListener(tvA, i, 1));
            findById(tr, R.id.buttonLeftB).setOnClickListener(courseBtnClickListener(tvB, i, 1));
            findById(tr, R.id.buttonRightB).setOnClickListener(courseBtnClickListener(tvB, i, 1));
            findById(tr, R.id.buttonSearchB).setOnClickListener(courseBtnClickListener(tvB, i, 1));
            btnDelA.setOnClickListener(courseBtnClickListener(tvA, i, 0));
            btnDelB.setOnClickListener(courseBtnClickListener(tvB, i, 1));
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        GA.sendScreenHit(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                "WeekFragment");
    }

    private View.OnClickListener courseBtnClickListener(final TextView tv, final int row, final int col) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Day> week = ((MainActivity) getActivity()).getWeek();
                Day selectedDay = week.get(row);
                Course currentCourse = (col == 0) ? selectedDay.firstCourse : selectedDay.secondCourse;
                Course newCourse = null;
                ListIterator<Course> it = null;
                List<Course> currentCoursesList = null;
                if (v.getId() == R.id.buttonLeftA || v.getId() == R.id.buttonRightA) {
                    currentCoursesList = allFirstCourses;
                } else if (v.getId() == R.id.buttonLeftB || v.getId() == R.id.buttonRightB) {
                    currentCoursesList = allSecondCourses;
                }
                if (currentCoursesList != null) {
                    if (currentCourse != null && currentCoursesList.contains(currentCourse)) {
                        it = currentCoursesList.listIterator(currentCoursesList.indexOf(currentCourse));
                    } else {
                        it = currentCoursesList.listIterator(0);
                    }
                }
                switch (v.getId()) {
                    case R.id.buttonSearchA:
                    case R.id.buttonSearchB:
                        showCoursesDialog(tv, row, col);
                        break;
                    case R.id.buttonLeftA:
                    case R.id.buttonLeftB:
                        if (col == 0) {
                            findById(allTableRows[row], R.id.buttonDelA).setEnabled(true);
                        } else {
                            findById(allTableRows[row], R.id.buttonDelB).setEnabled(true);
                        }
                        do {
                            if (it.hasPrevious()) {
                                newCourse = it.previous();
                            } else {
                                newCourse = currentCoursesList.get(currentCoursesList.size() - 1);
                            }
                        } while (newCourse == currentCourse);
                        break;
                    case R.id.buttonRightA:
                    case R.id.buttonRightB:
                        if (col == 0) {
                            findById(allTableRows[row], R.id.buttonDelA).setEnabled(true);
                        } else {
                            findById(allTableRows[row], R.id.buttonDelB).setEnabled(true);
                        }
                        do {
                            if (it.hasNext()) {
                                newCourse = it.next();
                            } else {
                                newCourse = currentCoursesList.get(0);
                            }
                        } while (newCourse == currentCourse);
                        break;
                    case R.id.buttonDelA:
                    case R.id.buttonDelB:
                        if (col == 0) {
                            findById(allTableRows[row], R.id.buttonDelA).setEnabled(false);
                        } else {
                            findById(allTableRows[row], R.id.buttonDelB).setEnabled(false);
                        }
                        newCourse = null;
                        break;
                    default:
                        break;
                }
                if (col == 0) {
                    selectedDay.firstCourse = newCourse;
                } else if (col == 1) {
                    selectedDay.secondCourse = newCourse;
                }
                tv.setText(newCourse != null ? newCourse.name : "");
                dirty = true;
            }
        };
    }

    private void showCoursesDialog(final TextView tv, final int row, final int col) {
        final List<Course> allCourses = new Select().from(Course.class).orderBy("UPPER(name) ASC").execute();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_course);
        ArrayAdapter<Course> adapter = new CourseAdapter(getActivity(), allCourses);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Course selectedCourse = allCourses.get(which);
                List<Day> week = ((MainActivity) getActivity()).getWeek();
                Day selectedDay = week.get(row);
                if (col == 0) {
                    selectedDay.firstCourse = selectedCourse;
                } else if (col == 1) {
                    selectedDay.secondCourse = selectedCourse;
                }
                tv.setText(selectedCourse.name);
                dirty = true;
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
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

        if (dirty) {
            persist();
        }
    }

    @DebugLog
    private void persist() {
        try {
            ActiveAndroid.beginTransaction();
            List<Day> week = ((MainActivity) getActivity()).getWeek();
            for (int i = 0; i < allTableRows.length; i++) {
                Day day = week.get(i);
                day.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    public void clearAllCourses() {
        List<Day> week = ((MainActivity) getActivity()).getWeek();
        for (int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];
            TextView tvA = findById(tr, R.id.textViewA);
            tvA.setText("");

            TextView tvB = findById(tr, R.id.textViewB);
            tvB.setText("");

            findById(tr, R.id.buttonDelA).setEnabled(false);
            findById(tr, R.id.buttonDelB).setEnabled(false);

            week.get(i).firstCourse = null;
            week.get(i).secondCourse = null;
        }
        dirty = true;
    }

    public void randomFillAllCourses() {
        Course course = null;
        List<Day> week = ((MainActivity) getActivity()).getWeek();

        List<Course> notUsedFirstCourses = new ArrayList<>(allFirstCourses);
        List<Course> notUsedSecondCourses = new ArrayList<>(allSecondCourses);

        for (Day day : ((MainActivity) getActivity()).getWeek()) {
            if (day.firstCourse != null) notUsedFirstCourses.remove(day.firstCourse);
            if (day.secondCourse != null) notUsedSecondCourses.remove(day.secondCourse);
        }

        for (int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];
            TextView tvA = (TextView) tr.findViewById(R.id.textViewA);
            if (tvA.getText().equals("")) {
                rand = new Random();

                //Avoid repeating if possible
                if (!notUsedFirstCourses.isEmpty()) {
                    int randomInt = rand.nextInt(notUsedFirstCourses.size());
                    course = notUsedFirstCourses.get(randomInt);
                } else {
                    int randomInt = rand.nextInt(allFirstCourses.size());
                    course = allFirstCourses.get(randomInt);
                }
                week.get(i).firstCourse = course;
                tvA.setText(course.name);
                findById(tr, R.id.buttonDelA).setEnabled(true);
            }
            notUsedFirstCourses.remove(course);

            TextView tvB = (TextView) tr.findViewById(R.id.textViewB);
            if (tvB.getText().equals("")) {
                rand = new Random();

                //Avoid repeating if possible
                if (!notUsedFirstCourses.isEmpty()) {
                    int randomInt = rand.nextInt(notUsedSecondCourses.size());
                    course = notUsedSecondCourses.get(randomInt);
                } else {
                    int randomInt = rand.nextInt(allSecondCourses.size());
                    course = allSecondCourses.get(randomInt);
                }
                week.get(i).secondCourse = course;
                tvB.setText(course.name);
                findById(tr, R.id.buttonDelB).setEnabled(true);
            }
            notUsedSecondCourses.remove(course);
        }
        dirty = true;
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ((MainActivity) getActivity()).onNavigationDrawerItemSelected(COURSES.ordinal());
            getActivity().supportInvalidateOptionsMenu();
        }
    };
}
