package com.roadgems.testaccelerometer;

/**
 * Created by Miki on 15/04/2016.
 */
public class Average {
    private float avg;
    private int total;

    public Average(){
        this.avg=0;
        this.total=0;
    }

    public void updateAverage(float new_point){
        total++;
        avg = (avg + (new_point - avg)/(total));

    }

    public float getAverage(){
        return this.avg;
    }
}
