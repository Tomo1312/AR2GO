package com.example.logindemo;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static java.lang.String.valueOf;

public class TimerService extends IntentService {
    protected String userName, userEmail, unlockedSculptures;
    protected int lifes, bodovi;
    protected FirebaseAuth firebaseAuth;
    protected FirebaseDatabase firebaseDatabase;
    protected DatabaseReference databaseReference;
    protected static final long START_TIME_IN_MILIS = 30 * 1000;
    protected long mTimeLeftInMillis;
    protected CountDownTimer cdt = null;
    private final static String TAG = "BroadcastService";
    public TimerService(){
        super("Timer service");
    }


    @Override
    public void onCreate(){
        super.onCreate();
        Log.v("timer", "timerservice");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());
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

            }
        });
     }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        if (intent == null){
            startTimer();
            Log.i(TAG, "TIMER == NULL");
            return;
        }

        Log.i(TAG, "TIMER =! NULL");
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
                Log.i(TAG, valueOf(millisUntilFinished));
                //bi.putExtra("timeRunning", true);
                //bi.putExtra("countdown", millisUntilFinished);
                //sendBroadcast(bi);
            }
        }.start();
        Bundle bundle = new Bundle();
        bundle.putString("message", "Counting done...");
        receiver.send(1234, bundle);

    }
    private void startTimer() {
        cdt = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onFinish() {
                updateLifes();
                Log.i(TAG, "Timer finished");
            }

            @Override
            public void onTick(long millisUntilFinished) {
                SecondActivity.mTimerRunning = true;
                Log.i(TAG, valueOf(millisUntilFinished));
                //bi.putExtra("timeRunning", true);
                //bi.putExtra("countdown", millisUntilFinished);
                //sendBroadcast(bi);
            }
        }.start();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
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

}
