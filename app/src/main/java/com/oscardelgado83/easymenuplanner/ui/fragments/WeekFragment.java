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

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

import static butterknife.ButterKnife.findById;
import static butterknife.ButterKnife.unbind;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.FIRST;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.NONE;
import static com.oscardelgado83.easymenuplanner.model.Course.CourseType.SECOND;
import static com.oscardelgado83.easymenuplanner.ui.fragments.NavigationDrawerFragment.Section.COURSES;
import static com.oscardelgado83.easymenuplanner.util.Cons.DEBUGGING;

/**
 * Created by oscar on 23/03/15.
 */
public class WeekFragment extends Fragment {

    // Instead of class.getSimpleName() to avoid proGuard changing it.
    private static final String FRAGMENT_NAME = "WeekFragment";
    private static final String LOG_TAG = FRAGMENT_NAME;

    @Bind(R.id.day1)
    TableRow tableRow1;

    @Bind(R.id.day2)
    TableRow tableRow2;

    @Bind(R.id.day3)
    TableRow tableRow3;

    @Bind(R.id.day4)
    TableRow tableRow4;

    @Bind(R.id.day5)
    TableRow tableRow5;

    @Bind(R.id.day6)
    TableRow tableRow6;

    @Bind(R.id.day7)
    TableRow tableRow7;

    private TableRow[] allTableRows;
    private List<Course> allFirstCourses;

    private List<Course> allSecondCourses;

    private boolean dirty;

