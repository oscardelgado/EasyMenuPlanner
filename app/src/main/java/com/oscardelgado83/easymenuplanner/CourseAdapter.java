package com.oscardelgado83.easymenuplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.oscardelgado83.easymenuplanner.model.Course;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by oscar on 2/04/15.
 */
public class CourseAdapter extends ArrayAdapter<Course> {

    @InjectView(R.id.course_name)
    TextView courseName;

    public CourseAdapter(Context context, List<Course> courses) {
        super(context, 0, courses);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Course course = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_course, parent, false);
        }

        ButterKnife.inject(this, convertView);

        // Populate the data into the template view using the data object
        courseName.setText(course.name);

        // Return the completed view to render on screen
        return convertView;
    }
}
