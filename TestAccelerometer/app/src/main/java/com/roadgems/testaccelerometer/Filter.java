package com.roadgems.testaccelerometer;

import java.io.FileOutputStream;

/**
 * Created by Miki on 06/04/2016.
 */
public class Filter {

    static final float ALPHA_HIGH = 0.25f;

    public float[] highPass(float[] input, float[] output) {
        if (output == null) {
            return input;
        }
        for (int i = 1; i < input.length; i++) {
            output[i] = ALPHA_HIGH * (output[i] - input[i - 1] + input[i]);
        }
        return output;
    }

}
