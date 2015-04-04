package com.oscardelgado83.easymenuplanner.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by oscar on 2/04/15.
 */
public class CourseAdapter extends ArrayAdapter<Course> {

    public CourseAdapter(Context context, List<Course> courses) {
        super(context, 0, courses);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_course, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        // Get the data item for this position
        Course course = getItem(position);

        // Populate the data into the template view using the data object
        holder.courseName.setText(course.name);

        // Return the completed view to render on screen
        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.course_name)
        TextView courseName;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        Course course = getItem(position);
        return course.getId();
    }
}
