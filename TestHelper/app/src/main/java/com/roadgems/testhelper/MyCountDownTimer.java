package com.roadgems.testhelper;

/**
 * Created by Miki on 02/04/2016.
 */
import android.os.CountDownTimer;
import android.widget.ViewFlipper;

public class MyCountDownTimer extends CountDownTimer{
    private boolean running = false;

    public MyCountDownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onFinish() {
        running = false;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        running = true;
    }

    public boolean isRunning() {
        return running;
    }

    public void cancelTimer() {
        running = false;
        cancel();
    }
}
