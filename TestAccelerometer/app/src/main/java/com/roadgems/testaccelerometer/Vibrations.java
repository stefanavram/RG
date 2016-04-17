package com.roadgems.testaccelerometer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class Vibrations extends Service implements SensorEventListener {


    static final double THRESHOLD = 0.8;
    private boolean started = false;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ArrayList<AccelData> sensorData;
    private Average avg_x = new Average();
    private Average avg_y = new Average();
    private Average avg_z = new Average();
    private GPSTracker gps;
    private Timer timer;
    private boolean holeDetected = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorData = new ArrayList<>();
        started = true;
        gps = new GPSTracker(Vibrations.this);
        timer = new Timer();

        timer.schedule(new HoleTimer(), 0, 2 * 1000); // 2 seconds
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {
            avg_x.updateAverage(event.values[0]);
            avg_y.updateAverage(event.values[1]);
            avg_z.updateAverage(event.values[2]);

            long timestamp = System.currentTimeMillis();

            AccelData data = new AccelData(timestamp, event.values[0], event.values[1], event.values[2],
                    avg_x.getAverage(), avg_y.getAverage(), avg_z.getAverage(), THRESHOLD);
            sensorData.add(data);


            if (Math.abs(event.values[0] - avg_x.getAverage()) > THRESHOLD ||
                    Math.abs(event.values[1] - avg_y.getAverage()) > THRESHOLD ||
                    Math.abs(event.values[2] - avg_z.getAverage()) > THRESHOLD)
                holeDetected = true;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        DataSaver dataSaver = new DataSaver();
        dataSaver.save("save.csv", false, sensorData, getBaseContext());
        started = false;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class HoleTimer extends TimerTask {
        public void run() {
            if (holeDetected) {
                if (gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    new PostClass(Vibrations.this).execute(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(System.currentTimeMillis()));
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
