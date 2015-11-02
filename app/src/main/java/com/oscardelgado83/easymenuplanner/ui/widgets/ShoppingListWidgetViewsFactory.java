package com.oscardelgado83.easymenuplanner.ui.widgets;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.EMPApplication;
import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.model.CourseIngredient;
import com.oscardelgado83.easymenuplanner.model.Day;
import com.oscardelgado83.easymenuplanner.model.Ingredient;

import java.util.ArrayList;
import java.util.Calendar;
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

    @DebugLog
    private List<Ingredient> queryIngredients() {
        int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Sunday is 1, Saturday is 7.
        int firstDay = EMPApplication.USER_WEEK_START_DAY;
        int weekdayIndexWithCurrentOrder = (currentDayOfWeek - firstDay + 7) % 7;

        return new Select().from(Ingredient.class)
                .innerJoin(CourseIngredient.class).on("CourseIngredients.ingredient = Ingredients.Id")
                .innerJoin(Day.class).on("(CourseIngredients.course = Days.firstCourse OR CourseIngredients.course = Days.secondCourse OR CourseIngredients.course = Days.breakfast OR CourseIngredients.course = Days.dinner)")
                .where("(Days.Id + 7 - " + EMPApplication.USER_WEEK_START_DAY + ")%7 >= " + weekdayIndexWithCurrentOrder)//0-6 sunday==0 /D.Id 1-7
                .orderBy("checked ASC, Days.Id, UPPER (name) ASC")
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
            row.setViewVisibility(R.id.text_checked, View.VISIBLE);
            row.setViewVisibility(R.id.text_unchecked, View.GONE);
            row.setTextViewText(R.id.text_checked, items.get(position).name);
            row.setInt(R.id.text_checked, "setPaintFlags", Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        } else {
            row.setViewVisibility(R.id.unchecked, View.VISIBLE);
            row.setViewVisibility(R.id.checked, View.GONE);
            row.setViewVisibility(R.id.text_unchecked, View.VISIBLE);
            row.setViewVisibility(R.id.text_checked, View.GONE);
            row.setTextViewText(R.id.text_unchecked, items.get(position).name);
        }

        Intent i = new Intent();

        Bundle extras = new Bundle();
        extras.putInt(ShoppingListAppWidget.EXTRA_ITEM, position);
        i.putExtras(extras);

        row.setOnClickFillInIntent(R.id.row, i);

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