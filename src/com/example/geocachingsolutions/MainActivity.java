package com.example.geocachingsolutions;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Button addsolution;
	private Button search;
	private Button settings;
	private EditText id;
	private static final int RESULT_SETTINGS = 33;
	private String address;
	private String port;
	private TextView taddress;
	private TextView tport;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final Context context = this; 
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//set the buttons and textEdit
		taddress = (TextView) findViewById(R.id.textView2);
		tport = (TextView) findViewById(R.id.textView3);
		addsolution = (Button) findViewById(R.id.button2);
		search = (Button) findViewById(R.id.button1);
		id = (EditText) findViewById(R.id.editText1);
		
		
		//listen for 'add solution' button to be pressed
		addsolution.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String gcid = id.getText().toString();
				Intent toNewSolution = new Intent(context, NewSolution.class);
				toNewSolution.putExtra("gcid", gcid);
				toNewSolution.putExtra("address", address);
				toNewSolution.putExtra("port", port);
				startActivity(toNewSolution);
			}
		});
		
		//listen for 'search' button to be pressed
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String gcid = id.getText().toString();
				Intent toSolution = new Intent(context, Solution.class);
				toSolution.putExtra("gcid", gcid);
				toSolution.putExtra("address", address);
				toSolution.putExtra("port", port);
				startActivity(toSolution);
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.preferences:
			Intent i = new Intent(MainActivity.this, PrefActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			break;
		}
		
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case RESULT_SETTINGS:
			updateUserSettings();
			break;
		}
	}
	
	private void updateUserSettings() {
		SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
		address = sharedPrefs.getString("address", "NULL");
		port = sharedPrefs.getString("port", "NULL");
		taddress.setText("Address: " + address);
		tport.setText("Port: " + port);
		
	}

}
