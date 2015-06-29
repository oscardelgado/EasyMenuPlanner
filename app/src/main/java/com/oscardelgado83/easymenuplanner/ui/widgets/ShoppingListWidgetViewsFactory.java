package com.oscardelgado83.easymenuplanner.ui.widgets;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.Ingredient;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ShoppingListWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context ctxt = null;
    private int appWidgetId;
    private final List<Ingredient> items;

    @DebugLog
    public ShoppingListWidgetViewsFactory(Context ctxt, Intent intent) {
        this.ctxt = ctxt;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        items = new ArrayList<>();
    }

    private List<Ingredient> queryIngredients() {
        return new Select().from(Ingredient.class)
                .where("Id IN (SELECT CI.ingredient FROM CourseIngredients CI, Days D " +
                        "WHERE CI.course = D.firstCourse OR CI.course = D.secondCourse)")
                .orderBy("checked ASC, UPPER (name) ASC")
                .execute();
    }

    @DebugLog
    @Override
    public void onCreate() {
        // no-op
    }

    @DebugLog
    @Override
    public void onDestroy() {
        // no-op
    }

    @DebugLog
    @Override
    public int getCount() {
        return (items.size());
    }

    @DebugLog
    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(ctxt.getPackageName(),
                R.layout.shopping_list_widget_row_layout);

        if (items.get(position).checked) {
            row.setViewVisibility(R.id.checked, View.VISIBLE);
            row.setViewVisibility(R.id.unchecked, View.GONE);
        } else {
            row.setViewVisibility(R.id.unchecked, View.VISIBLE);
            row.setViewVisibility(R.id.checked, View.GONE);
        }
        row.setTextViewText(android.R.id.text1, items.get(position).name);

        Intent i = new Intent();
        row.setOnClickFillInIntent(android.R.id.text1, i);

        return (row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return (null);
    }

    @Override
    public int getViewTypeCount() {
        return (1);
    }

    @Override
    public long getItemId(int position) {
        return (position);
    }

    @Override
    public boolean hasStableIds() {
        return (true);
    }

    @DebugLog
    @Override
    public void onDataSetChanged() {
        items.clear();
        items.addAll(queryIngredients());
    }
}