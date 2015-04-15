package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.ui.adapters.ShoppingListAdapter;
import com.oscardelgado83.easymenuplanner.util.GA;

import java.util.List;

import butterknife.ButterKnife;

/**
* Created by oscar on 23/03/15.
*/
public class ShoppingListFragment extends ListFragment {

    private static final String LOG_TAG = ShoppingListFragment.class.getSimpleName();

    private List<Ingredient> ingredientList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ingredientList = new Select().from(Ingredient.class).execute();

        setListAdapter(new ShoppingListAdapter(getActivity(), ingredientList));
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
