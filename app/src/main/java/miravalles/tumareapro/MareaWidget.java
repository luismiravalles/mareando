package miravalles.tumareapro;

import miravalles.tumareapro.R;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

public class MareaWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		ComponentName thisWidget = new ComponentName(context,MareaWidget.class);		 
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		
		for (int widgetId : allWidgetIds) {
		      RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
			          R.layout.widget_layout);
		      
		      
		      appWidgetManager.updateAppWidget(widgetId, remoteViews);		      
			  }
	}
}
