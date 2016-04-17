package com.roadgems.testaccelerometer;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class MainActivity extends Activity {

    GPSTracker gps;
    TextView outputView;


    private ProgressDialog progress;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outputView = (TextView) findViewById(R.id.showOutput);


        startService(new Intent(this, Vibrations.class));

    }

    public void saveData(View view) {
        stopService(new Intent(this, Vibrations.class));
    }

    public void displayMap(View view) {

        Intent myIntent = new Intent(view.getContext(), Map.class);
        startActivityForResult(myIntent, 0);
    }

    public void hole(View view) {

        gps = new GPSTracker(MainActivity.this);
        // check if GPS enabled
        checkIfGPSEnabled();
    }

    private void checkIfGPSEnabled() {
        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            new PostClass(this).execute(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(System.currentTimeMillis()));

            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            gps.showSettingsAlert();
        }
    }

    public void postData(View view) {
        //new PostClass(this).execute(String.valueOf(lat), String.valueOf(lng), "Put");
    }


    private class PostClass extends AsyncTask<String, Void, Void> {
        private String http = "https://roadgems.go.ro/create_pothole.php";
        private final Context context;

        public PostClass(Context c) {
            this.context = c;
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
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