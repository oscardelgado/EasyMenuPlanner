package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.ui.adapters.CourseAdapter;
import com.oscardelgado83.easymenuplanner.util.Cons;
import com.oscardelgado83.easymenuplanner.util.GA;
import com.readystatesoftware.viewbadger.BadgeView;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

    @Bind(R.id.headers)
    TableRow headers;

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

        refreshWeekData();

        repaintWeekRows();

        return view;
    }

    private void refreshWeekData() {
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
    }

    private void repaintWeekRows() {
        final List<Day> week = ((MainActivity) getActivity()).getWeek();

        boolean includeDinner = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Cons.INCLUDE_DINNER, false);
        boolean includeBreakfast = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Cons.INCLUDE_BREAKFAST, false);
        int dinnerVisibility = includeDinner ? View.VISIBLE : View.GONE;
        int breakfastVisibility = includeBreakfast ? View.VISIBLE : View.GONE;

        headers.findViewById(R.id.dinner_header).setVisibility(dinnerVisibility);
        headers.findViewById(R.id.breakfast_header).setVisibility(breakfastVisibility);

        int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Sunday is 1, Saturday is 7.
        boolean dayIsPast = true;
        for (int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];

            findById(tr, R.id.card_view_dinner).setVisibility(dinnerVisibility);
            findById(tr, R.id.card_view_breakfast).setVisibility(breakfastVisibility);

            TextView tvFirstCourse = (TextView) findById(tr, R.id.card_view_first_course).findViewById(R.id.textView);
            TextView tvSecondCourse = (TextView) findById(tr, R.id.card_view_second_course).findViewById(R.id.textView);
            TextView tvDinner = (TextView) findById(tr, R.id.card_view_dinner).findViewById(R.id.textView);
            TextView tvBreakfast = (TextView) findById(tr, R.id.card_view_breakfast).findViewById(R.id.textView);

            TextView weekDayName = findById(tr, R.id.week_day_name);
            View placeholderFirstCourse = findById(tr, R.id.card_view_first_course).findViewById(R.id.badge_placeholder);
            View placeholderSecondCourse = findById(tr, R.id.card_view_second_course).findViewById(R.id.badge_placeholder);
            View placeholderDinner = findById(tr, R.id.card_view_dinner).findViewById(R.id.badge_placeholder);
            View placeholderBreakfast = findById(tr, R.id.card_view_breakfast).findViewById(R.id.badge_placeholder);

            int indexWithCurrentOrder = (i + Calendar.getInstance().getFirstDayOfWeek() - 1) % (dayNames.length - 1) + 1;
            weekDayName.setText(dayNames[indexWithCurrentOrder]);

            if (indexWithCurrentOrder == currentDayOfWeek) {
                tr.setBackgroundColor(getResources().getColor(R.color.background));
                weekDayName.setTextColor(getResources().getColor(android.R.color.white));
                dayIsPast = false;
            } else {
                tr.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                weekDayName.setTextColor(getResources().getColor(R.color.accent));
            }

            if (week == null || week.isEmpty()) {
                if (DEBUGGING) Log.w(LOG_TAG, "The week has not been initialized.");
            } else {
                if (dayIsPast)
                    weekDayName.setTextColor(getResources().getColor(R.color.light_text));
                if (week.get(i).firstCourse != null) {
                    final Course course = week.get(i).firstCourse;
                    tvFirstCourse.setText(course.name);

                    if (!dayIsPast) {
                        prepareBadge(placeholderFirstCourse, course);
                    }
                }
                if (week.get(i).secondCourse != null) {
                    final Course course = week.get(i).secondCourse;
                    tvSecondCourse.setText(course.name);
                    if (!dayIsPast) {
                        prepareBadge(placeholderSecondCourse, course);
                    }
                }
                if (week.get(i).dinner != null) {
                    final Course course = week.get(i).dinner;
                    tvDinner.setText(course.name);
                    if (!dayIsPast) {
                        prepareBadge(placeholderDinner, course);
                    }
                }
                if (week.get(i).breakfast != null) {
                    final Course course = week.get(i).breakfast;
                    tvBreakfast.setText(course.name);
                    if (!dayIsPast) {
                        prepareBadge(placeholderBreakfast, course);
                    }
                }
            }
            //TODO: swipe
//            findById(tr, R.id.buttonLeftA).setOnClickListener(courseBtnClickListener(tvA, placeholderA, i, 0));
//            findById(tr, R.id.buttonRightA).setOnClickListener(courseBtnClickListener(tvA, placeholderA, i, 0));
            findById(tr, R.id.card_view_first_course).setOnClickListener(courseBtnClickListener(tvFirstCourse, placeholderFirstCourse, i, 0));
            //TODO: swipe
