package com.roadgems.testaccelerometer;

/**
 * Created by Miki on 15/04/2016.
 */
public class Average {
    private double avg;
    private int total;
    private float counter;
    static final float THRESHOLD =0.8f;
    //private static int outsideCountLimit = 1000;


    public Average() {
        this.avg = 0;
        this.total = 0;
        this.counter = 0;
    }

    public void updateAverage(float new_point) {

//        if (isLimitExceded(new_point)) {
//            outsideCounter++;
//        } else outsideCounter = (outsideCounter < 0) ? 0 : outsideCounter--;

        total++;
        avg = avg + (new_point - avg) / total;

//        if (isOutsideCounterGreater())
//            reset();
    }

//    private boolean isOutsideCounterGreater() {
//        return counter >= outsideCountLimit;
//    }

    public double getAverage() {
        return this.avg;
    }

    public void reset() {
        this.avg = 0;
        this.total = 0;
        this.counter = 0;
    }

    public boolean isLimitExceded(float new_point) {
        return (Math.abs(new_point - avg) > THRESHOLD);
    }
}