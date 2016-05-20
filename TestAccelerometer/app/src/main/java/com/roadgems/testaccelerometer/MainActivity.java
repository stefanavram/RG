package com.roadgems.testaccelerometer;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.primitives.Floats;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener {

    GPSTracker gps;
    TextView outputView;
    private ProgressDialog progress;
    WebView myWebView;
    private boolean mapToggle = false;
    private boolean saveDataToggle = false;

    /*------openCV fields------*/
    DetectActivity act;
	TextView resultText;
    private static final String TAG = "RoadGems::Activity";
	private SensorManager mSensorManager;
    SensorEventListener accelerationListener;
    int sensorType1 = Sensor.TYPE_ACCELEROMETER;
    int sensorType2 = Sensor.TYPE_MAGNETIC_FIELD;
    float x = 0, y = 0, z = 0;
    List<Float> accData;
    private List<Float> accX; 
	private List<Float> accY;
	private List<Float> accZ;
    long timeStamp;
    List<Long> time;
    long totalTime = 5000;
    float label;
    Toast t;
    Button start;
    Button test;
    private boolean holeDetected = false;
    private Timer timer;
    /*-----------end------------*/
    
    
    /*----------*/
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                 // initialize the activity detection class.
                    act = new DetectActivity();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /*----------*/
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outputView = (TextView) findViewById(R.id.showOutput);

        myWebView = (WebView) findViewById(R.id.webView);
        myWebView.setVisibility(View.INVISIBLE);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        
        /*----------*/
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        resultText = (TextView) findViewById(R.id.activityText);
        
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        
        start=(Button)findViewById(R.id.startSensor);
        start.setOnClickListener(this);
        
        test=(Button)findViewById(R.id.testButton);
        test.setOnClickListener(this);
        /*----------*/
        timer = new Timer();
        timer.schedule(new HoleTimer(), 0, 1000);
        gps = new GPSTracker(MainActivity.this);

    }

    public void saveData(View view) {
        if (saveDataToggle == true) {
            Toast.makeText(getApplicationContext(), "Stop detecting..", Toast.LENGTH_SHORT).show();
            Button btnSave =(Button) findViewById(R.id.btnSave);
            btnSave.setText("Detect\nPotholes");
            stopService(new Intent(this, Vibrations.class));
            saveDataToggle = false;
        } else {
            Toast.makeText(getApplicationContext(), "Detecting potholes..", Toast.LENGTH_SHORT).show();
            Button btnSave =(Button) findViewById(R.id.btnSave);
            btnSave.setText("Stop\ndetecting");
            saveDataToggle = true;
            startService(new Intent(this, Vibrations.class));
        }
    }

    public void displayMap(View view) {
        if (mapToggle == false) {
            myWebView.loadUrl("https://roadgems.go.ro/map.html");
            myWebView.setVisibility(View.VISIBLE);
            mapToggle = true;
        } else {
            myWebView.setVisibility(View.INVISIBLE);
            mapToggle = false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, Vibrations.class));
        stopService(new Intent(this, GPSTracker.class));
    }
    
    @Override
    protected void onStop() {
        super.onStop();
    }
        
     @Override
    protected void onPause() {
        mSensorManager.unregisterListener(accelerationListener);
        super.onPause();
    }   

    @Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == start){
			startRecording();
			mTimer.start();
		}else if(v == test){
			if(accX!=null && accY!=null && accZ!=null)
				startDetecting();
			else{
				t = Toast.makeText(this, "No data to be processed", Toast.LENGTH_SHORT);
				t.show();
			}
		}
	}
    
    
    public void hole(View view) {

        gps = new GPSTracker(MainActivity.this);
        // check if GPS enabled
        markHole();
    }

    private void markHole() {
        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            new PostClass(this).execute(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(System.currentTimeMillis()));

            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            gps.showSettingsAlert();
        }
    }
    
    /*-----------------*/
    private final CountDownTimer mTimer = new CountDownTimer(totalTime, 1000) {
		@Override
		public void onTick(final long millisUntilFinished) {
		}
		@Override
		public void onFinish() {
			stopRecording();
		}
    };
    
    public void startRecording(){
		time = new ArrayList<Long>();
		accX = new ArrayList<Float>();
		accY =new ArrayList<Float>();
		accZ = new ArrayList<Float>();
		accData = new ArrayList<Float>();
		accelerationListener = new SensorEventListener() {
	    	
	    	@Override    
	        public void onSensorChanged(SensorEvent event) {
	            float accMag = 0;
	           
	            if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
	            x = event.values[0];
	            y = event.values[1];
	            z = event.values[2];
	            timeStamp = event.timestamp;
            	time.add(timeStamp);
	            float sum = (float) (Math.pow(x, 2)+Math.pow(y, 2)+Math.pow(z, 2));
            	accMag = (float) (Math.sqrt(sum)-9.8); 
            	accData.add(accMag);
            	accX.add(x);
            	accY.add(y);
            	accZ.add(z);
            	
	           }
	    }
	    	@Override
	        public void onAccuracyChanged(Sensor sensor, int accuracy) {
	        }
	    };
	    
        mSensorManager.registerListener(accelerationListener,mSensorManager.getDefaultSensor(sensorType1),
            SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(accelerationListener,mSensorManager.getDefaultSensor(sensorType2),
                SensorManager.SENSOR_DELAY_FASTEST);
	}
    
    public void stopRecording(){
		mSensorManager.unregisterListener(accelerationListener);
		
	}
    
    public void startDetecting(){
		float[] x = Floats.toArray(accX);
		float[] y = Floats.toArray(accY);
		float[] z = Floats.toArray(accZ);
		float[] mag = Floats.toArray(accData);

		 
		float actdetected = act.detect(x, y, z, mag, label);
		if(actdetected == 0) {
            resultText.setText("Activity Detected: Sitting");

        }
		else if(actdetected == 1) {
            resultText.setText("Activity Detected: Jogging");
            markHole();
        }
		else
			resultText.setText("Unknown Activity!");
		Toast toast1 = Toast.makeText(this, "Done!", Toast.LENGTH_SHORT);
		toast1.show();
	}
    /*-----------------*/

    private class HoleTimer extends TimerTask {
        public void run() {
            if (holeDetected) {
                if (gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    new PostClass(MainActivity.this).execute(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(System.currentTimeMillis()));
                }
                holeDetected = false;
            }
        }
    }


    private class PostClass extends AsyncTask<String, Void, Void> {
        private String http = "https://roadgems.go.ro/create_pothole.php";
        private final Context context;


        public PostClass(Context c) {
            this.context = c;
        }


        public String createPostParamsFromJson(JSONObject jsonobj) {
            StringBuilder postData = new StringBuilder();
            Iterator<?> keys = jsonobj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(key, "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(jsonobj.get(key)), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return postData.toString();
        }

        public JSONObject createJSONHole(Double lat, Double lng, String pothole) {
            JSONObject jsonobj = new JSONObject();

            try {

                jsonobj.put("lat", lat);
                jsonobj.put("lng", lng);
                jsonobj.put("pothole", pothole);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonobj;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Double lat = Double.valueOf(params[0]);
                Double lng = Double.valueOf(params[1]);
                String hole = params[2];
                JSONObject jsonobj = createJSONHole(lat, lng, hole);
                byte[] postDataBytes = createPostParamsFromJson(jsonobj).getBytes("UTF-8");
                //final TextView outputView = (TextView) findViewById(R.id.showOutput);

                URL url = new URL(http);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                OutputStream out = connection.getOutputStream();
                out.write(postDataBytes);
                out.flush();
                out.close();

                int responseCode = connection.getResponseCode();

                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Request Parameters " + jsonobj.toString());
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator") + "Type " + "POST");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                System.out.println("output===============" + br);
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }


    }
}