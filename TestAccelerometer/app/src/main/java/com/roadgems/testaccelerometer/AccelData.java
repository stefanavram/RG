package com.roadgems.testaccelerometer;


public class AccelData {
    private long timestamp;
    private double x;
    private double y;
    private double z;
    private double avg_x;
    private double avg_y;
    private double avg_z;
    private double up_avg_x;
    private double above_avg_x;
    private double up_avg_y;
    private double above_avg_y;
    private double up_avg_z;
    private double above_avg_z;

    public AccelData(long timestamp, double x, double y, double z,
                     double avg_x, double avg_y, double avg_z, double threshold) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.avg_x = avg_x;
        this.avg_y = avg_y;
        this.avg_z = avg_z;
        this.up_avg_x = avg_x + threshold;
        this.up_avg_y = avg_y + threshold;
        this.up_avg_z = avg_z + threshold;
        this.above_avg_x = avg_x - threshold;
        this.above_avg_y = avg_y - threshold;
        this.above_avg_z = avg_z - threshold;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String toString() {
        return "" + timestamp + "," + x + "," + y + "," + z + ","
                + avg_x + "," + avg_y + "," + avg_z + ","
                + up_avg_x + "," + up_avg_y + "," + up_avg_z + ","
                + above_avg_x + "," + above_avg_y + "," + above_avg_z;
    }

    public String coordinates() {
        return x + "," + y + "," + z;
    }

    public String averages() {
        return avg_x + "," + avg_y + "," + avg_z + ","
                + up_avg_x + "," + up_avg_y + "," + up_avg_z + ","
                + above_avg_x + "," + above_avg_y + "," + above_avg_z;
    }


}