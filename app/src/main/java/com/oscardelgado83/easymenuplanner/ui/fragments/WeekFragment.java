package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import static com.oscardelgado83.easymenuplanner.ui.fragments.NavigationDrawerFragment.Section.COURSES;
import static com.oscardelgado83.easymenuplanner.util.Cons.DEBUGGING;

/**
 * Created by oscar on 23/03/15.
 */
public class WeekFragment extends Fragment {

    // Instead of class.getSimpleName() to avoid proGuard changing it.
    private static final String FRAGMENT_NAME = "WeekFragment";
    private static final String LOG_TAG = FRAGMENT_NAME;
    private static final int FIRST_COURSE = 0;
    private static final int SECOND_COURSE = 1;
    private static final int BREAKFAST = 2;
    private static final int DINNER = 3;
    private static final int DINNER_SECOND = 4;

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
    private List<Course> allBreakfasts;
    private List<Course> allDinners;
    private List<Course> allDinnerSecondCourses;

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
                .where("firstCourse = 1")
                .orderBy("UPPER(name)")
                .execute();

        allSecondCourses = new Select()
                .from(Course.class)
                .where("secondCourse = 1")
                .orderBy("UPPER(name)")
                .execute();

        allBreakfasts = new Select()
                .from(Course.class)
                .where("breakfast = 1")
                .orderBy("UPPER(name)")
                .execute();

        allDinners = new Select()
                .from(Course.class)
                .where("dinner = 1")
                .orderBy("UPPER(name)")
                .execute();

        allDinnerSecondCourses = new Select()
                .from(Course.class)
                .where("dinnerSecondCourse = 1")
                .orderBy("UPPER(name)")
                .execute();

