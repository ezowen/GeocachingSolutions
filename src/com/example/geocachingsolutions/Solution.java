package com.example.geocachingsolutions;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class Solution extends Activity {
	
	private String gcid;
	private TextView heading;
	private TextView coordstext;
	private TextView light;
	private TextView words;
	private String address;
	private String portnumber;
	private String htmlString;
	private JSONObject servResponse;
	private ImageView image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solution);
		
		servResponse = new JSONObject();
		image = (ImageView)findViewById(R.id.imageView1);
		words = (TextView)findViewById(R.id.textView4);
		light = (TextView)findViewById(R.id.textView3);

		
		//get value from textEdit in mainActivity
		Bundle extras = getIntent().getExtras();
		gcid = extras.getString("gcid");
		address = extras.getString("address");
		portnumber = extras.getString("portnumber");
		//change heading message 
		String headingMessage = "Solution for " + gcid;
		heading = (TextView)findViewById(R.id.textView1);
		heading.setText(headingMessage);
		//set address and port number
		address = "10.0.0.7";
		portnumber = "8888";
		
		coordstext = (TextView) findViewById(R.id.textView2);
		coordstext.setText("coords here");
				
		GetData ast1 = new GetData();
		ast1.execute();
		
	}
	
	//used to get data from server
	class GetData extends AsyncTask<Long, Void, String> {
		private Exception exception_;
		private String url_ = "http://" + address + ":" + portnumber + "/get";
		private HttpResponse response_;
		JSONObject json = new JSONObject();
		
		@Override
		protected String doInBackground(Long... params) {
			try {
				json.put("gcid", gcid);
				HttpClient client = new DefaultHttpClient();
				HttpPost request = new HttpPost(url_);
				StringEntity se = new StringEntity(json.toString());
				se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				request.setEntity(se);
				response_ = client.execute(request);
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
		
		public void onPostExecute(String htmlString) {
			try {
				updateUI();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateUI() throws JSONException {
		servResponse = new JSONObject(htmlString);
		String image64 = (String) servResponse.get("image");
		float lightnum = Float.parseFloat((String) servResponse.get("light"));
		String mylight = "";
		if (lightnum > 100) {
			mylight = "Day";	
		}
		else {
			mylight = "Night";
		}
		String mywords = (String) servResponse.get("words");
		String mycoords = (String) servResponse.get("coords");
		try {
			byte[] decodedImage = Base64.decode(image64, Base64.DEFAULT);
			Bitmap photo = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
			image.setImageBitmap(photo);
		} catch (Exception e) {
			mywords = "No image available. " + mywords;
		}
		words.setText(mywords);
		light.setText("Solution was added during " + mylight + " time.");		
		coordstext.setText("Coordinates: " + mycoords);
		
	}

	
	//*********************************************************

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.solution, menu);
		return true;
	}

}
