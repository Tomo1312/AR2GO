package com.example.logindemo;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;

public class BroadcastService extends Service implements Serializable {
    private final static String TAG = "BroadcastService";
    public static final String COUNTDOWN_BR = "logindemo.countdown_br";
    private static final String ANDROID_CHANNEL_ID = "1";
    Intent bi = new Intent(COUNTDOWN_BR);
    protected static final long START_TIME_IN_MILIS = 30 * 1000;
    protected long mTimeLeftInMillis;
    protected CountDownTimer cdt = null;
    protected String userName, userEmail, unlockedSculptures;
    protected int lifes, bodovi;
    protected FirebaseAuth firebaseAuth;
    protected FirebaseDatabase firebaseDatabase;
    protected DatabaseReference databaseReference;
    public static boolean isServiceRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Starting timer...");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());
    }

    @Override
    public void onDestroy() {
        cdt.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        UserProfile user = (UserProfile) intent.getSerializableExtra("user");
        userName = intent.getStringExtra("userName");
        userEmail = intent.getStringExtra("userEmail");
        bodovi = intent.getIntExtra("bodovi", bodovi);
        unlockedSculptures = intent.getStringExtra("unlockedSculptures");
        lifes = intent.getIntExtra("lifes", lifes);
        startTimer();
        sendBroadcast(intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    protected void updateLifes(){
        lifes = lifes + 1;
        UserProfile addLifes = new UserProfile(userName, userEmail, bodovi, unlockedSculptures, lifes);
        databaseReference.setValue(addLifes);
        SecondActivity.mTimerRunning = false;

        if (lifes < 21){
            startTimer();
        }else{
            onDestroy();
        }
    }

    private void startTimer() {
        mTimeLeftInMillis = START_TIME_IN_MILIS;
        cdt = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onFinish() {
                updateLifes();
                Log.i(TAG, "Timer finished");
            }

            @Override
            public void onTick(long millisUntilFinished) {
                SecondActivity.mTimerRunning = true;
                //bi.putExtra("timeRunning", true);
                //bi.putExtra("countdown", millisUntilFinished);
                //sendBroadcast(bi);
            }
        }.start();
    }
}
