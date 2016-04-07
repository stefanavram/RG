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

    static final float ALPHA = 0.25f;
    protected float[] gravSensorVals = new float[3];
    private boolean started = false;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ArrayList<AccelData> sensorData;
    private Filter filters;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorData = new ArrayList<>();
        started = true;
        filters = new Filter();
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
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(myFile, true)));

            for (int i = 0; i < sensorData.size(); i++) {
                AccelData current = sensorData.get(i);
                out.write(current.getTimestamp() + "," + current.coordinates());
                out.write("\n");
            }
            out.close();
            Toast.makeText(getBaseContext(), "Data saved to file", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "No file", Toast.LENGTH_LONG).show();

        } finally {
            started = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {

            float[] lowPassFiltered = filters.lowPass(event.values.clone(), gravSensorVals);
            float[] highPassFiltered = filters.highPass(event.values.clone(), gravSensorVals);
            gravSensorVals[0] = (lowPassFiltered[0] + highPassFiltered[0]) / 2;
            gravSensorVals[1] = (lowPassFiltered[1] + highPassFiltered[1]) / 2;
            gravSensorVals[2] = (lowPassFiltered[2] + highPassFiltered[2]) / 2;

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
