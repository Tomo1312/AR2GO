package com.example.logindemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;

import static java.lang.String.valueOf;

public class SecondActivity extends AppCompatActivity {

    protected static TextView time;
    private ImageView info, tickets, events;
    private String eventMessage;
    private TextView freeLance, story, premiumStory, profileName, profileBodovi,collections, profileLifes;
    private ConstraintLayout layoutFreeLance, layoutStory, layoutPremiumStory, layoutCollections;
    private Animation atg;
    private FirebaseAuth firebaseAuth;
    private static int lifes, bodovi;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    protected static boolean mTimerRunning = false;
    protected static String unlockedSculptures, userName, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        setUiVeiws();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

        Boolean flag = displayGpsStatus();
        if(!flag){
            alertbox("Gps Status!!", "Your GPS is: OFF");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

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
}
