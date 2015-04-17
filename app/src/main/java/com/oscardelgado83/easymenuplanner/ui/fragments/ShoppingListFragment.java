package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.model.CourseIngredient;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.ui.adapters.ShoppingListAdapter;
import com.oscardelgado83.easymenuplanner.util.GA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;

/**
* Created by oscar on 23/03/15.
*/
public class ShoppingListFragment extends ListFragment {

    private static final String LOG_TAG = ShoppingListFragment.class.getSimpleName();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: substitute with a query
        List<Day> week = ((MainActivity) getActivity()).getWeek();
        Set<Ingredient> ingredientSet = new HashSet<>();
        for (int i = 0; i < MainActivity.WEEKDAYS; i++) {
            Day day = week.get(i);
            List<CourseIngredient> ciList = new ArrayList<>();
            if (day.firstCourse != null) ciList.addAll(day.firstCourse.getIngredients());
            if (day.secondCourse != null) ciList.addAll(day.secondCourse.getIngredients());
            for (CourseIngredient ci : ciList) {
                ingredientSet.add(ci.ingredient);
            }
        }

        setListAdapter(new ShoppingListAdapter(getActivity(), new ArrayList(ingredientSet)));
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

    @Override
    public void onResume() {
        super.onResume();

        GA.sendScreenHit(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                getClass().getSimpleName());
    }
}
