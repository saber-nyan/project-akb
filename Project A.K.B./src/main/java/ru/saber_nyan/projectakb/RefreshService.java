package ru.saber_nyan.projectakb;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RefreshService extends Service {

	public static final String TAG = RefreshService.class.getSimpleName();
	public static String URL_API_DEFAULT;

	public RefreshService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "service start");
		URL_API_DEFAULT = getString(R.string.prefDef_API_URL);
		String URL_API = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(getString(R.string.prefKey_API_URL), URL_API_DEFAULT);
		Log.d(TAG, "url: " + URL_API);
		DownloadTask task = new DownloadTask();
		task.execute(URL_API + "/pull");
		return super.onStartCommand(intent, flags, startId);
	}

	private void returnResult(boolean isError, String text) {
		Intent intent = new Intent(MainActivity.BROADCAST_TAG);
		intent.putExtra(MainActivity.BROADCAST_ERROR_TAG, isError);
		intent.putExtra(MainActivity.BROADCAST_TEXT_TAG, text);
		Log.i(TAG, "send broadcast:\n" +
				"\tisError = " + isError + "\n" +
				"\ttext = " + text);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		stopSelf();
	}

	private class DownloadTask extends AsyncTask<String, Void, Void> {

		private final String TAG = DownloadTask.class.getSimpleName();
		private final String JSON_KEY_STATUS = "status";
		private final String JSON_KEY_TEXT = "text";

		@Override
		protected Void doInBackground(String... params) {
			HttpURLConnection connection;
			try {
				connection = (HttpURLConnection) new URL(params[0]).openConnection();
			} catch (Exception e) {
				Log.e(TAG, "connection error:", e);
				returnResult(true, e.getLocalizedMessage());
				return null;
			}

			String jsonString;
			try {
				InputStream in = new BufferedInputStream(connection.getInputStream());
				jsonString = readStream(in);
			} catch (Exception e) {
				Log.e(TAG, "download error:", e);
				returnResult(true, e.getLocalizedMessage());
				return null;
			} finally {
				connection.disconnect();
			}

			// Parse JSON
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(jsonString);
				String statusStr = jsonObject.getString(JSON_KEY_STATUS);
				if (!statusStr.equals("Ok")) {
					Log.e(TAG, "server error:\n" +
							"\t" + statusStr);
					returnResult(true, statusStr);
					return null;
				}
				String textStr = jsonObject.getString(JSON_KEY_TEXT);
				returnResult(false, textStr);
			} catch (Exception e) {
				Log.e(TAG, "parse error:", e);
				returnResult(true, e.getLocalizedMessage());
				return null;
			}
			return null;
		}

		private String readStream(InputStream is) throws IOException {
			StringBuilder sb = new StringBuilder();
			BufferedReader r = new BufferedReader(new InputStreamReader(is), 1024);
			for (String line = r.readLine(); line != null; line = r.readLine()) {
				sb.append(line);
			}
			is.close();
			return sb.toString();
		}
	}
}
