package com.remkohde.sphero_test2;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import orbotix.robot.base.Robot;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

import com.remkohde.sphero_test2.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class MainActivity extends Activity {

	private SpheroConnectionView mSpheroConnectionView = null;
	private Sphero mSphero = null;
	
	@Override
	protected void onResume() {
	    // Required by android, this line must come first
	    super.onResume();
	    // This line starts the discovery process which finds Sphero's which can be connected to
	    mSpheroConnectionView.startDiscovery();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_main);
		
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        
		// Find Sphero Connection View from layout file
		mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);

		// This event listener will notify you when these events occur, it is up to you what you want to do during them
		ConnectionListener mConnectionListener = new ConnectionListener() {
		    @Override
		    // The method to run when a Sphero is connected
		    public void onConnected(Robot sphero) {
		    	Log.d("remkohdev-MySphero", "CONNECTED!");
		    	
		        // Hides the Sphero Connection View
		        mSpheroConnectionView.setVisibility(View.INVISIBLE);
		        // Cache the Sphero so we can send commands to it later
		        mSphero = (Sphero) sphero;
		        
		        // You can add commands to set up the ball here, these are some examples

		        /**
		        // Set the back LED brightness to full
		        mSphero.setBackLEDBrightness(1.0f);
		        // Set the main LED color to blue at full brightness
		        mSphero.setColor(0, 0, 255);
				*/
		        
		        //blink(true);
		        Button b1 = (Button)findViewById(R.id.button1);
		        b1.setVisibility(Button.VISIBLE);
		        // getSentimentButton
		        Button b2 = (Button)findViewById(R.id.button2);
		        b2.setVisibility(Button.VISIBLE);
		        EditText e3 = (EditText)findViewById(R.id.editText1);
		        e3.setVisibility(Button.VISIBLE);
		        //connectedSwitch
		        Switch connectedSwitch = (Switch) findViewById(R.id.switch1);
		        connectedSwitch.setChecked(true); //false default 
		        
		    }

		    // The method to run when a connection fails
		    @Override
		    public void onConnectionFailed(Robot sphero) {
		    	Log.d("remkohdev-MySphero", "CONNECTED FAILED");
		        // let the SpheroConnectionView handle or hide it and do something here...
		    	Switch connectedSwitch = (Switch) findViewById(R.id.switch1);
		        connectedSwitch.setChecked(false);  
		    }

		    // Ran when a Sphero connection drops, such as when the battery runs out or Sphero sleeps
		    @Override
		    public void onDisconnected(Robot sphero) {
		    	Log.d("remkohdev-MySphero", "DISCONNECTED!");
		    	Switch connectedSwitch = (Switch) findViewById(R.id.switch1);
		        connectedSwitch.setChecked(false);  
		    	// Starts looking for robots
		        mSpheroConnectionView.startDiscovery();
		        
		    }
		};
		// Add the listener to the Sphero Connection View
		mSpheroConnectionView.addConnectionListener(mConnectionListener);	
	}
	
	@Override
	public void onStart(){
	    super.onStart();
	    Log.d("remkohdev-MySphero", "STARTED!");
	}
	
	public void blinkButtonClicked(View v) {		
		Log.d("remkohdev-MySphero", "BLINK!");
		blink(true);
    }
	
	private void blink(final boolean lit){
		
        if(mSphero != null){

            //If not lit, send command to show blue light, or else, send command to show no light
            if(lit){
                mSphero.setColor(0, 0, 0);//turn off the LED.
            }else{
            	boolean red = Math.random() < 0.5;
            	if(red){
            		mSphero.setColor(255, 0, 0);
            	}else{
            		mSphero.setColor(0, 0, 255);//(R,G,B): turn on the blue LED at full brightness
            	}
            }

            //Send delayed message on a handler to run blink again
            final Handler handler = new Handler();                       // 3
            handler.postDelayed(new Runnable() {
                public void run() {
                    blink(!lit);
                }
            }, 1000);
        }
    }
	
	public void getSentimentButtonClicked(View v) {		
		Log.d("remkohdev-MySphero", "GET SENTIMENT ANALYSIS!");
		getSentiment();
    }
	
	public void getSentiment(){
		
		HttpClient httpclient = new DefaultHttpClient();
		String apikey = "2dee943c-31a9-4cc2-a6bf-a5ec570397f6";
		String url = "https://api.idolondemand.com/1/api/sync/analyzesentiment/v1";
		url += "?apikey="+apikey+"&text=i+feel+great+and+my+pet+is+the+best+ever";
		HttpGet httpget = new HttpGet(url); 
		HttpResponse response = null;
		
		Log.d("remkohdev-MySphero", "1");
		try {

			response = (HttpResponse) httpclient.execute(httpget);
			Log.d("remkohdev-MySphero", "1");
			HttpEntity entity = response.getEntity();
			String body = EntityUtils.toString(entity);
			JSONObject jsonObj = new JSONObject(body);
			JSONObject aggregate = jsonObj.getJSONObject("aggregate");
			String sentiment = aggregate.getString("sentiment");
			String score = aggregate.getString("score");
			String sentimentScore = sentiment + ": "+ score;
			EditText e3 = (EditText)findViewById(R.id.editText1);
		    e3.setText(sentimentScore);
			Log.d("remkohdev-MySphero", sentimentScore);
		    
		 }catch(ClientProtocolException cpe){
			 cpe.printStackTrace();
		 }catch(IOException ioe){
			 ioe.printStackTrace();
		 }catch(JSONException jsone){
			 jsone.printStackTrace();
		 }
	}
	
	public void connectedSwitchClicked(View v) {		
		
		//Switch connectedSwitch = (Switch) findViewById(R.id.switch1);
        
    }

	

}
