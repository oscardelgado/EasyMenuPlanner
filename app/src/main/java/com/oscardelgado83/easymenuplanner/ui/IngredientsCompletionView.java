package com.oscardelgado83.easymenuplanner.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Ingredient;
import com.tokenautocomplete.TokenCompleteTextView;

/**
 * Created by oscar on 4/04/15.
 */
public class IngredientsCompletionView extends TokenCompleteTextView {

    public IngredientsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(Object object) {
        Ingredient ingr = (Ingredient) object;

        LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout)l.inflate(R.layout.ingredient_token, (ViewGroup) IngredientsCompletionView.this.getParent(), false);
        ((TextView)view.findViewById(R.id.name)).setText(ingr.name);

        return view;
    }

    @Override
    protected Object defaultObject(String completionText) {
        Ingredient ingr = new Ingredient();
        ingr.name = completionText;
        return ingr;
    }
}
