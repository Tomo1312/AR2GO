package com.example.logindemo;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class BroadcastService extends Service {
    private final static String TAG = "BroadcastService";
    public static final String COUNTDOWN_BR = "logindemo.countdown_br";
    Intent bi = new Intent(COUNTDOWN_BR);
    protected static final long START_TIME_IN_MILIS = 30 * 1000;
    protected long mTimeLeftInMillis;
    CountDownTimer cdt = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Starting timer...");
        mTimeLeftInMillis = START_TIME_IN_MILIS;
        cdt = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onFinish() {
                SecondActivity.updateLifes();
                Log.i(TAG, "Timer finished");
                onDestroy();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                SecondActivity.mTimerRunning = true;
                bi.putExtra("timeRunning", true);
                bi.putExtra("countdown", millisUntilFinished);
                sendBroadcast(bi);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        cdt.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
