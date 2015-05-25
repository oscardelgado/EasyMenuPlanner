package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
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

        int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Sunday is 1, Saturday is 7.

        allTableRows = new TableRow[]{tableRow1, tableRow2, tableRow3, tableRow4, tableRow5, tableRow6, tableRow7};
        for (int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];
            tr.setTag(i);

            TextView tvA = findById(tr, R.id.textViewA);
            TextView tvB = findById(tr, R.id.textViewB);
            TextView weekDayName = findById(tr, R.id.week_day_name);

            String[] dayNames = new DateFormatSymbols().getShortWeekdays();
            int firstDay = Calendar.getInstance().getFirstDayOfWeek();
            int indexWithCurrentOrder = (i + firstDay - 1) % (dayNames.length - 1) + 1;
            weekDayName.setText(dayNames[indexWithCurrentOrder]);
            if (indexWithCurrentOrder == currentDayOfWeek) {
                tr.setBackgroundColor(getResources().getColor(R.color.background));
                weekDayName.setTextColor(getResources().getColor(android.R.color.white));
            }

            if (week == null || week.isEmpty()) {
                Log.w(LOG_TAG, "The week has not been initialized.");
            } else {
                if (week.get(i).firstCourse != null) {
                    tvA.setText(week.get(i).firstCourse.name);
                }
                if (week.get(i).secondCourse != null) {
                    tvB.setText(week.get(i).secondCourse.name);
                }
            }
            findById(tr, R.id.buttonLeftA).setOnClickListener(courseBtnClickListener(tvA, i, 0));
            findById(tr, R.id.buttonRightA).setOnClickListener(courseBtnClickListener(tvA, i, 0));
            findById(tr, R.id.card_view_left).setOnClickListener(courseBtnClickListener(tvA, i, 0));
            findById(tr, R.id.buttonLeftB).setOnClickListener(courseBtnClickListener(tvB, i, 1));
            findById(tr, R.id.buttonRightB).setOnClickListener(courseBtnClickListener(tvB, i, 1));
            findById(tr, R.id.card_view_right).setOnClickListener(courseBtnClickListener(tvB, i, 1));
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
                    case R.id.buttonLeftA:
                    case R.id.buttonLeftB:
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
                        do {
                            if (it.hasNext()) {
                                newCourse = it.next();
                            } else {
                                newCourse = currentCoursesList.get(0);
                            }
                        } while (newCourse == currentCourse);
                        break;
                    case R.id.card_view_left:
                    case R.id.card_view_right:
                        if (tv.getText().equals("")) {
                            showCoursesDialog(tv, row, col);
                        } else {
                            showDeleteOrSearchDialog(tv, row, col);
                        }
                        return;
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

    private void showDeleteOrSearchDialog(final TextView tv, final int row, final int col) {
        String[] items = {getString(R.string.day_course_change), getString(R.string.day_course_remove), };
        new AlertDialog.Builder(getActivity()).setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showCoursesDialog(tv, row, col);
                        break;
                    case 1:
                        List<Day> week = ((MainActivity) getActivity()).getWeek();
                        Day selectedDay = week.get(row);
                        if (col == 0) {
                                selectedDay.firstCourse = null;
                            } else if (col == 1) {
                                selectedDay.secondCourse = null;
                            }
                        tv.setText("");
                        dirty = true;
                        break;
                    default:
                        break;
                }
            }
        }).create().show();
    }

    private void showCoursesDialog(final TextView tv, final int row, final int col) {
        List<Course> allCourses = null;
        if (col == 0) {
            allCourses = allFirstCourses;
        } else if (col == 1) {
            allCourses = allSecondCourses;
        }
        final List<Course> finalAllCourses = allCourses;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_course);
        ArrayAdapter<Course> adapter = new CourseAdapter(getActivity(), allCourses);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Course selectedCourse = finalAllCourses.get(which);
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

        alert.getListView().setFastScrollEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            alert.getListView().setFastScrollAlwaysVisible(true);
        }
    }

    @Override
    public void onAttach(Activity activity) {
            super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        allTableRows = new TableRow[]{tableRow1, tableRow2, tableRow3, tableRow4, tableRow5, tableRow6, tableRow7};
        for (int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];
            findById(tr, R.id.buttonLeftA).setOnClickListener(null);
            findById(tr, R.id.buttonRightA).setOnClickListener(null);
            findById(tr, R.id.card_view_left).setOnClickListener(null);
            findById(tr, R.id.buttonLeftB).setOnClickListener(null);
            findById(tr, R.id.buttonRightB).setOnClickListener(null);
            findById(tr, R.id.card_view_right).setOnClickListener(null);
        }
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
