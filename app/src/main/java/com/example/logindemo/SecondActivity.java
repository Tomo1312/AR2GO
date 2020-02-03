package com.example.logindemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Locale;

import static java.lang.String.valueOf;

public class SecondActivity extends AppCompatActivity {

    private static TextView time;
    private ImageView info, tickets, events;
    private String eventMessage;
    private TextView freeLance, story, premiumStory, profileName, profileBodovi,collections, profileLifes;
    private ConstraintLayout layoutFreeLance, layoutStory, layoutPremiumStory, layoutCollections;
    private Animation atg;
    private FirebaseAuth firebaseAuth;
    private static int lifes, bodovi;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    protected static long mTimeLeftInMillis;
    protected static boolean mTimerRunning = false;
    private static String unlockedSculptures, userName, userEmail;

/*    protected static final long START_TIME_IN_MILIS = 10 * 1000;
    private CountDownTimer mCountDownTimer;
    protected static long mTimeLeftInMillis;
    private long mEndTime;
    private final static String TAG = "BroadcastService";
    protected static UserProfile user;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        setUiVeiws();

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
                profileName.setText(userProfile.getUserName());
                profileBodovi.setText("= " + valueOf(bodovi));
                profileLifes.setText("="+ valueOf(lifes));
                if (!mTimerRunning && lifes < 20){
                    //user = new UserProfile(userName, userEmail, bodovi, unlockedSculptures, lifes);
                    Intent i = new Intent(SecondActivity.this, BroadcastService.class);
                    i.putExtra("userName", userName);
                    i.putExtra("userEmail", userEmail);
                    i.putExtra("bodovi", bodovi);
                    i.putExtra("unlockedSculptures", unlockedSculptures);
                    i.putExtra("lifes", lifes);

                    startService(i);
                    //ContextCompat.startForegroundService(SecondActivity.this,i);
                    //startForegroundService(SecondActivity.this, new Intent(SecondActivity.this, BroadcastService.class));
                    Log.i("Broadcast", "Started service");
                }else if(lifes == 20){
                    try{
                        stopService(new Intent(SecondActivity.this, BroadcastService.class));
                    }catch (Exception ex){

                    }}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SecondActivity.this,"Couldn't connect to database",Toast.LENGTH_LONG).show();
            }
        });

        final DatabaseReference databaseReferenceOFEvents = firebaseDatabase.getReference("Events");
        databaseReferenceOFEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventMessage = new String();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    MuseumEvent event = postSnapshot.getValue(MuseumEvent.class);
                    String eventName = event.getName();
                    String eventDescription = event.getDescription();
                    String eventDuration = event.getDuration();

                    eventMessage = eventMessage + eventName + "\n"+ eventDuration + "\n" + eventDescription + "\n\n";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SecondActivity.this,"Couldn't connect to database",Toast.LENGTH_LONG).show();
            }
        });

        layoutFreeLance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SecondActivity.this, FreeLanceActivity.class));
            }
        });

        layoutStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SecondActivity.this, "Not supported yet!", Toast.LENGTH_LONG).show();
            }
        });

        layoutPremiumStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SecondActivity.this, "Not supported yet!", Toast.LENGTH_LONG).show();
            }
        });

        layoutCollections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SecondActivity.this, CollectionActivity.class));
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo();
            }
        });

        tickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SecondActivity.this, "Not supported yet!", Toast.LENGTH_LONG).show();
            }
        });

        events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventBox();
            }
        });

        profileLifes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning){
                    int minutes = (int) mTimeLeftInMillis / 1000 / 60;
                    int seconds = (int) mTimeLeftInMillis / 1000 % 60;
                    String timeLeftFormated = "In " + valueOf(minutes) + " minutes and " + valueOf(seconds)+ " seconds you will get another life";
                    Toast.makeText(SecondActivity.this, timeLeftFormated , Toast.LENGTH_LONG);
                }
                else{
                    Toast.makeText(SecondActivity.this, "Max number of lives is reached!" , Toast.LENGTH_LONG);
                }
            }
        });
        Boolean flag = displayGpsStatus();
        if(!flag){
            alertbox("Gps Status!!", "Your GPS is: OFF");
        }
    }


/*    protected static BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGui(intent);
        }
    };

    protected static void updateGui(Intent intent){
        if (intent.getExtras() != null) {
            long mTimeLeftInMillis = intent.getLongExtra("countdown", 0);
            int minutes = (int) mTimeLeftInMillis / 1000 / 60;
            int seconds = (int) mTimeLeftInMillis / 1000 % 60;
            String timeLeftFormated = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            time.setText(timeLeftFormated);
        }
    }*/
    /*@Override
    public void onResume() {
        super.onResume();
        //registerReceiver(br, new IntentFilter(BroadcastService.COUNTDOWN_BR));
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregisterReceiver(br);
    }

    @Override
    public void onStop() {
        try {
            //unregisterReceiver(br);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        //stopService(new Intent(SecondActivity.this, BroadcastService.class));
        super.onDestroy();
    }*/



