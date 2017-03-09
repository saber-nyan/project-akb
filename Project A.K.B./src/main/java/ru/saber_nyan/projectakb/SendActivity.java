package ru.saber_nyan.projectakb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

public class SendActivity extends AppCompatActivity {

	public static final String TAG = SendActivity.class.getSimpleName();
	public Spinner spinner_mode;
	public TextView textView_mode_desc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send);
		textView_mode_desc = (TextView) findViewById(R.id.textView_description);
		spinner_mode = (Spinner) findViewById(R.id.spinner_mode);
		spinner_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch ((int) id) {
					case 0: // Char
						textView_mode_desc.setText(R.string.SendLayout_textView_description_character);
						break;
					case 1: // Obj
						textView_mode_desc.setText(R.string.SendLayout_textView_description_object);
						break;
					case 2: // Story
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
	}
}
