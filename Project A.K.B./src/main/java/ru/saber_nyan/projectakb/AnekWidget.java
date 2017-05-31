/*******************************************************************************
 * The Unlicense 2017.
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to < http://unlicense.org >
 ******************************************************************************/

package ru.saber_nyan.projectakb;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import static ru.saber_nyan.projectakb.MainActivity.BROADCAST_ERROR_TAG;
import static ru.saber_nyan.projectakb.MainActivity.BROADCAST_TAG;
import static ru.saber_nyan.projectakb.MainActivity.BROADCAST_TEXT_TAG;

public class AnekWidget extends AppWidgetProvider {

	public final static String EXTRA_ANEK = "EXTRA_ANEK";
	public final static String TAG = AnekWidget.class.getSimpleName();
	public final static String INTENT_UPDATE_PRESSED_ACTION = "plsUpdate";
	public final static String INTENT_MORE_PRESSED_ACTION = "plsMoar";
	public String anek;
	public Boolean isError = false;

	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "broadcast received!\n" +
					"\tContext: " + context.toString());
			isError = intent.getBooleanExtra(BROADCAST_ERROR_TAG, true);
			anek = intent.getStringExtra(BROADCAST_TEXT_TAG);
			updateNow(context);
		}
	};

	void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
						 int appWidgetId) {
		Log.i(TAG, "update widget #" + appWidgetId + "!");
		Log.i(TAG, "Service error? " + String.valueOf(isError) + "\n" +
				"\tText: " + anek);
		// Construct the RemoteViews object
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.anek_widget);

		Intent updateIntent = new Intent(context, AnekWidget.class);
		Intent moreIntent = new Intent(context, AnekWidget.class);
		updateIntent.setAction(INTENT_UPDATE_PRESSED_ACTION);
		moreIntent.setAction(INTENT_MORE_PRESSED_ACTION);
		PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, 0);
		PendingIntent morePendingIntent = PendingIntent.getBroadcast(context, 0, moreIntent, 0);
		views.setOnClickPendingIntent(R.id.appwidget_textView_anek, updatePendingIntent);
		views.setOnClickPendingIntent(R.id.button_more, morePendingIntent);
		views.setTextViewText(R.id.appwidget_textView_anek, anek);

		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	public void updateNow(Context context) {
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		this.onUpdate(context, widgetManager,
				widgetManager.getAppWidgetIds(new ComponentName(context, AnekWidget.class)));
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		for (int appWidgetId : appWidgetIds) {
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	@Override
	public void onEnabled(Context context) {
		Log.v(TAG, "onEnabled called!\n" +
				"\tContext: " + context);
		LocalBroadcastManager.getInstance(context).registerReceiver(refreshReceiver,
				new IntentFilter(BROADCAST_TAG));
		anek = context.getString(R.string.widget_touchToUpdate);
		this.updateNow(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.d(TAG, "onReceive called!\n" +
				"\tAction: " + intent.getAction());
//		if (intent.getAction().equals(INTENT_UPDATE_PRESSED_ACTION)) {
//			context.startService(new Intent(context, RefreshService.class));
//		}
		switch (intent.getAction()) {
			case INTENT_UPDATE_PRESSED_ACTION:
				context.startService(new Intent(context, RefreshService.class));
				break;

			case INTENT_MORE_PRESSED_ACTION:
				Intent dialogIntent = new Intent(context, WidgetDialogActivity.class);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				dialogIntent.putExtra(EXTRA_ANEK, anek);
				context.startActivity(dialogIntent);
				break;
		}
	}

	@Override
	public void onDisabled(Context context) {
		LocalBroadcastManager.getInstance(context).unregisterReceiver(refreshReceiver);
	}
}

