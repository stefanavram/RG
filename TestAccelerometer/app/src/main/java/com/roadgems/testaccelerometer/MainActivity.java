package com.roadgems.testaccelerometer;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
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
    WebView myWebView;
    private boolean mapToggle = false;
    private boolean saveDataToggle = false;

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