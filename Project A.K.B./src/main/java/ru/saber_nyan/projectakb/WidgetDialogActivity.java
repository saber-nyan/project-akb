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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import static ru.saber_nyan.projectakb.AnekWidget.PREF_KEY_ANEK;
import static ru.saber_nyan.projectakb.AnekWidget.PREF_NAME_ANEK;

public class WidgetDialogActivity extends AppCompatActivity {

	private TextView anekDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_widget_dialog);
		anekDialog = (TextView) findViewById(R.id.textView_anek_dialog);
	}

	@Override
	protected void onStart() {
		super.onStart();
//		String anekExtra = getIntent().getStringExtra(EXTRA_ANEK);
//		Log.i(TAG, "onStart called, string:\n\t" +
//				anekExtra);
		SharedPreferences prefs = getSharedPreferences(PREF_NAME_ANEK, MODE_PRIVATE);
		anekDialog.setText(prefs.getString(PREF_KEY_ANEK, getString(R.string.AnekWidget_error))); // FIX!ME: Строка приходит пустой...
		findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
