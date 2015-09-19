package com.oscardelgado83.easymenuplanner.ui.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by oscar on 2/04/15.
 */
public class CourseAdapter extends ArrayAdapter<Course> implements SectionIndexer{

    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;
    private Context context;

    public CourseAdapter(Context context, List<Course> courses) {
        super(context, 0, courses);
        this.context = context;
        alphaIndexer = new HashMap<>();
        for (int i = 0; i < courses.size(); i++)
        {
            String s = courses.get(i).name.substring(0, 1).toUpperCase();
            if (!alphaIndexer.containsKey(s))
                alphaIndexer.put(s, i);
        }

        Set<String> sectionLetters = alphaIndexer.keySet();
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
        Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        for (int i = 0; i < sectionList.size(); i++)
            sections[i] = sectionList.get(i);
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
        holder.courseNameTV.setText(course.name);
        holder.ingredientsTV.setText(StringUtils.join(course.getIngredients(), ", "));

        int weekdayIndexWithCurrentOrder = ((MainActivity) getContext()).getWeekdayIndexWithCurrentOrder();

//        List<Day> days = new Select().from(Day.class)
//                .where("(Days.firstCourse = ? OR Days.secondCourse = ? OR Days.dinner = ?)", course.getId(), course.getId(), course.getId())
//                .and("Days.Id = ? + 1", weekdayIndexWithCurrentOrder)
//                .execute();
//        if (days.isEmpty()) {
//            holder.daysTV.setText("");
//        } else {
//            holder.daysTV.setText(getContext().getString(R.string.assigned_to)
//                    + StringUtils.join(days, ", "));
//        }

        List<String> coursesStrings = new LinkedList<>();

        boolean inCurrentDay = new Select().from(Day.class)
                .where("(Days.firstCourse = ? OR Days.secondCourse = ? OR Days.dinner = ?)", course.getId(), course.getId(), course.getId())
                .and("Days.Id = ? + 1", weekdayIndexWithCurrentOrder)
                .exists();

        boolean tomorrow = new Select().from(Day.class)
                .where("(Days.firstCourse = ? OR  Days.secondCourse = ? OR Days.dinner = ?)", course.getId(), course.getId(), course.getId())
                .and("Days.Id = ? + 2", weekdayIndexWithCurrentOrder)
                .exists();

        List<Day> futureDays = new Select().from(Day.class)
                .where("(Days.firstCourse = ? OR  Days.secondCourse = ? OR Days.dinner = ?)", course.getId(), course.getId(), course.getId())
                .and("Days.Id > ? + 2", weekdayIndexWithCurrentOrder)
                .execute();


        if (inCurrentDay) {
            coursesStrings.add("<b><font color='#3B5A01'>" + context.getString(R.string.today) + "</font></b>");
        } if (tomorrow) {
            coursesStrings.add(context.getString(R.string.tomorrow));
        } else if ( ! futureDays.isEmpty()) {
            String daysString = context.getString(R.string.shoppinglist_ingredient_on_days) + " "
                    + StringUtils.join(StringUtils.join(futureDays, ", "));
            coursesStrings.add(daysString);
        }

        holder.daysTV.setText(Html.fromHtml(StringUtils.join(coursesStrings, ", ")));

        // Return the completed view to render on screen
        return convertView;
    }

    /**
     * Returns an array of objects representing sections of the list. The
     * returned array and its contents should be non-null.
     * <p/>
     * The list view will call toString() on the objects to get the preview text
     * to display while scrolling. For example, an adapter may return an array
     * of Strings representing letters of the alphabet. Or, it may return an
     * array of objects whose toString() methods return their section titles.
     *
     * @return the array of section objects
     */
    @Override
    public Object[] getSections() {
        return sections;
    }

    /**
     * Given the index of a section within the array of section objects, returns
     * the starting position of that section within the adapter.
     * <p/>
     * If the section's starting position is outside of the adapter bounds, the
     * position must be clipped to fall within the size of the adapter.
     *
     * @param sectionIndex the index of the section within the array of section
     *                     objects
     * @return the starting position of that section within the adapter,
     * constrained to fall within the adapter bounds
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        return alphaIndexer.get(sections[sectionIndex]);
    }

    /**
     * Given a position within the adapter, returns the index of the
     * corresponding section within the array of section objects.
     * <p/>
     * If the section index is outside of the section array bounds, the index
     * must be clipped to fall within the size of the section array.
     * <p/>
     * For example, consider an indexer where the section at array index 0
     * starts at adapter position 100. Calling this method with position 10,
     * which is before the first section, must return index 0.
     *
     * @param position the position within the adapter for which to return the
     *                 corresponding section index
     * @return the index of the corresponding section within the array of
     * section objects, constrained to fall within the array bounds
     */
    @Override
    public int getSectionForPosition(int position) {
        for ( int i = sections.length - 1; i >= 0; i-- ) {
            if ( position >= alphaIndexer.get( sections[ i ] ) ) {
                return i;
            }
        }
        return 0;
    }

    static class ViewHolder {
        @Bind(R.id.course_name)
        TextView courseNameTV;

        @Bind(R.id.ingredients)
        TextView ingredientsTV;

        @Bind(R.id.days)
        TextView daysTV;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
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
