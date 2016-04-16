package com.roadgems.testaccelerometer;

import android.content.Context;
import android.os.Environment;

import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by seby on 07.04.2016.
 */

public class DataSaver {

    private File createFile(String name) throws IOException {
        File Root = Environment.getExternalStorageDirectory();
        File Dir = createDir(Root);
        File myFile = new File(Dir, name);
        myFile.createNewFile();
        return myFile;
    }


    private File createDir(File root) {
        File Dir = new File(root.getAbsolutePath() + "/RoadGems");
        if (!Dir.exists()) {
            Dir.mkdir();
        }
        return Dir;
    }

    public void save(String fileName, boolean append, ArrayList<AccelData> sensorData, Context context) {

        try {
            File myFile = createFile(fileName);
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(myFile, append)));

            for (int i = 0; i < sensorData.size(); i++) {
                AccelData current = sensorData.get(i);
                out.write(current.getTimestamp() + "," + current.coordinates());
                out.write("\n");
            }
            out.close();
            Toast.makeText(context, "Data saved to file", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(context, "No file", Toast.LENGTH_LONG).show();

        }
    }
}