        allTableRows = new TableRow[]{tableRow1, tableRow2, tableRow3, tableRow4, tableRow5, tableRow6, tableRow7};
        dayNames = new DateFormatSymbols().getShortWeekdays();
    }

    private boolean checkEnoughCoursesExist() {
        if (allFirstCourses.size() < 2 || allSecondCourses.size() < 2
                || (((MainActivity) getActivity()).isBreakfastEnabled() && allBreakfasts.size() < 2)
                || (((MainActivity) getActivity()).isDinnerEnabled() && allDinners.size() < 2)
                || (((MainActivity) getActivity()).isDinnerEnabled() && allDinnerSecondCourses.size() < 2)) {
            String message = null;
            if (allFirstCourses.size() < 2) {
                message = getString(R.string.first_courses_needed);
            } else if (allSecondCourses.size() < 2) {
                message = getString(R.string.second_course_needed);
            } else if (((MainActivity) getActivity()).isBreakfastEnabled() && allBreakfasts.size() < 2) {
                message = getString(R.string.breakfast_needed);
            } else if (((MainActivity) getActivity()).isDinnerEnabled() && allDinners.size() < 2) {
                message = getString(R.string.dinner_needed);
            } else if (((MainActivity) getActivity()).isDinnerEnabled() && allDinnerSecondCourses.size() < 2) {
                message = getString(R.string.dinner_second_needed);
            }
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.not_enough_courses))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.go_to_courses), dialogClickListener)
                    .create().show();

            return false;
        }
        return true;
    }

    private void repaintWeekRows() {
        final List<Day> week = ((MainActivity) getActivity()).getWeek();

        boolean includeBreakfast = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Cons.INCLUDE_BREAKFAST, false);
        boolean includeLunch = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Cons.INCLUDE_LUNCH, true);
        boolean includeDinner = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Cons.INCLUDE_DINNER, false);
        int breakfastVisibility = includeBreakfast ? View.VISIBLE : View.GONE;
        int lunchVisibility = includeLunch ? View.VISIBLE : View.GONE;
        int dinnerVisibility = includeDinner ? View.VISIBLE : View.GONE;

        headers.findViewById(R.id.breakfast_header).setVisibility(breakfastVisibility);
        headers.findViewById(R.id.first_course_header).setVisibility(lunchVisibility);
        headers.findViewById(R.id.second_course_header).setVisibility(lunchVisibility);
        headers.findViewById(R.id.dinner_header).setVisibility(dinnerVisibility);
        headers.findViewById(R.id.dinner_second_header).setVisibility(dinnerVisibility);

        int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Sunday is 1, Saturday is 7.
        boolean dayIsPast = true;
        for (int i = 0; i < allTableRows.length; i++) {
            TableRow tr = allTableRows[i];

            findById(tr, R.id.card_view_breakfast).setVisibility(breakfastVisibility);
            findById(tr, R.id.card_view_dinner).setVisibility(dinnerVisibility);
            findById(tr, R.id.card_view_dinner_second).setVisibility(dinnerVisibility);
            findById(tr, R.id.card_view_first_course).setVisibility(lunchVisibility);
            findById(tr, R.id.card_view_second_course).setVisibility(lunchVisibility);

            TextView tvFirstCourse = (TextView) findById(tr, R.id.card_view_first_course).findViewById(R.id.textView);
            TextView tvSecondCourse = (TextView) findById(tr, R.id.card_view_second_course).findViewById(R.id.textView);
            TextView tvDinner = (TextView) findById(tr, R.id.card_view_dinner).findViewById(R.id.textView);
            TextView tvDinnerSecond = (TextView) findById(tr, R.id.card_view_dinner_second).findViewById(R.id.textView);
            TextView tvBreakfast = (TextView) findById(tr, R.id.card_view_breakfast).findViewById(R.id.textView);

            TextView weekDayName = findById(tr, R.id.week_day_name);
            View placeholderFirstCourse = findById(tr, R.id.card_view_first_course).findViewById(R.id.badge_placeholder);
            View placeholderSecondCourse = findById(tr, R.id.card_view_second_course).findViewById(R.id.badge_placeholder);
            View placeholderDinner = findById(tr, R.id.card_view_dinner).findViewById(R.id.badge_placeholder);
            View placeholderDinnerSecond = findById(tr, R.id.card_view_dinner_second).findViewById(R.id.badge_placeholder);
            View placeholderBreakfast = findById(tr, R.id.card_view_breakfast).findViewById(R.id.badge_placeholder);

            int indexWithCurrentOrder = (i + EMPApplication.USER_WEEK_START_DAY - 1) % (dayNames.length - 1) + 1;
            weekDayName.setText(dayNames[indexWithCurrentOrder]);

            if (indexWithCurrentOrder == currentDayOfWeek) {
                tr.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background));
                weekDayName.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                dayIsPast = false;
            } else {
                tr.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
                weekDayName.setTextColor(ContextCompat.getColor(getContext(), R.color.accent));
            }

            if (week == null || week.isEmpty()) {
                if (DEBUGGING) Log.w(LOG_TAG, "The week has not been initialized.");
            } else {
                if (dayIsPast)
                    weekDayName.setTextColor(ContextCompat.getColor(getContext(), R.color.light_text));
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
                if (week.get(i).dinnerSecondCourse != null) {
                    final Course course = week.get(i).dinnerSecondCourse;
                    tvDinnerSecond.setText(course.name);
                    if (!dayIsPast) {
                        prepareBadge(placeholderDinnerSecond, course);
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
            findById(tr, R.id.card_view_first_course).setOnClickListener(courseBtnClickListener(tvFirstCourse, placeholderFirstCourse, i, FIRST_COURSE));
            //TODO: swipe
//            findById(tr, R.id.buttonLeftB).setOnClickListener(courseBtnClickListener(tvB, placeholderB, i, 1));
//            findById(tr, R.id.buttonRightB).setOnClickListener(courseBtnClickListener(tvB, placeholderB, i, 1));
            findById(tr, R.id.card_view_second_course).setOnClickListener(courseBtnClickListener(tvSecondCourse, placeholderSecondCourse, i, SECOND_COURSE));

            findById(tr, R.id.card_view_dinner).setOnClickListener(courseBtnClickListener(tvDinner, placeholderDinner, i, DINNER));
            findById(tr, R.id.card_view_dinner_second).setOnClickListener(courseBtnClickListener(tvDinnerSecond, placeholderDinnerSecond, i, DINNER_SECOND));
            findById(tr, R.id.card_view_breakfast).setOnClickListener(courseBtnClickListener(tvBreakfast, placeholderBreakfast, i, BREAKFAST));

            placeholderFirstCourse.setTag(R.id.DAY_IS_PAST_KEY, dayIsPast);
            placeholderSecondCourse.setTag(R.id.DAY_IS_PAST_KEY, dayIsPast);
            placeholderDinner.setTag(R.id.DAY_IS_PAST_KEY, dayIsPast);
            placeholderDinnerSecond.setTag(R.id.DAY_IS_PAST_KEY, dayIsPast);
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
                    case R.id.card_view_dinner_second:
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
                if (col == FIRST_COURSE) {
                    selectedDay.firstCourse = newCourse;
                } else if (col == SECOND_COURSE) {
                    selectedDay.secondCourse = newCourse;
                } else if (col == BREAKFAST) {
                    selectedDay.breakfast = newCourse;
                } else if (col == DINNER) {
                    selectedDay.dinner = newCourse;
                } else if (col == DINNER_SECOND) {
                    selectedDay.dinnerSecondCourse = newCourse;
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
                        if (col == FIRST_COURSE) {
                            selectedDay.firstCourse = null;
                        } else if (col == SECOND_COURSE) {
                            selectedDay.secondCourse = null;
                        } else if (col == BREAKFAST) {
                            selectedDay.breakfast = null;
                        } else if (col == DINNER) {
                            selectedDay.dinner = null;
                        } else if (col == DINNER_SECOND) {
                            selectedDay.dinnerSecondCourse = null;
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
        if (col == FIRST_COURSE) {
            allCourses = allFirstCourses;
        } else if (col == SECOND_COURSE) {
            allCourses = allSecondCourses;
        } else if (col == BREAKFAST) {
            allCourses = allBreakfasts;
        } else if (col == DINNER) {
            allCourses = allDinners;
        } else if (col == DINNER_SECOND) {
            allCourses = allDinnerSecondCourses;
        }

        if (checkEnoughCoursesExist()) {

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
                    if (col == FIRST_COURSE) {
                        selectedDay.firstCourse = selectedCourse;
                    } else if (col == SECOND_COURSE) {
                        selectedDay.secondCourse = selectedCourse;
                    } else if (col == BREAKFAST) {
                        selectedDay.breakfast = selectedCourse;
                    } else if (col == DINNER) {
                        selectedDay.dinner = selectedCourse;
                    } else if (col == DINNER_SECOND) {
                        selectedDay.dinnerSecondCourse = selectedCourse;
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
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            Activity activity = (Activity) context;
            try {
                ((MainActivity) activity).onSectionAttached(this);
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
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

            placeholder = findById(tr, R.id.card_view_dinner_second).findViewById(R.id.badge_placeholder);
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

            TextView tvDinnerSecond = (TextView) findById(tr, R.id.card_view_dinner_second).findViewById(R.id.textView);
            View placeholderDinnerSecond = findById(tr, R.id.card_view_dinner_second).findViewById(R.id.badge_placeholder);

            tvDinnerSecond.setText("");
            clearBadge(placeholderDinnerSecond);

            TextView tvBreakfast = (TextView) findById(tr, R.id.card_view_breakfast).findViewById(R.id.textView);
            View placeholderBreakfast = findById(tr, R.id.card_view_breakfast).findViewById(R.id.badge_placeholder);

            tvBreakfast.setText("");
            clearBadge(placeholderBreakfast);

            week.get(i).firstCourse = null;
            week.get(i).secondCourse = null;
            week.get(i).dinner = null;
            week.get(i).dinnerSecondCourse = null;
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
        if (checkEnoughCoursesExist()) {

            Course course = null;
            List<Day> week = ((MainActivity) getActivity()).getWeek();

            List<Course> notUsedFirstCourses = new ArrayList<>(allFirstCourses);
            List<Course> notUsedSecondCourses = new ArrayList<>(allSecondCourses);
            List<Course> notUsedBreakfasts = new ArrayList<>(allBreakfasts);
            List<Course> notUsedDinners = new ArrayList<>(allDinners);
            List<Course> notUsedDinnerSeconds = new ArrayList<>(allDinnerSecondCourses);

            rand = new Random();

            for (Day day : ((MainActivity) getActivity()).getWeek()) {
                if (day.firstCourse != null) {

                    // Remove from all lists (If type is multiple, it might be in all lists).
                    notUsedFirstCourses.remove(day.firstCourse);
                    notUsedSecondCourses.remove(day.firstCourse);
                    notUsedBreakfasts.remove(day.firstCourse);
                    notUsedDinners.remove(day.firstCourse);
                    notUsedDinnerSeconds.remove(day.firstCourse);
                }
                if (day.secondCourse != null) {

                    // Remove from all lists (If type is multiple, it might be in all lists).
                    notUsedFirstCourses.remove(day.secondCourse);
                    notUsedSecondCourses.remove(day.secondCourse);
                    notUsedBreakfasts.remove(day.secondCourse);
                    notUsedDinners.remove(day.secondCourse);
                    notUsedDinnerSeconds.remove(day.secondCourse);
                }
                if (day.breakfast != null) {

                    // Remove from all lists (If type is multiple, it might be in all lists).
                    notUsedFirstCourses.remove(day.breakfast);
                    notUsedSecondCourses.remove(day.breakfast);
                    notUsedBreakfasts.remove(day.breakfast);
                    notUsedDinners.remove(day.breakfast);
                    notUsedDinnerSeconds.remove(day.breakfast);
                }
                if (day.dinner != null) {

                    // Remove from all lists (If type is multiple, it might be in all lists).
                    notUsedFirstCourses.remove(day.dinner);
                    notUsedSecondCourses.remove(day.dinner);
                    notUsedBreakfasts.remove(day.dinner);
                    notUsedDinners.remove(day.dinner);
                    notUsedDinnerSeconds.remove(day.dinner);
                }
                if (day.dinnerSecondCourse != null) {

                    // Remove from all lists (If type is multiple, it might be in all lists).
                    notUsedFirstCourses.remove(day.dinnerSecondCourse);
                    notUsedSecondCourses.remove(day.dinnerSecondCourse);
                    notUsedBreakfasts.remove(day.dinnerSecondCourse);
                    notUsedDinners.remove(day.dinnerSecondCourse);
                    notUsedDinnerSeconds.remove(day.dinnerSecondCourse);
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

                // Remove from all lists (If type is multiple, it might be in all lists).
                notUsedFirstCourses.remove(course);
                notUsedSecondCourses.remove(course);
                notUsedBreakfasts.remove(course);
                notUsedDinners.remove(course);
                notUsedDinnerSeconds.remove(course);

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

                // Remove from all lists (If type is multiple, it might be in all lists).
                notUsedFirstCourses.remove(course);
                notUsedSecondCourses.remove(course);
                notUsedBreakfasts.remove(course);
                notUsedDinners.remove(course);
                notUsedDinnerSeconds.remove(course);

                if (((MainActivity) getActivity()).isBreakfastEnabled()) {
                    TextView tvBreakfast = (TextView) tr.findViewById(R.id.card_view_breakfast).findViewById(R.id.textView);
                    if (tvBreakfast.getText().equals("")) {

                        //Avoid repeating if possible
                        if (!notUsedBreakfasts.isEmpty()) {
                            int randomInt = rand.nextInt(notUsedBreakfasts.size());
                            course = notUsedBreakfasts.get(randomInt);
                        } else {
                            int randomInt = rand.nextInt(allBreakfasts.size());//FIXME: java.lang.IllegalArgumentException
                            course = allBreakfasts.get(randomInt);
                        }
                        week.get(i).breakfast = course;
                    }

                    // Remove from all lists (If type is multiple, it might be in all lists).
                    notUsedFirstCourses.remove(course);
                    notUsedSecondCourses.remove(course);
                    notUsedBreakfasts.remove(course);
                    notUsedDinners.remove(course);
                    notUsedDinnerSeconds.remove(course);
                }

                if (((MainActivity) getActivity()).isDinnerEnabled()) {
                    TextView tvDinner = (TextView) tr.findViewById(R.id.card_view_dinner).findViewById(R.id.textView);
                    if (tvDinner.getText().equals("")) {

                        //Avoid repeating if possible
                        if (!notUsedDinners.isEmpty()) {
                            int randomInt = rand.nextInt(notUsedDinners.size());
                            course = notUsedDinners.get(randomInt);
                        } else {
                            int randomInt = rand.nextInt(allDinners.size());
                            course = allDinners.get(randomInt);
                        }
                        week.get(i).dinner = course;
                    }

                    // Remove from all lists (If type is multiple, it might be in all lists).
                    notUsedFirstCourses.remove(course);
                    notUsedSecondCourses.remove(course);
                    notUsedBreakfasts.remove(course);
                    notUsedDinners.remove(course);
                    notUsedDinnerSeconds.remove(course);

                    TextView tvDinnerSecond = (TextView) tr.findViewById(R.id.card_view_dinner_second).findViewById(R.id.textView);
                    if (tvDinnerSecond.getText().equals("")) {

                        //Avoid repeating if possible
                        if (!notUsedDinners.isEmpty()) {
                            int randomInt = rand.nextInt(notUsedDinnerSeconds.size());
                            course = notUsedDinnerSeconds.get(randomInt);
                        } else {
                            int randomInt = rand.nextInt(allDinnerSecondCourses.size());
                            course = allDinnerSecondCourses.get(randomInt);
                        }
                        week.get(i).dinnerSecondCourse = course;
                    }

                    // Remove from all lists (If type is multiple, it might be in all lists).
                    notUsedFirstCourses.remove(course);
                    notUsedSecondCourses.remove(course);
                    notUsedBreakfasts.remove(course);
                    notUsedDinners.remove(course);
                    notUsedDinnerSeconds.remove(course);
                }
            }
            dirty = true;

            GA.sendEvent(
                    ((EMPApplication) getActivity().getApplication()).getTracker(),
                    FRAGMENT_NAME,
                    "action tapped",
                    "random fill");

            repaintWeekRows();
        }
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
                week.get(i).dinnerSecondCourse = null;
                TableRow tr = allTableRows[i];

                TextView tv = (TextView) findById(tr, R.id.card_view_dinner).findViewById(R.id.textView);
                tv.setText("");
                View placeholder = findById(tr, R.id.card_view_dinner).findViewById(R.id.badge_placeholder);
                clearBadge(placeholder);

                tv = (TextView) findById(tr, R.id.card_view_dinner_second).findViewById(R.id.textView);
                tv.setText("");
                placeholder = findById(tr, R.id.card_view_dinner_second).findViewById(R.id.badge_placeholder);
                clearBadge(placeholder);
            }
            dirty = true;
        }
        repaintWeekRows();
    }

    public void setLunchVisibility(boolean includeLunch) {
        final List<Day> week = ((MainActivity) getActivity()).getWeek();
        if (! includeLunch) {
            for (int i = 0; i < allTableRows.length; i++) {
                week.get(i).firstCourse = null;
                week.get(i).secondCourse = null;
                TableRow tr = allTableRows[i];

                TextView tv = (TextView) findById(tr, R.id.card_view_first_course).findViewById(R.id.textView);
                tv.setText("");
                View placeholder = findById(tr, R.id.card_view_first_course).findViewById(R.id.badge_placeholder);
                clearBadge(placeholder);

                tv = (TextView) findById(tr, R.id.card_view_second_course).findViewById(R.id.textView);
                tv.setText("");
                placeholder = findById(tr, R.id.card_view_second_course).findViewById(R.id.badge_placeholder);
                clearBadge(placeholder);
            }
            dirty = true;
        }
        repaintWeekRows();
    }
}
