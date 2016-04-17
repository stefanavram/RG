package com.roadgems.testaccelerometer;

/**
 * Created by Miki on 15/04/2016.
 */
public class Average {
    private double avg;
    private int total;
    private float outside;
    static final float THRESHOLD = 1;

    public Average() {
        this.avg = 0;
        this.total = 0;
        this.outside = 0;
    }

    public void updateAverage(float new_point) {
        total++;
        avg = (avg + (new_point - avg) / (total));

        if (Math.abs(new_point - avg) > THRESHOLD)
            outside++;
        else outside = (outside < 0) ? 0 : outside--;

        if (outside >= 5)
            reset();
    }

    public double getAverage() {
        return this.avg;
    }

    public void reset() {
        this.avg = 0;
        this.total = 0;
        this.outside = 0;
    }
}