package com.roadgems.testaccelerometer;


public class AccelData {
    private long timestamp;
    private double x;
    private double y;
    private double z;
    private float avg_x;
    private float avg_y;
    private float avg_z;
    private float up_avg_x;
    private float above_avg_x;
    private float up_avg_y;
    private float above_avg_y;
    private float up_avg_z;
    private float above_avg_z;

    public AccelData(long timestamp, double x, double y, double z,
                     float avg_x,float avg_y, float avg_z,float threshold) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.avg_x=avg_x;
        this.avg_y=avg_y;
        this.avg_z=avg_z;
        this.up_avg_x=avg_x+threshold;
        this.up_avg_y=avg_y+threshold;
        this.up_avg_z=avg_z+threshold;
        this.above_avg_x=avg_x-threshold;
        this.above_avg_y=avg_y-threshold;
        this.above_avg_z=avg_z-threshold;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }
    public void setZ(double z) {
        this.z = z;
    }

    public float getAvg_x() {
        return avg_x;
    }
    public void setAvg_x(float avg_x) {
        this.avg_x = avg_x;
    }

    public float getAvg_y() {
        return avg_y;
    }
    public void setAvg_y(float avg_y) {
        this.avg_y = avg_y;
    }

    public float getAvg_z() {
        return avg_z;
    }
    public void setAvg_z(float avg_z) {
        this.avg_z = avg_z;
    }

    public float getAbove_avg_x() {
        return above_avg_x;
    }
    public void setAbove_avg_x(float above_avg_x) {
        this.above_avg_x = above_avg_x;
    }

    public float getAbove_avg_y() {
        return above_avg_y;
    }
    public void setAbove_avg_y(float above_avg_y) {
        this.above_avg_y = above_avg_y;
    }

    public float getAbove_avg_z() {
        return above_avg_z;
    }
    public void setAbove_avg_z(float above_avg_z) {
        this.above_avg_z = above_avg_z;
    }

    public float getUp_avg_x() {
        return up_avg_x;
    }
    public void setUp_avg_x(float up_avg_x) {
        this.up_avg_x = up_avg_x;
    }

    public float getUp_avg_y() {
        return up_avg_y;
    }
    public void setUp_avg_y(float up_avg_y) {
        this.up_avg_y = up_avg_y;
    }

    public float getUp_avg_z() {
        return up_avg_z;
    }
    public void setUp_avg_z(float up_avg_z) {
        this.up_avg_z = up_avg_z;
    }

    public String toString() {
        return "" + timestamp + "," + x + "," + y + "," + z+ ","
                  + avg_x+ "," + avg_y+ "," + avg_z+ ","
                  + up_avg_x+ "," + up_avg_y+ "," + up_avg_z+ ","
                  + above_avg_x+ "," + above_avg_y+ "," + above_avg_z;
    }

    public String coordinates() {
        return x + "," + y + "," + z;
    }

    public String averages(){
        return avg_x + "," + avg_y + "," + avg_y + ","
                + up_avg_x + "," + up_avg_y + "," + up_avg_z + ","
                + above_avg_x + "," + above_avg_y + "," + above_avg_z;
    }


}