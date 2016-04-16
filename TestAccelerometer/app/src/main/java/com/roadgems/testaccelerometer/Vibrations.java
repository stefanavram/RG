package com.roadgems.testaccelerometer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import java.util.ArrayList;

public class Vibrations extends Service implements SensorEventListener {




    static final float THRESHOLD = 0.5f;
    protected float[] gravSensorVals = new float[3];
    private boolean started = false;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ArrayList<AccelData> sensorData;
    private Filter filters = new Filter();
    private Average avg_x = new Average();
    private Average avg_y = new Average();
    private Average avg_z = new Average();
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {

//            gravSensorVals = filters.highPass(event.values.clone(), gravSensorVals);
//            avg_x.updateAverage(gravSensorVals[0]);
//            avg_y.updateAverage(gravSensorVals[1]);
//            avg_z.updateAverage(gravSensorVals[2]);
            avg_x.updateAverage(event.values[0]);
            avg_y.updateAverage(event.values[1]);
            avg_z.updateAverage(event.values[2]);


            long timestamp = System.currentTimeMillis();
            /*with filter*/
//            AccelData data = new AccelData(timestamp, gravSensorVals[0], gravSensorVals[1], gravSensorVals[2],
//                                            avg_x.getAverage(),avg_y.getAverage(),avg_z.getAverage(), THRESHOLD);

            AccelData data = new AccelData(timestamp, event.values[0], event.values[1], event.values[2],
                    avg_x.getAverage(),avg_y.getAverage(),avg_z.getAverage(), THRESHOLD);
            sensorData.add(data);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        DataSaver dataSaver = new DataSaver();
        dataSaver.save("save.csv", true, sensorData, getBaseContext());
        started = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//
//
//    }
}