    /*----Method to Check GPS is enable or disable ----- */
    @NonNull
    protected Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    /*----------Method to create an AlertBox ------------- */
    protected void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Device's GPS is Disable")
                .setCancelable(false)
                .setTitle("** Gps Status **")
                .setPositiveButton("Gps On",
                        new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull DialogInterface dialog, int id) {
                                // finish the current activity
                                // AlertBoxAdvance.this.finish();
                                Intent myIntent = new Intent(
                                        Settings.ACTION_SECURITY_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void eventBox(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Current events available")
                .setMessage(eventMessage)
                .setNeutralButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("ar2go is app for exploring the beauty of the city and collecting cards of sculptures to get some points that you can change for various tickets")
                .setCancelable(false)
                .setTitle("** INFO **")
                .setNeutralButton("Okay!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setUiVeiws() {
        info = (ImageView)findViewById(R.id.ivInfo);
        tickets = (ImageView)findViewById(R.id.ivTickets);
        events = (ImageView)findViewById(R.id.ivEvents);
        freeLance = (TextView)findViewById(R.id.tvFreelance);
        story = (TextView)findViewById(R.id.tvStory);
        premiumStory = (TextView)findViewById(R.id.tvPremiumStory);
        collections = (TextView)findViewById(R.id.tvColletions);
        time = (TextView)findViewById(R.id.tvTime);

        layoutFreeLance = (ConstraintLayout)findViewById(R.id.layoutFreelance);
        layoutStory = (ConstraintLayout)findViewById(R.id.layoutStory);
        layoutPremiumStory = (ConstraintLayout)findViewById(R.id.layoutPremiumStory);
        layoutCollections = (ConstraintLayout)findViewById(R.id.layoutColletions);

        profileName = (TextView) findViewById(R.id.tvProfileName);
        profileBodovi = (TextView) findViewById(R.id.tvProfileBodovi);
        profileLifes = (TextView)findViewById(R.id.tvProfileLifes);
        //textunlockedSculputres = (TextView)findViewById(R.id.tvUnlockedSculptures);
        atg = AnimationUtils.loadAnimation(this,R.anim.atg);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"FREESCPT.TTF");
        freeLance.setTypeface(typeface);
        story.setTypeface(typeface);
        premiumStory.setTypeface(typeface);
        collections.setTypeface(typeface);
        startAnimations();
    }

    private void startAnimations(){
        layoutFreeLance.startAnimation(atg);
        layoutStory.startAnimation(atg);
        layoutPremiumStory.setAnimation(atg);
        layoutCollections.setAnimation(atg);

    }

    /*private void showUnlockedSculptures() {
        String xmlString = new String();
        for (Sculpture sculpture : sculptures) {
            if (unlockedSculptures.contains(sculpture.name)) {
                xmlString = xmlString + sculpture.name + "\t is unlocked\n";
            }
        }
        textunlockedSculputres.setText(xmlString);
    }*/

    /*protected ArrayList<Sculpture> getAllSculptures() throws IOException, XmlPullParserException{
        XmlPullParserFactory parserFactory;
        XmlPullParser parser;
        parserFactory = XmlPullParserFactory.newInstance();
        parser = parserFactory.newPullParser();
        try{
            InputStream is = getAssets().open("sculptures.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
        }catch (IOException e){
        }
        ArrayList<Sculpture> sculpturesInXml = new ArrayList<>();
        int eventType = parser.getEventType();
        Sculpture currentSculpture = null;

        while(eventType != XmlPullParser.END_DOCUMENT){
            String eltName = null;
            switch (eventType){
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if("Sculpture".equals(eltName)){
                        currentSculpture = new Sculpture();
                        sculpturesInXml.add(currentSculpture);
                    }else if (currentSculpture != null){
                        if ("name".equals(eltName)){
                            currentSculpture.name = parser.nextText();
                        }else if ("imagePath".equals(eltName)){
                            currentSculpture.imagePath= parser.nextText();
                        }else if ("description".equals(eltName)){
                            currentSculpture.description = parser.nextText();
                        }else if ("latitude".equals(eltName)){
                            currentSculpture.latitude = parser.nextText();
                        }else if ("longtitude".equals(eltName)){
                            currentSculpture.longtitude = parser.nextText();
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        return sculpturesInXml;
    }*/

    /*    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mTimeLeftInMillis = prefs.getLong("millisLeft", START_TIME_IN_MILIS);
        mTimerRunning = prefs.getBoolean("timerRunning", false);

        updateGui();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateGui();
            } else {
                startTimer();
            }
        }
    }
    private void startTimer(){
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mTimeLeftInMillis = START_TIME_IN_MILIS;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                mTimerRunning = true;
                updateGui();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mTimeLeftInMillis = START_TIME_IN_MILIS;
            }
        }.start();
    }

    private void updateGui(){
        int minutes = (int) mTimeLeftInMillis / 1000 / 60;
        int seconds = (int) mTimeLeftInMillis / 1000 % 60;
        String timeLeftFormated = String.format(Locale.getDefault(), "%02d:%02d",minutes,seconds);
        tvuserLifes.setText(timeLeftFormated);
    }*/
}
