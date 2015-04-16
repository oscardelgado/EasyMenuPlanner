package com.oscardelgado83.easymenuplanner.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Ingredient;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by oscar on 15/04/15.
 */
public class ShoppingListAdapter extends ArrayAdapter<Ingredient> {

    public ShoppingListAdapter(Context context, List<Ingredient> ingredientList) {
        super(context, 0, ingredientList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Ingredient ingredient = getItem(position);

        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_ingredient, parent, false);
            holder = new ViewHolder(convertView, ingredient);
            convertView.setTag(holder);
        }

        // Populate the data into the template view using the data object
        holder.ingredientName.setText(ingredient.name);

        holder.ingredientChecked.setChecked(ingredient.checked);

        // Return the completed view to render on screen
        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.ingredient_name)
        TextView ingredientName;

        @InjectView(R.id.ingredient_chk)
        CheckBox ingredientChecked;

        public ViewHolder(View view, final Ingredient ingredient) {
            ButterKnife.inject(this, view);
            ingredientChecked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    ingredient.checked = ingredientChecked.isChecked();
                    ingredient.save();
                }
            });
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