//            findById(tr, R.id.buttonLeftB).setOnClickListener(courseBtnClickListener(tvB, placeholderB, i, 1));
//            findById(tr, R.id.buttonRightB).setOnClickListener(courseBtnClickListener(tvB, placeholderB, i, 1));
            findById(tr, R.id.card_view_second_course).setOnClickListener(courseBtnClickListener(tvSecondCourse, placeholderSecondCourse, i, 1));

            findById(tr, R.id.card_view_dinner).setOnClickListener(courseBtnClickListener(tvDinner, placeholderDinner, i, 2));
            findById(tr, R.id.card_view_breakfast).setOnClickListener(courseBtnClickListener(tvBreakfast, placeholderBreakfast, i, 3));//TODO: constants

            placeholderFirstCourse.setTag(R.id.DAY_IS_PAST_KEY, dayIsPast);
            placeholderSecondCourse.setTag(R.id.DAY_IS_PAST_KEY, dayIsPast);
            placeholderDinner.setTag(R.id.DAY_IS_PAST_KEY, dayIsPast);
            placeholderBreakfast.setTag(R.id.DAY_IS_PAST_KEY, dayIsPast);
        }
    }

    private void prepareBadge(View placeholder, final Course course) {
        int count = course.getNotCheckedIngredientsCount();
        BadgeView badge = (BadgeView) placeholder.getTag(R.id.BADGE_KEY);
        if (badge == null) {
            badge = new BadgeView(getActivity(), placeholder);
            placeholder.setTag(R.id.BADGE_KEY, badge);
        }
        if (count > 0) {
            badge.setText("" + count);
            badge.show();

            View.OnClickListener badgeClickListener = getBadgeOnClickListener(course);
            badge.setOnClickListener(badgeClickListener);
        } else {
            badge.hide();
        }
    }

    @NonNull
    private View.OnClickListener getBadgeOnClickListener(final Course course) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(),
                        getNotCheckedIngredientsString(course),
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    private String getNotCheckedIngredientsString(Course course) {
        int notCheckedCount = course.getNotCheckedIngredientsCount();
        if (notCheckedCount == 0) {
            return "";
        } else {
            String ingredientString = StringUtils.join(course.getNotCheckedIngredients(), ", ");
            return getResources().getQuantityString(R.plurals.intredients_missing, notCheckedCount, notCheckedCount, ingredientString);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshWeekData();

        repaintWeekRows();

        GA.sendScreenHit(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                FRAGMENT_NAME);
    }

    private View.OnClickListener courseBtnClickListener(final TextView tv, final View placeholder, final int row, final int col) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Day> week = ((MainActivity) getActivity()).getWeek();
                Day selectedDay = week.get(row);
//                Course currentCourse = (col == 0) ? selectedDay.firstCourse : selectedDay.secondCourse;
                Course newCourse = null;
//                ListIterator<Course> it = null;
//                List<Course> currentCoursesList = null;
                //TODO: swipe
//                if (v.getId() == R.id.buttonLeftA || v.getId() == R.id.buttonRightA) {
//                    currentCoursesList = allFirstCourses;
//                } else if (v.getId() == R.id.buttonLeftB || v.getId() == R.id.buttonRightB) {
//                    currentCoursesList = allSecondCourses;
//                }
//                if (currentCoursesList != null) {
//                    if (currentCourse != null && currentCoursesList.contains(currentCourse)) {
//                        it = currentCoursesList.listIterator(currentCoursesList.indexOf(currentCourse));
//                    } else {
//                        it = currentCoursesList.listIterator(0);
//                    }
//                }
                switch (v.getId()) {
                    //TODO: swipe
//                    case R.id.buttonLeftA:
//                    case R.id.buttonLeftB:
//                        do {
//                            if (it.hasPrevious()) {
//                                newCourse = it.previous();
//                            } else {
//                                newCourse = currentCoursesList.get(currentCoursesList.size() - 1);
//                            }
//                        } while (newCourse == currentCourse);
//                        break;
//                    case R.id.buttonRightA:
//                    case R.id.buttonRightB:
//                        do {
//                            if (it.hasNext()) {
//                                newCourse = it.next();
//                            } else {
//                                newCourse = currentCoursesList.get(0);
//                            }
//                        } while (newCourse == currentCourse);
//                        break;
                    case R.id.card_view_first_course:
                    case R.id.card_view_second_course:
                    case R.id.card_view_dinner:
                    case R.id.card_view_breakfast:
                        if (tv.getText().equals("")) {
                            showCoursesDialog(tv, placeholder, row, col);
                        } else {
                            showDeleteOrChangeDialog(tv, placeholder, row, col);
                        }
                        return;
                    default:
                        break;
                }
                if (col == 0) {
                    selectedDay.firstCourse = newCourse;
                } else if (col == 1) {
                    selectedDay.secondCourse = newCourse;
                } else if (col == 2) { //TODO: constants
                    selectedDay.dinner = newCourse;
                } else if (col == 3) {
                    selectedDay.breakfast = newCourse;
                }
                tv.setText(newCourse != null ? newCourse.name : "");
                if (!(boolean) placeholder.getTag(R.id.DAY_IS_PAST_KEY)) { // If day is not past.
                    prepareBadge(placeholder, newCourse);
                }
                dirty = true;
            }
        };
    }

    private void showDeleteOrChangeDialog(final TextView tv, final View placeholder, final int row, final int col) {
        String[] items = {getString(R.string.day_course_change), getString(R.string.day_course_remove),};
        new AlertDialog.Builder(getActivity()).setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showCoursesDialog(tv, placeholder, row, col);
                        break;
                    case 1:
                        List<Day> week = ((MainActivity) getActivity()).getWeek();
                        Day selectedDay = week.get(row);
                        if (col == 0) {
                            selectedDay.firstCourse = null;
                        } else if (col == 1) {
                            selectedDay.secondCourse = null;
                        } else if (col == 2) { //TODO: constants
                            selectedDay.dinner = null;
                        } else if (col == 3) {
                            selectedDay.breakfast = null;
                        }
                        tv.setText("");
                        clearBadge(placeholder);
                        dirty = true;
                        break;
                    default:
                        break;
                }
            }
        }).create().show();
    }

    private void clearBadge(View placeholder) {
        BadgeView badge = (BadgeView) placeholder.getTag(R.id.BADGE_KEY);
        if (badge != null) {
            badge.hide();
        }
    }

    private void showCoursesDialog(final TextView tv, final View placeholder, final int row, final int col) {
        if (dirty) {
            persist();
            dirty = false;
        }

        List<Course> allCourses = null;
        if (col == 0) {
            allCourses = allFirstCourses;
        } else if (col == 1) {
            allCourses = allSecondCourses;
        } else if (col == 2 || col == 3) { //TODO: constants
            allCourses = new Select()
                    .from(Course.class)
                    .orderBy("UPPER(name)")
                    .execute(); // Query instead of union of list, to have the courses in order.
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
                } else if (col == 2) { //TODO: constants
                    selectedDay.dinner = selectedCourse;
                } else if (col == 3) {
                    selectedDay.breakfast = selectedCourse;
                }
                tv.setText(selectedCourse.name);
                if (!(boolean) placeholder.getTag(R.id.DAY_IS_PAST_KEY)) {
                    prepareBadge(placeholder, selectedCourse);
                }
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
            //TODO: swipe
//            findById(tr, R.id.buttonLeftA).setOnClickListener(null);
//            findById(tr, R.id.buttonRightA).setOnClickListener(null);
            View placeholder = findById(tr, R.id.card_view_first_course).findViewById(R.id.badge_placeholder);
            placeholder.setOnClickListener(null);
            BadgeView badgeView = (BadgeView) placeholder.getTag(R.id.BADGE_KEY);
            if (badgeView != null) badgeView.setOnClickListener(null);

            //TODO: swipe
//            findById(tr, R.id.buttonLeftB).setOnClickListener(null);
//            findById(tr, R.id.buttonRightB).setOnClickListener(null);
            placeholder = findById(tr, R.id.card_view_second_course).findViewById(R.id.badge_placeholder);
            placeholder.setOnClickListener(null);
            badgeView = (BadgeView) placeholder.getTag(R.id.BADGE_KEY);
            if (badgeView != null) badgeView.setOnClickListener(null);

            placeholder = findById(tr, R.id.card_view_dinner).findViewById(R.id.badge_placeholder);
            placeholder.setOnClickListener(null);
            badgeView = (BadgeView) placeholder.getTag(R.id.BADGE_KEY);
            if (badgeView != null) badgeView.setOnClickListener(null);

            placeholder = findById(tr, R.id.card_view_breakfast).findViewById(R.id.badge_placeholder);
            placeholder.setOnClickListener(null);
            badgeView = (BadgeView) placeholder.getTag(R.id.BADGE_KEY);
            if (badgeView != null) badgeView.setOnClickListener(null);
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

            TextView tvFirstCourse = (TextView) findById(tr, R.id.card_view_first_course).findViewById(R.id.textView);
            View placeholderFirstCourse = findById(tr, R.id.card_view_first_course).findViewById(R.id.badge_placeholder);

            tvFirstCourse.setText("");
            clearBadge(placeholderFirstCourse);

            TextView tvSecondCourse = (TextView) findById(tr, R.id.card_view_second_course).findViewById(R.id.textView);
            View placeholderSecondCourse = findById(tr, R.id.card_view_second_course).findViewById(R.id.badge_placeholder);

            tvSecondCourse.setText("");
            clearBadge(placeholderSecondCourse);

            TextView tvDinner = (TextView) findById(tr, R.id.card_view_dinner).findViewById(R.id.textView);
            View placeholderDinner = findById(tr, R.id.card_view_dinner).findViewById(R.id.badge_placeholder);

            tvDinner.setText("");
            clearBadge(placeholderDinner);

            TextView tvBreakfast = (TextView) findById(tr, R.id.card_view_breakfast).findViewById(R.id.textView);
            View placeholderBreakfast = findById(tr, R.id.card_view_breakfast).findViewById(R.id.badge_placeholder);

            tvBreakfast.setText("");
            clearBadge(placeholderBreakfast);

            week.get(i).firstCourse = null;
            week.get(i).secondCourse = null;
            week.get(i).dinner = null;
            week.get(i).breakfast = null;
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
            TextView tvFirstCourse = (TextView) tr.findViewById(R.id.card_view_first_course).findViewById(R.id.textView);

            if (tvFirstCourse.getText().equals("")) {

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

            TextView tvSecondCourse = (TextView) tr.findViewById(R.id.card_view_second_course).findViewById(R.id.textView);
            if (tvSecondCourse.getText().equals("")) {

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

            TextView tvDinner = (TextView) tr.findViewById(R.id.card_view_dinner).findViewById(R.id.textView);
            if (tvDinner.getText().equals("")) {

                //Avoid repeating if possible
                List<Course> allNotUsedCourses = new ArrayList<>(notUsedFirstCourses);
                allNotUsedCourses.addAll(notUsedSecondCourses);
                List<Course> allCourses = new ArrayList<>(allFirstCourses);
                allCourses.addAll(allSecondCourses);
                if (!allNotUsedCourses.isEmpty()) {
                    int randomInt = rand.nextInt(allNotUsedCourses.size());
                    course = allNotUsedCourses.get(randomInt);
                } else if (!allCourses.isEmpty()) {
                    int randomInt = rand.nextInt(allCourses.size());
                    course = allCourses.get(randomInt);
                }
                week.get(i).dinner = course;
            }

            // Remove from both lists (If type is "both", it might be in both lists).
            notUsedFirstCourses.remove(course);
            notUsedSecondCourses.remove(course);

            TextView tvBreakfast = (TextView) tr.findViewById(R.id.card_view_breakfast).findViewById(R.id.textView);
            if (tvBreakfast.getText().equals("")) {

                // Breakfast is repeatable
                List<Course> allCourses = new ArrayList<>(allFirstCourses);
                allCourses.addAll(allSecondCourses);
                if (!allCourses.isEmpty()) {
                    int randomInt = rand.nextInt(allCourses.size());
                    course = allCourses.get(randomInt);
                }
                week.get(i).breakfast = course;
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
            //TODO: check this vs  mNavigationDrawerFragment.selectItem()
        }
    };


    public void setBreakfastVisibility(boolean includeBreakfast) {
        final List<Day> week = ((MainActivity) getActivity()).getWeek();
        if (! includeBreakfast) {
            for (int i = 0; i < allTableRows.length; i++) {
                week.get(i).breakfast = null;
                TableRow tr = allTableRows[i];

                TextView tv = (TextView) findById(tr, R.id.card_view_breakfast).findViewById(R.id.textView);
                tv.setText("");
                View placeholder = findById(tr, R.id.card_view_breakfast).findViewById(R.id.badge_placeholder);
                clearBadge(placeholder);
            }
            dirty = true;
        }
        repaintWeekRows();
    }

    public void setDinnerVisibility(boolean includeDinner) {
        final List<Day> week = ((MainActivity) getActivity()).getWeek();
        if (! includeDinner) {
            for (int i = 0; i < allTableRows.length; i++) {
                week.get(i).dinner = null;
                TableRow tr = allTableRows[i];

                TextView tv = (TextView) findById(tr, R.id.card_view_dinner).findViewById(R.id.textView);
                tv.setText("");
                View placeholder = findById(tr, R.id.card_view_dinner).findViewById(R.id.badge_placeholder);
                clearBadge(placeholder);
            }
            dirty = true;
        }
        repaintWeekRows();
    }
}
