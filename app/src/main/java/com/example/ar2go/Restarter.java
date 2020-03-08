package com.example.ar2go;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class Restarter extends BroadcastReceiver {
    public static long milisLeft;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();

        milisLeft = Timer.tempLeftMilis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, Timer.class));
        } else {
            context.startService(new Intent(context, Timer.class));
        }
    }
}