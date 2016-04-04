package com.roadgems.testaccelerometer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Vibrations extends Service implements SensorEventListener {

    private boolean started = false;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ArrayList<AccelData> sensorData;

    static final float ALPHA = 0.25f;
    protected float[] gravSensorVals;


    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorData = new ArrayList<>();
        started = true;
    }

    private File createFile(String name) throws IOException {
        File Root = Environment.getExternalStorageDirectory();
        File Dir = createDir(Root);
        File myFile = new File(Dir, name);
        myFile.createNewFile();
        return myFile;
    }

    @NonNull
    private File createDir(File root) {
        File Dir = new File(root.getAbsolutePath() + "/RoadGems");
        if (!Dir.exists()) {
            Dir.mkdir();
        }
        return Dir;
    }

    public void save() {

        try {
            File myFile = createFile("save.txt");
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(myFile)));

            for (int i = 0; i < sensorData.size(); i++) {
                out.write(sensorData.get(i).toString());
                out.write("\n");
            }
            out.close();
            Toast.makeText(getBaseContext(), "Start saving data", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "No file", Toast.LENGTH_LONG).show();

        } finally {
            started = false;
        }
    }

    protected float[] lowPass(float[] input, float[] output) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {

            gravSensorVals=lowPass(event.values.clone(),gravSensorVals);

            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];
            long timestamp = System.currentTimeMillis();
            AccelData data = new AccelData(timestamp, gravSensorVals[0], gravSensorVals[1], gravSensorVals[2]);
            sensorData.add(data);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        save();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
