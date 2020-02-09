package com.example.logindemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.String.valueOf;

public class BroadcastService extends Service {
    public int counter = 0;
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
    public boolean isFirst;
    public int delay;
    protected Intent broadcastIntent;
    protected static long tempLeftMilis;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Starting timer...");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                unlockedSculptures = userProfile.getOtkljucaneSkulputre();
                bodovi = userProfile.getUserBodovi();
                userName = userProfile.getUserName();
                userEmail = userProfile.getUserEmail();
                lifes = userProfile.getUserLifes();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BroadcastService.this,"Couldn't connect to database",Toast.LENGTH_LONG).show();
            }
        });
        tempLeftMilis = Restarter.milisLeft;

        if(tempLeftMilis > 0){
            mTimeLeftInMillis = tempLeftMilis;
        }else {
            mTimeLeftInMillis = START_TIME_IN_MILIS;
        }
        isFirst = true;
        startTimer();
        return START_STICKY;
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
/*        if(isFirst){
            delay = 20  * 1000;
        }else{
            delay = 0;
        }
        if (timer == null) {
            timer = new Timer();
    *//*        timerTask = new TimerTask() {
                public void run() {
                    Log.i("Count", "=========  "+ (counter++));
                }
            };*//*
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateLifes();
                    Log.d("SCHEDULE FIXED RATE", "ideee");
                }

            }, delay, 20 * 1000);
        }*/
        cdt = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onFinish() {
                updateLifes();
                Log.i(TAG, "Timer finished");
            }

            @Override
            public void onTick(long millisUntilFinished) {

                int minutes = (int) millisUntilFinished / 1000 / 60;
                int seconds = (int) millisUntilFinished / 1000 % 60;
                String timeLeftFormated = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                    try{
                        SecondActivity.time.setText(timeLeftFormated);
                        SecondActivity.mTimerRunning = true;
                    }catch (Exception ex){

                    }
                    tempLeftMilis = millisUntilFinished;
            }
        }.start();
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

        isFirst = false;
        if (lifes >= 20){
            stopService(broadcastIntent);
            //stopService(BroadcastService.this);
        }else{
            mTimeLeftInMillis = START_TIME_IN_MILIS;
            startTimer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();

        broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(BroadcastService.this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    public void stoptimertask() {
        if (cdt != null) {
            try {
                cdt.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

/*    private void startTimer() {
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
    }*/
}
