package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.MenuItem;

import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.ui.adapters.ShoppingListAdapter;
import com.oscardelgado83.easymenuplanner.util.GA;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

import static com.oscardelgado83.easymenuplanner.util.Cons.DEBUGGING;

/**
 * Created by oscar on 23/03/15.
 */
public class ShoppingListFragment extends ListFragment {

    // Instead of class.getSimpleName() to avoid proGuard changing it.
    public static final String FRAGMENT_NAME = "ShoppingListFragment";
    private static final String LOG_TAG = FRAGMENT_NAME;

    private List<Ingredient> currentIngredientsList;
    private List<Ingredient> allIngredientsList;

    private MenuItem hideCompleted;
    private MenuItem showAll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        allIngredientsList = getIngredients();
        currentIngredientsList = new ArrayList<>(allIngredientsList);

        setListAdapter(new ShoppingListAdapter(getActivity(), currentIngredientsList));
    }

    @DebugLog
    private List<Ingredient> getIngredients() {

        // Day -> Course <- CI -> Ingredient
        List<Ingredient> ingrList = new Select().from(Ingredient.class)
                .where("Id IN (SELECT CI.ingredient FROM CourseIngredients CI, Days D " +
                        "WHERE CI.course = D.firstCourse OR CI.course = D.secondCourse)")
                .orderBy("UPPER (name) ASC")
                .execute();
        return ingrList;
    }

    @DebugLog
    private List<Ingredient> getNotMarkedIngredients() {

        // Day -> Course <- CI -> Ingredient
        List<Ingredient> ingrList = new Select().from(Ingredient.class)
                .where("Id IN (SELECT CI.ingredient FROM CourseIngredients CI, Days D " +
                        "WHERE CI.course = D.firstCourse OR CI.course = D.secondCourse) " +
                        "AND checked = 0")
                .orderBy("checked ASC, UPPER (name) ASC")
                .execute();
        return ingrList;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        GA.sendScreenHit(
                ((EMPApplication) getActivity().getApplication()).getTracker(),
                FRAGMENT_NAME);
    }

    public void hideCompletedItems() {
        currentIngredientsList.clear();
        currentIngredientsList.addAll(getNotMarkedIngredients());
        ((ShoppingListAdapter) getListAdapter()).notifyDataSetChanged();
        refreshMenu();
    }

    public void showAllItems() {
        currentIngredientsList.clear();
        currentIngredientsList.addAll(allIngredientsList);
        ((ShoppingListAdapter) getListAdapter()).notifyDataSetChanged();
        refreshMenu();
    }

    public void refreshMenu() {
        int visibleCheckedItems = countVisibleChecked();
        int hiddenItems = allIngredientsList.size() - currentIngredientsList.size();

        if (DEBUGGING) Log.d(LOG_TAG, "checkedItems: " + visibleCheckedItems);
        if (DEBUGGING) Log.d(LOG_TAG, "hiddenItems:" + hiddenItems);

        hideCompleted.setVisible(visibleCheckedItems > 0);
        showAll.setVisible(hiddenItems > 0);
    }

    @DebugLog
    private int countVisibleChecked() {
        List<Ingredient> allUncheckedItems = new Select().from(Ingredient.class)
                .where("Id IN (SELECT CI.ingredient FROM CourseIngredients CI, Days D " +
                        "WHERE CI.course = D.firstCourse OR CI.course = D.secondCourse) " +
                        "AND checked = 0")
                .execute();

        // All visible items.
        List<Ingredient> visibleCheckedItems = new ArrayList<>(currentIngredientsList);

        // Remove unchecked -> now we have checked visible items.
        visibleCheckedItems.removeAll(allUncheckedItems);

        return visibleCheckedItems.size();
    }

    public void setHideCompleted(MenuItem hideCompleted) {
        this.hideCompleted = hideCompleted;
    }

    public void setShowAll(MenuItem showAll) {
        this.showAll = showAll;
    }
}