    private Random rand;
    private String[] dayNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);
        ButterKnife.bind(this, view);

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
        dayNames = new DateFormatSymbols().getShortWeekdays();

        repaintWeekRows();
        return view;
    }

    private void repaintWeekRows() {
        List<Day> week = ((MainActivity) getActivity()).getWeek();

        int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Sunday is 1, Saturday is 7.
        boolean dayIsPast = true;
        for (int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];
            tr.setTag(i);

            TextView tvA = findById(tr, R.id.textViewA);
            TextView ingrA = findById(tr, R.id.ingredientsA);
            TextView tvB = findById(tr, R.id.textViewB);
            TextView ingrB = findById(tr, R.id.ingredientsB);
            TextView weekDayName = findById(tr, R.id.week_day_name);

            int indexWithCurrentOrder = (i + Calendar.getInstance().getFirstDayOfWeek() - 1) % (dayNames.length - 1) + 1;
            weekDayName.setText(dayNames[indexWithCurrentOrder]);

            if (indexWithCurrentOrder == currentDayOfWeek) {
                tr.setBackgroundColor(getResources().getColor(R.color.background));
                weekDayName.setTextColor(getResources().getColor(android.R.color.white));
                dayIsPast = false;
            }

            if (week == null || week.isEmpty()) {
                if (DEBUGGING) Log.w(LOG_TAG, "The week has not been initialized.");
            } else {
                if (dayIsPast) weekDayName.setTextColor(getResources().getColor(R.color.light_text));
                if (week.get(i).firstCourse != null) {
                    tvA.setText(week.get(i).firstCourse.name);
                    if (! dayIsPast) {
                        ingrA.setText(getNotCheckedIngredientsCount(week.get(i).firstCourse));
                    }
                }
                if (week.get(i).secondCourse != null) {
                    tvB.setText(week.get(i).secondCourse.name);
                    if (! dayIsPast) {
                        ingrB.setText(getNotCheckedIngredientsCount(week.get(i).secondCourse));
                    }
                }
            }
            findById(tr, R.id.buttonLeftA).setOnClickListener(courseBtnClickListener(tvA, ingrA, i, 0));
            findById(tr, R.id.buttonRightA).setOnClickListener(courseBtnClickListener(tvA, ingrA, i, 0));
            findById(tr, R.id.card_view_A).setOnClickListener(courseBtnClickListener(tvA, ingrA, i, 0));
            findById(tr, R.id.buttonLeftB).setOnClickListener(courseBtnClickListener(tvB, ingrB, i, 1));
            findById(tr, R.id.buttonRightB).setOnClickListener(courseBtnClickListener(tvB, ingrB, i, 1));
            findById(tr, R.id.card_view_B).setOnClickListener(courseBtnClickListener(tvB, ingrB, i, 1));
        }
    }

    private String getNotCheckedIngredientsCount(Course course) {
        int notCheckedCount = course.getNotCheckedIngredientsCount();
        if (notCheckedCount == 0) {
            return "";
        } else {
            return getResources().getQuantityString(R.plurals.intredients_missing, notCheckedCount, notCheckedCount);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        GA.sendScreenHit(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                FRAGMENT_NAME);
    }

    private View.OnClickListener courseBtnClickListener(final TextView tv, final TextView ingrTv, final int row, final int col) {
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
                    case R.id.card_view_A:
                    case R.id.card_view_B:
                        if (tv.getText().equals("")) {
                            showCoursesDialog(tv, ingrTv, row, col);
                        } else {
                            showDeleteOrChangeDialog(tv, ingrTv, row, col);
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
                ingrTv.setText(newCourse != null ? getNotCheckedIngredientsCount(newCourse) : "");
                dirty = true;
            }
        };
    }

    private void showDeleteOrChangeDialog(final TextView tv, final TextView ingrTv, final int row, final int col) {
        String[] items = {getString(R.string.day_course_change), getString(R.string.day_course_remove), };
        new AlertDialog.Builder(getActivity()).setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showCoursesDialog(tv, ingrTv, row, col);
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
                        ingrTv.setText("");
                        dirty = true;
                        break;
                    default:
                        break;
                }
            }
        }).create().show();
    }

    private void showCoursesDialog(final TextView tv, final TextView ingrTv, final int row, final int col) {
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
                ingrTv.setText(getNotCheckedIngredientsCount(selectedCourse));
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
            findById(tr, R.id.card_view_A).setOnClickListener(null);
            findById(tr, R.id.buttonLeftB).setOnClickListener(null);
            findById(tr, R.id.buttonRightB).setOnClickListener(null);
            findById(tr, R.id.card_view_B).setOnClickListener(null);
        }
        unbind(this);
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
            TextView ingrA = findById(tr, R.id.ingredientsA);
            tvA.setText("");
            ingrA.setText("");

            TextView tvB = findById(tr, R.id.textViewB);
            TextView ingrB = findById(tr, R.id.ingredientsB);

            tvB.setText("");
            ingrB.setText("");

            week.get(i).firstCourse = null;
            week.get(i).secondCourse = null;
        }
        dirty = true;

        GA.sendEvent(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                FRAGMENT_NAME,
                "action tapped",
                "clear all");
    }

    public void randomFillAllCourses() {
        Course course = null;
        List<Day> week = ((MainActivity) getActivity()).getWeek();

        List<Course> notUsedFirstCourses = new ArrayList<>(allFirstCourses);
        List<Course> notUsedSecondCourses = new ArrayList<>(allSecondCourses);

        rand = new Random();

        for (Day day : ((MainActivity) getActivity()).getWeek()) {
            if (day.firstCourse != null) {

                // Remove from both lists (If type is "both", it might be in both lists).
                notUsedFirstCourses.remove(day.firstCourse);
                notUsedSecondCourses.remove(day.firstCourse);
            }
            if (day.secondCourse != null) {

                // Remove from both lists (If type is "both", it might be in both lists).
                notUsedFirstCourses.remove(day.secondCourse);
                notUsedSecondCourses.remove(day.secondCourse);
            }
        }

        for (int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];
            TextView tvA = (TextView) tr.findViewById(R.id.textViewA);

            int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Sunday is 1, Saturday is 7.

            if (tvA.getText().equals("")) {

                //Avoid repeating if possible
                if (!notUsedFirstCourses.isEmpty()) {
                    int randomInt = rand.nextInt(notUsedFirstCourses.size());
                    course = notUsedFirstCourses.get(randomInt);
                } else {
                    int randomInt = rand.nextInt(allFirstCourses.size());
                    course = allFirstCourses.get(randomInt);
                }
                week.get(i).firstCourse = course;
            }

            // Remove from both lists (If type is "both", it might be in both lists).
            notUsedFirstCourses.remove(course);
            notUsedSecondCourses.remove(course);

            TextView tvB = (TextView) tr.findViewById(R.id.textViewB);
            if (tvB.getText().equals("")) {

                //Avoid repeating if possible
                if (!notUsedSecondCourses.isEmpty()) {
                    int randomInt = rand.nextInt(notUsedSecondCourses.size());
                    course = notUsedSecondCourses.get(randomInt);
                } else {
                    int randomInt = rand.nextInt(allSecondCourses.size());
                    course = allSecondCourses.get(randomInt);
                }
                week.get(i).secondCourse = course;
            }

            // Remove from both lists (If type is "both", it might be in both lists).
            notUsedFirstCourses.remove(course);
            notUsedSecondCourses.remove(course);
        }
        dirty = true;

        GA.sendEvent(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                FRAGMENT_NAME,
                "action tapped",
                "random fill");

        repaintWeekRows();
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ((MainActivity) getActivity()).onNavigationDrawerItemSelected(COURSES.ordinal());
            getActivity().supportInvalidateOptionsMenu();
        }
    };
}
