package com.oscardelgado83.easymenuplanner.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.oscardelgado83.easymenuplanner.ui.IngredientsCompletionView;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;
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
                } else {
                    mode.setSubtitle(count + getString(R.string.cab_elements_selected));
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

        builder.setTitle(getString(R.string.dialog_new_course_title)).setView(newCourseView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = (Dialog) dialog;
                        EditText nameET = ButterKnife.findById(d, R.id.name_edit_text);
//                        EditText ingredientsET = ButterKnife.findById(d, R.id.ingredients_edit_text);
                        RadioGroup courseTypeRG = ButterKnife.findById(d, R.id.course_type_radio);

                        createCourse(nameET.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        List<Ingredient> ingredients = new Select().from(Ingredient.class).execute();
        ArrayAdapter<Ingredient> ingrAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, ingredients);
        IngredientsCompletionView completionView = ButterKnife.findById(newCourseView, R.id.ingredients_edit_text);
        completionView.setAdapter(ingrAdapter);

        builder.show();
    }

    private void createCourse(String name) {
        Course c = new Course();
        c.name = name;
        c.save();

        courseList.add(c); //TODO: insert in order

        ((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
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
}
