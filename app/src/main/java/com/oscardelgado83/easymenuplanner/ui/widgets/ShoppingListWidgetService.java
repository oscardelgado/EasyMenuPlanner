package com.oscardelgado83.easymenuplanner.ui.widgets;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ShoppingListWidgetService extends RemoteViewsService {
  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {
    return(new ShoppingListWidgetViewsFactory(this.getApplicationContext(),
                                 intent));
  }
}