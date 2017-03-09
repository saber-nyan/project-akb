package ru.saber_nyan.projectakb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	public static final String TAG = MainActivity.class.getSimpleName();
	public static final String BROADCAST_TAG = "service result";
	public static final String BROADCAST_ERROR_TAG = "isError";
	public static final String BROADCAST_TEXT_TAG = "text";
	public static final int ID_MENU_SETTINGS = 0;
	public static final int ID_MENU_QUIT = 1;
	public TextView textView_anek;
	@SuppressWarnings("deprecation")
	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "broadcast received!\n" +
					"\tContext: " + context.toString());
			boolean isError = intent.getBooleanExtra(BROADCAST_ERROR_TAG, true);
			String text = intent.getStringExtra(BROADCAST_TEXT_TAG);
			if (isError) {
				switch (text) {
					case "DefectiveDatabase":
						textView_anek.setText(Html.fromHtml(
								getString(R.string.MainActivity_db_error)));
						break;
					case "TempDisabled":
						textView_anek.setText(Html.fromHtml(
								getString(R.string.MainActivity_temp_disabled_error)));
						break;
					default:
						textView_anek.setText(Html.fromHtml(
								String.format(getString(R.string.MainActivity_other_error), text)));
						break;
				}
			} else {
				textView_anek.setText(text);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.v(TAG, "onCreate called");

		textView_anek = (TextView) findViewById(R.id.textView_anek);
		findViewById(R.id.button_refresh).setOnClickListener(this);
		findViewById(R.id.button_add).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ID_MENU_SETTINGS, 0, R.string.prefTitle_settings);
		menu.add(0, ID_MENU_QUIT, 0, R.string.MainActivity_quit);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case ID_MENU_SETTINGS:
				Log.v(TAG, "starting settings...");
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			case ID_MENU_QUIT:
				moveTaskToBack(true);
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(0);
				break;
			default:
				Log.w(TAG, item.getTitle() + " not yet implemented");
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
		textView_anek.setText(R.string.MainActivity_refreshing);
		startService(new Intent(this, RefreshService.class));
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume called");
		LocalBroadcastManager.getInstance(this).registerReceiver(refreshReceiver,
				new IntentFilter(BROADCAST_TAG));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_refresh:
				Log.d(TAG, "refresh clicked");
				textView_anek.setText(R.string.MainActivity_refreshing);
				startService(new Intent(this, RefreshService.class));
				break;
			case R.id.button_add:
				Log.d(TAG, "add clicked");
				startActivity(new Intent(this, SendActivity.class));
				break;
			default:
				Log.w(TAG, v.getId() + " not yet implemented");
				break;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshReceiver);
	}
}
