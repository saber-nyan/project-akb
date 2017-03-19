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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static ru.saber_nyan.projectakb.RefreshService.URL_API_DEFAULT;

public class SendActivity extends AppCompatActivity implements View.OnClickListener {

	public static final String TAG = SendActivity.class.getSimpleName();
	public static final int SPINNER_ID_CHARACTER = 0;
	public static final int SPINNER_ID_OBJECT = 1;
	public static final int SPINNER_ID_STORY = 2;
	public Spinner spinner_mode;
	public TextView textView_mode_desc;
	public EditText editText_story;
	private String BROADCAST_TAG = "send done";

	private BroadcastReceiver sendReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			editText_story.setText("");
			Toast.makeText(SendActivity.this, R.string.SendActivity_sent, Toast.LENGTH_LONG).show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send);
		editText_story = (EditText) findViewById(R.id.editText_story);
		textView_mode_desc = (TextView) findViewById(R.id.textView_description);
		spinner_mode = (Spinner) findViewById(R.id.spinner_mode);
		spinner_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch ((int) id) {
					case SPINNER_ID_CHARACTER:
						textView_mode_desc.setText(R.string.SendLayout_textView_description_character);
						break;
					case SPINNER_ID_OBJECT:
						textView_mode_desc.setText(R.string.SendLayout_textView_description_object);
						break;
					case SPINNER_ID_STORY:
						textView_mode_desc.setText(R.string.SendLayout_textView_description_story);
						break;
					default:
						Log.e(TAG, id + " not implemented!");
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.v(TAG, "nothing selected...");
			}
		});
		findViewById(R.id.button_send).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(sendReceiver,
				new IntentFilter(BROADCAST_TAG));
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(sendReceiver);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_send:
				send(editText_story.getText().toString(), spinner_mode.getSelectedItemPosition());
				break;
			default:
				Log.w(TAG, v.getId() + " not yet implemented");
				break;
		}
	}

	private void send(String text, int mode) {
		if (text.equals("")) {
			Toast.makeText(this, R.string.SendActivity_noText, Toast.LENGTH_LONG).show();
			return;
		}
		SendTask task = new SendTask();
		String id = "android_" + Build.BOARD.length() + Build.BRAND.length() +
				Build.DEVICE.length() + Build.DISPLAY.length() + Build.HOST.length() +
				Build.ID.length() + Build.MANUFACTURER.length() + Build.MODEL.length() +
				Build.PRODUCT.length() + Build.TAGS.length() + Build.TYPE.length() +
				Build.USER.length();
		String URL_API_PUSH = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(getString(R.string.prefKey_API_URL), URL_API_DEFAULT) + "/push";
		Log.i(TAG, "Sending...\n" +
				"\tUUID: " + id + "\n" +
				"\ttext: " + text + "\n" +
				"\tmode: " + mode + "\n" +
				"\tAPI: " + URL_API_PUSH);
		task.execute(id, text, String.valueOf(mode), URL_API_PUSH);
	}

	private class SendTask extends AsyncTask<String, Void, Object> {

		private static final String JSON_KEY_STATUS = "status";
		private final String TAG = SendTask.class.getSimpleName();
		private ProgressDialog progressDialog;

		@SuppressWarnings("ConstantConditions")
		private void doKeepDialog(Dialog dialog) {
			try {
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				lp.copyFrom(dialog.getWindow().getAttributes());
				lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
				lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
				dialog.getWindow().setAttributes(lp);
			} catch (Exception e) {
				Log.e(TAG, "doKeep failed!", e);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(SendActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage(getString(R.string.SendActivity_sending));
			progressDialog.show();
			doKeepDialog(progressDialog);
		}

		/**
		 * Sends data to server.
		 *
		 * @param params first:		android UUID
		 *               second:	text
		 *               third:		mode
		 *               fourth:	API URL
		 * @return none
		 */
		@Override
		protected Object doInBackground(String... params) {
			String uuid = params[0];
			String text = params[1];
			int mode = Integer.parseInt(params[2]);
			String API_URL = params[3];

			Map<String, String> paramsMap = new HashMap<>();
			paramsMap.put("author", uuid);
			paramsMap.put("value", text);

			try {
				URL url;
				switch (mode) {
					case SPINNER_ID_CHARACTER:
						url = new URL(API_URL + "/characters");
						break;
					case SPINNER_ID_OBJECT:
						url = new URL(API_URL + "/objects");
						break;
					case SPINNER_ID_STORY:
						url = new URL(API_URL + "/stories");
						break;
					default:
						Log.e(TAG, mode + " is not yet implemented");
						throw new IllegalArgumentException(mode + "is not yet implemented!");
				}
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoInput(true);
				connection.setDoOutput(true);

				OutputStream os = connection.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(getPostDataString(paramsMap));
				writer.close();
				os.close();
				int responseCode = connection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					return readStream(connection.getInputStream());
				} else {
//					reportError("HTTP Error " + responseCode, SendActivity.this);
					return responseCode;
				}
			} catch (Exception e) {
				//reportError(e);
				return e;
			}
		}

		@Override
		protected void onPostExecute(Object s) {
			super.onPostExecute(s);
			progressDialog.hide();
			Log.i(TAG, "in result " + s);
			try {
				JSONObject jsonObject = new JSONObject((String) s);
				String statusStr = jsonObject.getString(JSON_KEY_STATUS);
				Log.i(TAG, "we got status " + statusStr);
				if (!statusStr.equals("Ok")) {
					reportError(statusStr, SendActivity.this);
				} else {
					LocalBroadcastManager.getInstance(SendActivity.this)
							.sendBroadcast(new Intent(BROADCAST_TAG));
				}
			} catch (Exception e) {
				try {
					//noinspection ConstantConditions
					reportError((Exception) s, SendActivity.this);
				} catch (ClassCastException ex) {
					reportError("HTTP Error " + s, SendActivity.this);
				}
			}
		}

		@SuppressWarnings("deprecation")
		private void reportError(Exception e, Context context) {
			Log.e(TAG, "Error occurred!", e);
			AlertDialog dialog = new AlertDialog.Builder(context)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(Html.fromHtml(String.format(getString(R.string.SendActivity_error),
							e.getLocalizedMessage())))
					.setTitle(R.string.SendActivity_error_title)
					.setPositiveButton(android.R.string.ok, null)
					.create();
			dialog.show();
			doKeepDialog(dialog);
		}

		@SuppressWarnings("deprecation")
		private void reportError(String message, Context context) {
			Log.e(TAG, "Error occurred!\n" + message);
			AlertDialog dialog = new AlertDialog.Builder(context)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(Html.fromHtml(String.format(getString(R.string.SendActivity_error),
							message)))
					.setTitle(R.string.SendActivity_error_title)
					.setPositiveButton(android.R.string.ok, null)
					.create();
			dialog.show();
			doKeepDialog(dialog);
		}

		private String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
			StringBuilder result = new StringBuilder();
			boolean first = true;
			for (Map.Entry<String, String> entry : params.entrySet()) {
				if (first) {
					first = false;
				} else {
					result.append("&");
				}
				result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				result.append("=");
				result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			}
			return result.toString();
		}

		private String readStream(InputStream is) throws IOException {
			StringBuilder sb = new StringBuilder();
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			for (String line = r.readLine(); line != null; line = r.readLine()) {
				sb.append(line);
			}
			is.close();
			return sb.toString();
		}
	}

}
