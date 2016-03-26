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
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends Activity implements SensorEventListener {
    private ProgressDialog progress;
    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float) 1.0;
    protected ArrayList<String> xCoord = new ArrayList<>();
    protected ArrayList<String> yCoord = new ArrayList<>();
    protected ArrayList<String> zCoord = new ArrayList<>();

    // Button btnS = (Button) findViewById(R.id.btnSave);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Button map = (Button) findViewById(R.id.map);
        map.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), Map.class);
                startActivityForResult(myIntent, 0);
            }

        });
    }

    private String makeStringFromSensorData(ArrayList<String> list) {
        String finalFrom = null;
        for (int i = 0; i < list.size(); i++) {
            finalFrom += list.get(i) + "\n";

        }
        return finalFrom;
    }

    public void saveToTxt(View view) {

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File Root = Environment.getExternalStorageDirectory();
            File Dir = createDir(Root);
            File save_file = new File(Dir, "save.txt");
            File file1 = new File(Dir, "xData.txt");
            File file2 = new File(Dir, "yData.txt");
            File file3 = new File(Dir, "zData.txt");

            String x_values = makeStringFromSensorData(xCoord);
            String y_values = makeStringFromSensorData(yCoord);
            String z_values = makeStringFromSensorData(zCoord);

            try {
                writeExternalSD(file1, x_values);
                writeExternalSD(file2, y_values);
                writeExternalSD(file3, z_values);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "SD card not found", Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    private File createDir(File root) {
        File Dir = new File(root.getAbsolutePath() + "/RoadGems");
        if (!Dir.exists()) {
            Dir.mkdir();
        }
        return Dir;
    }

    private void writeExternalSD(File file, String msg) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(msg.getBytes());
        fileOutputStream.close();
        Toast.makeText(getApplicationContext(), "Save succeded", Toast.LENGTH_LONG).show();
    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
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


        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            tvX.setText("0.0");
            tvY.setText("0.0");
            tvZ.setText("0.0");
            mInitialized = true;
        } else {
            float deltaX = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);
            if (deltaX < NOISE) deltaX = (float) 0.0;
            if (deltaY < NOISE) deltaY = (float) 0.0;
            if (deltaZ < NOISE) deltaZ = (float) 0.0;
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            tvX.setText(Float.toString(deltaX));
            xCoord.add("" + deltaX);
            tvY.setText(Float.toString(deltaY));
            yCoord.add("" + deltaY);
            tvZ.setText(Float.toString(deltaZ));
            zCoord.add("" + deltaZ);

        }
    }

    public void postData(View view) {
        new PostClass(this).execute();
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
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("lat", 22);
                jsonobj.put("lng", 12);
                jsonobj.put("pothole", "Groapa mare");

                StringBuilder postData = new StringBuilder();
                Iterator<?> keys = jsonobj.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(key, "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf( jsonobj.get(key)), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");


                final TextView outputView = (TextView) findViewById(R.id.showOutput);
                URL url = new URL("http://roadgems.ml/create_pothole.php");
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
//              BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String line = "";
//                StringBuilder responseOutput = new StringBuilder();
//                System.out.println("output===============" + br);
//                while ((line = br.readLine()) != null) {
//                    responseOutput.append(line);
//                }
//                br.close();
//
//                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
//JSONObject jsonobj = new JSONObject();
//jsonobj.put("lat", 22);
//        jsonobj.put("lng", 12);
//        jsonobj.put("pothole", "Groapa mare");
//
//final TextView outputView = (TextView) findViewById(R.id.showOutput);
//        URL url = new URL("http://roadgems.ml/create_product.php");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
//        connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
//        connection.setDoOutput(true);
//        DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
//        dStream.writeBytes(jsonobj.toString());
//        dStream.flush();
//        dStream.close();
//        int responseCode = connection.getResponseCode();
//
//final StringBuilder output = new StringBuilder("Request URL " + url);
//        output.append(System.getProperty("line.separator") + "Request Parameters " +jsonobj.toString());
//        output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
//        output.append(System.getProperty("line.separator") + "Type " + "POST");
//        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        String line = "";
//        StringBuilder responseOutput = new StringBuilder();
//        System.out.println("output===============" + br);
//        while ((line = br.readLine()) != null) {
//        responseOutput.append(line);
//        }
//        br.close();
//
//        output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());
//
//        MainActivity.this.runOnUiThread(new Runnable() {
//
//@Override
//public void run() {
//        outputView.setText(output);
//        progress.dismiss();
//        }
//        });
//
//
//

