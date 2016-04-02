package com.roadgems.testaccelerometer;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener, ConnectionCallbacks,
        OnConnectionFailedListener {


    String http = "http://roadgems.ml/create_pothole.php";
    private ProgressDialog progress;
    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float) 1.0;
    protected ArrayList<String> xCoord = new ArrayList<>();
    protected ArrayList<String> yCoord = new ArrayList<>();
    protected ArrayList<String> zCoord = new ArrayList<>();

    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient client;


    TextView outputView;
    private double lat;
    private double lng;

    boolean stopFlag = false;
    boolean startFlag = false;
    boolean isFirstSet = true;
    boolean isFileCreated = false;
    File myFile;
    FileOutputStream fOut;
    OutputStreamWriter myOutWriter;
    BufferedWriter myBufferedWriter;
    PrintWriter myPrintWriter;

    float x;
    float y;
    float z;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        outputView = (TextView) findViewById(R.id.showOutput);

        Button map = (Button) findViewById(R.id.map);
        map.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), Map.class);
                startActivityForResult(myIntent, 0);
            }

        });

        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        Button btnGps = (Button) findViewById(R.id.btnGps);
        btnGps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                displayLocation();
            }

        });

        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    createFile();
                    fOut = new FileOutputStream(myFile);
                    myOutWriter = new OutputStreamWriter(fOut);
                    myBufferedWriter = new BufferedWriter(myOutWriter);
                    myPrintWriter = new PrintWriter(myBufferedWriter);
                    Toast.makeText(getBaseContext(), "Start saving data", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "No file", Toast.LENGTH_LONG).show();

                } finally {
                    startFlag = true;
                }

            }
        });

        Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    stopFlag = true;
                    Toast.makeText(getBaseContext(), "Data saved", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void createFile() throws IOException {
        File Root = Environment.getExternalStorageDirectory();
        File Dir = createDir(Root);
        myFile = new File(Dir, "save.txt");
        myFile.createNewFile();
    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            this.lat=latitude;
            this.lng=longitude;
            outputView.setText(latitude + ", " + longitude);

        } else {

            outputView.setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    public void saveToTxt() {

        myPrintWriter.append("" + x + "  " + y + "  " + z + "\n");
        //myPrintWriter.flush();
    }

    @NonNull
    private File createDir(File root) {
        File Dir = new File(root.getAbsolutePath() + "/RoadGems");
        if (!Dir.exists()) {
            Dir.mkdir();
        }
        return Dir;
    }


    public double getLat(){
        return this.lat;
    }
    public double getLng(){
        return this.lng;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        checkPlayServices();
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView tvX = (TextView) findViewById(R.id.x_axis);
        TextView tvY = (TextView) findViewById(R.id.y_axis);
        TextView tvZ = (TextView) findViewById(R.id.z_axis);

        if(startFlag){

            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                x= event.values[0];
                y = event.values[1];
                z = event.values[2];
                tvX.setText(Float.toString(x));
                tvY.setText(Float.toString(y));
                tvZ.setText(Float.toString(z));


            }

            for(int i=0; i<1;i++){
                if(!stopFlag){
                    saveToTxt();
                }
                else{
                    try{
                        myOutWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }catch(NullPointerException e){
                        e.printStackTrace();
                    }

                    try{
                        fOut.close();
                    }catch(IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }



        }


    }

    public void postData(View view) {

        new PostClass(this).execute("1", "2", "Put");
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
    public JSONObject createJSONHole(Integer lat, Integer lng, String pothole) {
        JSONObject jsonobj = new JSONObject();

        try {

            jsonobj.put("lat", this.getLat());
            jsonobj.put("lng", this.getLng());
            jsonobj.put("pothole", pothole);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonobj;
    }

    @Override
    public void onConnected(Bundle bundle) {

        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }


    private class PostClass extends AsyncTask<String, Void, Void> {

        private final Context context;

        public PostClass(Context c) {
            this.context = c;
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Integer lat = Integer.valueOf(params[0]);
                Integer lng = Integer.valueOf(params[1]);
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

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        outputView.setText(output);
                        progress.dismiss();
                    }
                });

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