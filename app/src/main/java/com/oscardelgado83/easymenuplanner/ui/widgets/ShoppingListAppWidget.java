package com.oscardelgado83.easymenuplanner.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import com.oscardelgado83.easymenuplanner.R;
import com.oscardelgado83.easymenuplanner.ui.MainActivity;

public class ShoppingListAppWidget extends AppWidgetProvider {

    public static final String EXTRA_ITEM = "ShoppingListAppWidget_extra_item";

    @Override
    public void onUpdate(Context ctxt, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {

            RemoteViews widget = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                Intent svcIntent = new Intent(ctxt, ShoppingListWidgetService.class);

                svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
                svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
                widget = new RemoteViews(ctxt.getPackageName(),
                        R.layout.shopping_list_widget_layout);

                Intent clickIntent = new Intent(ctxt, MainActivity.class);

                PendingIntent clickPI = PendingIntent
                        .getActivity(ctxt, 0,
                                clickIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                widget.setPendingIntentTemplate(R.id.list_view, clickPI);

                widget.setOnClickPendingIntent(R.id.shopping_list_linear_layout, clickPI);

                widget.setRemoteAdapter(appWidgetIds[i], R.id.list_view, svcIntent);

                widget.setEmptyView(R.id.list_view, R.id.empty_view);
            } else {
                widget = new RemoteViews(ctxt.getPackageName(),
                        R.layout.shopping_list_widget_layout_old_api);
            }
            appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
        }

        super.onUpdate(ctxt, appWidgetManager, appWidgetIds);
    }
}