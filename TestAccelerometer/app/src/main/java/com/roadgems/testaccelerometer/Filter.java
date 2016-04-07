package com.roadgems.testaccelerometer;

/**
 * Created by Miki on 06/04/2016.
 */
public class Filter {

    static final float ALPHA_LOW = 0.25f;
    static final float ALPHA_HIGH = 0.25f;

    public float[] lowPass(float[] input, float[] output) {
        if (output == null) {
            return input;
        }
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA_LOW * (input[i] - output[i]);
        }
        return output;
    }

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
