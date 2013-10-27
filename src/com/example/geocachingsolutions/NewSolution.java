package com.example.geocachingsolutions;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class NewSolution extends Activity {

	private String gcid;
	private Button addphoto;
	private Button post;
	private TextView heading;
	private EditText wordbox;
	private ProgressBar progress;
	
	private LocationManager locmgr = null;
	private String provider;
	private static final int CAMERA_REQUEST = 1888;
	
	private String image64;
	private Location lastLocation;
	private String m_latitude;
	private String m_longitude;
	private String address;
	private String portnumber;
	private String htmlString;
	private String light;
	private String words;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_solution);
		
		wordbox = (EditText)findViewById(R.id.editText1);
		progress = (ProgressBar)findViewById(R.id.progressBar1);
		progress.setVisibility(View.INVISIBLE);
		
		//get value from textEdit in mainActivity
		Bundle extras = getIntent().getExtras();
		gcid = extras.getString("gcid");
		address = extras.getString("address");
		portnumber = extras.getString("portnumber");
		//change heading message 
		String headingMessage = "New Solution for " + gcid;
		heading = (TextView)findViewById(R.id.textView1);
		heading.setText(headingMessage);
		//set address and port number
		address = "10.0.0.7";
		portnumber = "8888";
		
		//set buttons
		addphoto = (Button) findViewById(R.id.button1);
		post = (Button) findViewById(R.id.button4);
		
		//start location service
		locmgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = locmgr.getBestProvider(criteria, false);
		locmgr.requestLocationUpdates(provider,  0,  0,  locationListener);
		lastLocation = locmgr.getLastKnownLocation(provider);
		
		//start light sensor
		SensorManager sensemgr = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		Sensor lightSensor = sensemgr.getDefaultSensor(Sensor.TYPE_LIGHT);
		sensemgr.registerListener(lightSensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		//default values for image 
		image64 = "did not take an image";
				
				
		//click listener for addphoto
		addphoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
				
			}
		});
		
		//click listener for post
		post.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				progress.setVisibility(View.VISIBLE);
				words = wordbox.getText().toString();
				PostData ast1 = new PostData();
				ast1.execute();
			}
		});
	} // end onCreate *****************************************
	
	//Called when the camera activities respond when finished
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//camera 
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			Bitmap photo = (Bitmap) data.getExtras().get("data");
		    ByteArrayOutputStream stream = new ByteArrayOutputStream();
		    photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		    byte[] b = stream.toByteArray();
		    image64 = Base64.encodeToString(b, Base64.DEFAULT);
		    //ttest.setText(image64);
		    Toast.makeText(NewSolution.this, "Image captured", Toast.LENGTH_LONG).show();
		}	

	}
	
	//used for light
	SensorEventListener lightSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_LIGHT){
				float currentReading = event.values[0];
				light = Float.toString(currentReading);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
	};

	//Used to listen for updates from the GPS
	LocationListener locationListener = new LocationListener() {
		private int i = 0;
		public void onLocationChanged(Location location) {
			lastLocation = location;
			if (location != null) {
				m_latitude = String.valueOf(location.getLatitude());
				m_longitude = String.valueOf(location.getLongitude());
			} 
		}
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};
	
	//Used to send data to server when post button is pressed
	class PostData extends AsyncTask<Long, Void, String> {
		private Exception exception_;
		private String url_ = "http://" + address + ":" + portnumber + "/post";
		JSONObject json = new JSONObject();
		private HttpResponse response_;
		
		@Override
		protected void onPreExecute() {
			progress.setMax(100);
		}
		
		@Override
		protected String doInBackground(Long... params) {
			try {
				formatJson();
				HttpClient client = new DefaultHttpClient();	
				progress.setProgress(20);
				HttpPost request = new HttpPost(url_);
				StringEntity se = new StringEntity(json.toString());
				se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				request.setEntity(se);
				progress.setProgress(40);
				response_ = client.execute(request);
				progress.setProgress(80);
			} catch (Exception e) {
				exception_ = e;				
			}
			HttpEntity entity = response_.getEntity();
			try {
				htmlString = EntityUtils.toString(entity);				
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
			return htmlString;
		}
		
		protected void onProgressUpdate(Integer... values) {
		}
		
		public void onPostExecute(String htmlString) {
			progress.setProgress(100);
			updateUI();
		}
				
		//formats the JSON object to be sent to server
		private void formatJson () throws JSONException {
			json.put("gcid", gcid);
			json.put("image", image64);
			json.put("light",  light);
			json.put("words", words);
			json.put("latitude", m_latitude);
			json.put("longitude", m_longitude);
		}
				
	}
	
	public void updateUI() {
		progress.setVisibility(View.INVISIBLE);
		Toast.makeText(NewSolution.this, "Solution posted", Toast.LENGTH_LONG).show();
		//ADD TOAST FOR SUCCESS OR FAIL
	}
	
	
	//*************************************************************
	
	@Override
	protected void onPause() {
		super.onDestroy();
		locmgr.removeUpdates(locationListener);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		locmgr.requestLocationUpdates(provider,  0,  0,  locationListener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_solution, menu);
		return true;
	}
	
	

}
