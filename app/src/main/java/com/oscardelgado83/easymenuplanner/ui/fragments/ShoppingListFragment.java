package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.ui.adapters.ShoppingListAdapter;
import com.oscardelgado83.easymenuplanner.util.GA;

import java.util.List;

import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

/**
* Created by oscar on 23/03/15.
*/
public class ShoppingListFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;
    private List<Ingredient> ingredientList;

    private MenuItem hideCompleted;
    private MenuItem showCompleted;

    private static final String LOG_TAG = ShoppingListFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        ingredientList = getIngredients();
        setListAdapter(new ShoppingListAdapter(getActivity(), ingredientList));
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
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                "ShoppingListFragment");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        hideCompleted = menu.findItem(R.id.action_hide_completed);
        showCompleted = menu.findItem(R.id.action_show_completed);
    }

    public void hideCompletedItems() {
        showCompleted.setVisible(true);
        hideCompleted.setVisible(false);
        ingredientList.clear();
        ingredientList.addAll(getNotMarkedIngredients());
        ((ShoppingListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public void showCompletedItems() {
        showCompleted.setVisible(false);
        hideCompleted.setVisible(true);
        ingredientList.clear();
        ingredientList.addAll(getIngredients());
        ((ShoppingListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Long ingredientId);
    }
}
