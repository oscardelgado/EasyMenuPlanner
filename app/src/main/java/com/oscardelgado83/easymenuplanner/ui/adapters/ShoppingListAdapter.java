package com.oscardelgado83.easymenuplanner.ui.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Course;
import com.oscardelgado83.easymenuplanner.model.CourseIngredient;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

import static com.oscardelgado83.easymenuplanner.util.Cons.DEBUGGING;

/**
 * Created by oscar on 15/04/15.
 */
public class ShoppingListAdapter extends ArrayAdapter<Ingredient> {

    private static final String LOG_TAG = ShoppingListAdapter.class.getSimpleName();

    MainActivity context;
    private final int weekdayIndexWithCurrentOrder;

    public ShoppingListAdapter(Context context, List<Ingredient> ingredientList) {
        super(context, 0, ingredientList);
        this.context = (MainActivity) context;

        int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Sunday is 1, Saturday is 7.
        int firstDay = Calendar.getInstance().getFirstDayOfWeek();
        weekdayIndexWithCurrentOrder = (currentDayOfWeek - firstDay + 7) % 7;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        final Ingredient ingredient = getItem(position);

        final ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_ingredient, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        // Populate the data into the template view using the data object
        holder.ingredientName.setText(ingredient.name);

        holder.ingredientChecked.setChecked(ingredient.checked);

        if (ingredient.checked) {
            holder.ingredientName.setEnabled(false);
            holder.ingredientName.setPaintFlags(holder.ingredientName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.ingredientName.setEnabled(true);
            holder.ingredientName.setPaintFlags(holder.ingredientName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        List<Course> courses = new Select().from(Course.class)
                .innerJoin(CourseIngredient.class).on("CourseIngredients.course = Courses.Id")
                .and("CourseIngredients.ingredient = ?", ingredient.getId())
                .innerJoin(Day.class).on("Days.firstCourse = Courses.Id OR Days.secondCourse = Courses.Id")
                .where("Days.Id > ?", weekdayIndexWithCurrentOrder)
                .execute();

        List<String> coursesStrings = new LinkedList<>();
        for (Course course : courses) {
            List<Day> futureDays = new Select().from(Day.class)
                    .where("(Days.firstCourse = ? OR Days.secondCourse = ?)", course.getId(), course.getId())
                    .and("Days.Id > ?", weekdayIndexWithCurrentOrder + 1)
                    .execute();

            boolean inCurrentDay = new Select().from(Day.class)
                    .where("(Days.firstCourse = ? OR Days.secondCourse = ?)", course.getId(), course.getId())
                    .and("Days.Id = ?", weekdayIndexWithCurrentOrder + 1)
                    .exists();

            List<String> daysStrings = new LinkedList<>();
            if ( ! futureDays.isEmpty()) {
                daysStrings.add(context.getString(R.string.shoppinglist_ingredient_on_days) + " "
                        + StringUtils.join(StringUtils.join(futureDays, ", ")));
            }
            if (inCurrentDay) daysStrings.add("<b>Today</b>");

            coursesStrings.add(course.name + ", " + StringUtils.join(daysStrings, ", "));
        }

        holder.course.setText(Html.fromHtml(StringUtils.join(coursesStrings, "\n")));

        convertView.setOnClickListener(new View.OnClickListener() {
            @DebugLog
            @Override
            public void onClick(View v) {
                ingredient.checked = !ingredient.checked;
                ingredient.save();
                if (DEBUGGING) Log.d(LOG_TAG, "Ingredient saved: " + ingredient);
                holder.ingredientChecked.setChecked(ingredient.checked);

                if (ingredient.checked) {
                    holder.ingredientName.setEnabled(false);
                    holder.ingredientName.setPaintFlags(holder.ingredientName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    holder.ingredientName.setEnabled(true);
                    holder.ingredientName.setPaintFlags(holder.ingredientName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }

                context.refreshShoppinglistMenu();
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.ingredient_name)
        TextView ingredientName;

        @Bind(R.id.ingredient_chk)
        CheckBox ingredientChecked;

        @Bind(R.id.course)
        TextView course;

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
        Ingredient ingredient = getItem(position);
        return ingredient.getId();
    }
}
