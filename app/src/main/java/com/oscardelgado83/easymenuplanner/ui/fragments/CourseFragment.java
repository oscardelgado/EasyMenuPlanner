package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;
import com.oscardelgado83.easymenuplanner.model.CourseIngredient;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.oscardelgado83.easymenuplanner.ui.IngredientsCompletionView;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.ui.adapters.CourseAdapter;

import java.util.List;

import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class CourseFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;
    private List<Course> courseList;

    private static final String LOG_TAG = CourseFragment.class.getSimpleName();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CourseFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        courseList = new Select().from(Course.class).orderBy("name ASC").execute();

        setListAdapter(new CourseAdapter(getActivity(), courseList));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO: context dialog for Android < 11 (10)
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            public Menu menu;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB

                int count = getListView().getCheckedItemCount();
                if (count == 1) {
                    mode.setSubtitle(getString(R.string.cab_one_item_selected));
                    mode.getMenu().findItem(R.id.action_edit).setVisible(true);
                } else {
                    mode.setSubtitle(count + getString(R.string.cab_elements_selected));//TODO: http://developer.android.com/guide/topics/resources/string-resource.html#Plurals
                    mode.getMenu().findItem(R.id.action_edit).setVisible(false);
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.action_remove:
                        deleteSelectedItems(getListView().getCheckedItemPositions());
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.action_edit:
                        editSelectedItem(getListView().getCheckedItemPositions());
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.courses_context, menu);

                mode.setTitle(getString(R.string.cab_title));

                this.menu = menu;

                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }
        });
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(courseList.get(position).getId());
        }
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
        public void onFragmentInteraction(Long courseId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public void addCourseClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View newCourseView = LayoutInflater.from(getActivity()).inflate(R.layout.course_view, null);
        ButterKnife.inject(this, newCourseView);

        // Auto-complete for Ingredients
        setIngredientsAutocompleteAdapter(newCourseView);

       final AlertDialog d = builder.setTitle(getString(R.string.dialog_new_course_title)).setView(newCourseView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        d.show();
        Button positiveButton = d.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EditText nameET = ButterKnife.findById(d, R.id.name_edit_text);

                IngredientsCompletionView completionView = ButterKnife.findById(d, R.id.ingredients_edit_text);
                addUnconfirmedIngredient(completionView);

                RadioGroup courseTypeRG = ButterKnife.findById(d, R.id.course_type_radio); //TODO

                createCourse(nameET.getText().toString(), completionView.getObjects());

                d.dismiss();

                getListView().smoothScrollToPosition(ScrollView.FOCUS_DOWN);
            }
        });
    }

    // If the last item wasn't confirmed, we manually confirm it so it doesn't get lost
    private void addUnconfirmedIngredient(IngredientsCompletionView completionView) {
        String[] strings = completionView.getText().toString().split(",");
        if (! strings[strings.length - 1].trim().equals("")) {
            completionView.append(",");
        }
    }

    private IngredientsCompletionView setIngredientsAutocompleteAdapter(View newCourseView) {
        List<Ingredient> ingredients = new Select().from(Ingredient.class).execute();
        ArrayAdapter<Ingredient> ingrAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, ingredients);
        IngredientsCompletionView completionView = ButterKnife.findById(newCourseView, R.id.ingredients_edit_text);
        completionView.allowDuplicates(false);
        completionView.setThreshold(1);
        completionView.setAdapter(ingrAdapter);
//        completionView.performBestGuess(false); //Turn off making a best guess when converting text into a token (allows free entry)

        return completionView;
    }

    private void createCourse(String name, List<Object> ingredients) {
        ActiveAndroid.beginTransaction();
        try {
            Course c = new Course();
            c.name = name;
            c.save();

            courseList.add(c); //TODO: insert in order

            ((ArrayAdapter) getListAdapter()).notifyDataSetChanged();

            for (Object ingrObj : ingredients) {
                Ingredient ingr = (Ingredient) ingrObj;
                if (ingr.getId() == null) ingr.save();
                CourseIngredient ci = new CourseIngredient(c, ingr);
                ci.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    @DebugLog
    private void deleteSelectedItems(final SparseBooleanArray checkedItemPositions) {
        final DialogInterface.OnClickListener dialogClickListener =
                new DeleteBtnClickListener(checkedItemPositions, (CourseAdapter) getListAdapter());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.dialog_delete_confirmation))
                .setPositiveButton(android.R.string.yes, dialogClickListener)
                .setNegativeButton(android.R.string.no, dialogClickListener)
                .show();
    }

    private static class DeleteBtnClickListener implements DialogInterface.OnClickListener {

        private SparseBooleanArray checkedItemPositions;
        private CourseAdapter listAdapter;

        public DeleteBtnClickListener(SparseBooleanArray checkedItemPositions, CourseAdapter listAdapter) {
            this.listAdapter = listAdapter;
            this.checkedItemPositions = checkedItemPositions.clone();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    Log.d(LOG_TAG, "checkedItemPositions: " + checkedItemPositions);
                    // Inverse order to avoid possible IndexOutOfBoundsException after remove.
                    for (int i = checkedItemPositions.size() - 1; i >= 0; i --) {
                        if (checkedItemPositions.valueAt(i)) {
                            Course deletedCourse = listAdapter.getItem(checkedItemPositions.keyAt(i));
                            Log.d(LOG_TAG, "deletedCourse: " + deletedCourse);
                            listAdapter.remove(deletedCourse);
                            deletedCourse.delete();
                        }
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }

    private void editSelectedItem(SparseBooleanArray checkedItemPositions) {
        final Course c = (Course) getListAdapter().getItem(
                checkedItemPositions.keyAt(checkedItemPositions.indexOfValue(true)));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View editCourseView = LayoutInflater.from(getActivity()).inflate(R.layout.course_view, null);
        ButterKnife.inject(this, editCourseView);

        // Auto-complete for Ingredients
        IngredientsCompletionView completionView = setIngredientsAutocompleteAdapter(editCourseView);

        for (CourseIngredient rel : c.getIngredients()) {
            completionView.addObject(rel.ingredient);
        }

        EditText nameET = ButterKnife.findById(editCourseView, R.id.name_edit_text);
        nameET.setText(c.name);

        final AlertDialog d =
                builder.setTitle(getString(R.string.dialog_edit_course_title)).setView(editCourseView)
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create();
        d.show();
        Button positiveButton = d.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameET = ButterKnife.findById(d, R.id.name_edit_text);

                if (nameET.getText().length() == 0) {
                    Toast.makeText(getActivity(), "El nombre no puede estar vacío", Toast.LENGTH_LONG).show();
                } else {
                    IngredientsCompletionView completionView = ButterKnife.findById(d, R.id.ingredients_edit_text);
                    //addUnconfirmedIngredient(completionView);

                    RadioGroup courseTypeRG = ButterKnife.findById(d, R.id.course_type_radio); //TODO

                    updateCourse(c, nameET.getText().toString(), completionView.getObjects());
                }
            }
        });
    }

    private void updateCourse(Course c, String newName, List<Object> ingredients) {
        ActiveAndroid.beginTransaction();
        try {
            c.name = newName;

            c.removeAllIngredients();
            for (Object ingrObj : ingredients) {
                Ingredient ingr = (Ingredient) ingrObj;
                if (ingr.getId() == null) ingr.save();
                CourseIngredient ci = new CourseIngredient();
                ci.ingredient = ingr;
                ci.course = c;
                ci.save();
            }
            c.save();
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }
}
