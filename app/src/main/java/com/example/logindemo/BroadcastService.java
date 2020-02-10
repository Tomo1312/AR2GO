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

import java.util.Locale;

public class BroadcastService extends Service {
    private final static String TAG = "BroadcastService";
    protected static final long START_TIME_IN_MILIS = 30 * 60 * 1000;
    protected long mTimeLeftInMillis;
    protected CountDownTimer cdt = null;
    protected String userName, userEmail, unlockedSculptures;
    protected int lifes, bodovi;
    protected FirebaseAuth firebaseAuth;
    protected FirebaseDatabase firebaseDatabase;
    protected DatabaseReference databaseReference;
    public boolean isFirst;
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


    public void startTimer() {
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
                        if(timeLeftFormated.equals("00:00")){
                            SecondActivity.time.setText("");
                        }else{
                            SecondActivity.time.setText(timeLeftFormated);
                        }
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
        lifes++;
        UserProfile addLifes = new UserProfile(userName, userEmail, bodovi, unlockedSculptures, lifes);
        databaseReference.setValue(addLifes);

        if (lifes > 19){
            stoptimertask();
            stopForeground(true);
            stopService(broadcastIntent);
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
}
