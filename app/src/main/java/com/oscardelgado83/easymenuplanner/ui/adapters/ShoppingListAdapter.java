package com.oscardelgado83.easymenuplanner.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;

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

    public ShoppingListAdapter(Context context, List<Ingredient> ingredientList) {
        super(context, 0, ingredientList);
        this.context = (MainActivity) context;
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

        convertView.setOnClickListener(new View.OnClickListener() {
            @DebugLog
            @Override
            public void onClick(View v) {
                ingredient.checked = ! ingredient.checked;
                ingredient.save();
                if (DEBUGGING) Log.d(LOG_TAG, "Ingredient saved: " + ingredient);
                holder.ingredientChecked.setChecked(ingredient.checked);
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
