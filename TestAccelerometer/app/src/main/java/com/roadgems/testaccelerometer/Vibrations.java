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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class Vibrations extends Service implements SensorEventListener {
    float x;
    float y;
    float z;
    boolean stopFlag = false;
    boolean startFlag = false;
    boolean isFirstSet = true;
    boolean isFileCreated = false;
    File myFile;
    FileOutputStream fOut;
    OutputStreamWriter myOutWriter;
    BufferedWriter myBufferedWriter;
    PrintWriter myPrintWriter;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean mInitialized;

    @Override
    public void onCreate() {
        super.onCreate();
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void createFile() throws IOException {
        File Root = Environment.getExternalStorageDirectory();
        File Dir = createDir(Root);
        myFile = new File(Dir, "save.txt");
        myFile.createNewFile();
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

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (startFlag)

        {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
            }

            for (int i = 0; i < 1; i++) {
                if (!stopFlag) {
                    saveToTxt();
                } else {
                    try {
                        myOutWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    try {
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void save() {

        try {
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
}
