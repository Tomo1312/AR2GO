package com.example.ar2go;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GestureDetectorCompat;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
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
    private ImageView info, tickets, events, loginScreen;
    private String eventMessage;
    private TextView freeLance, story, premiumStory, profileName, profileBodovi, collections, profileLifes;
    private ConstraintLayout layoutFreeLance, layoutStory, layoutPremiumStory, layoutCollections, allTheMainThings;
    private Animation atg, fadein;
    private FirebaseAuth firebaseAuth;
    private static int lifes, bodovi;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    protected static boolean mTimerRunning = false;
    protected static String unlockedSculptures, userName, userEmail;
    protected Boolean flag, internetConectivity;
    private GestureDetectorCompat gestureDetectorCompat = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        setUiVeiws();
        if (internetConectivity) {
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
                    profileBodovi.setText("= " + bodovi);
                    profileLifes.setText("=" + lifes);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(SecondActivity.this, "Couldn't connect to database", Toast.LENGTH_LONG).show();
                }
            });
            final DatabaseReference databaseReferenceOFEvents = firebaseDatabase.getReference("Events");
            databaseReferenceOFEvents.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    eventMessage = "";
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        MuseumEvent event = postSnapshot.getValue(MuseumEvent.class);
                        String eventName = event.getName();
                        String eventDescription = event.getDescription();
                        String eventDuration = event.getDuration();

                        eventMessage = eventMessage + eventName + "\n" + eventDuration + "\n" + eventDescription + "\n\n";
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(SecondActivity.this, "Couldn't connect to database", Toast.LENGTH_LONG).show();
                }
            });

            layoutFreeLance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SecondActivity.this, FreeLanceActivity.class));
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });

            layoutStory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SecondActivity.this, StoryActivity.class));
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
        } else {
            Toast.makeText(SecondActivity.this, "There is no Internet Conectivity!", Toast.LENGTH_LONG);
        }
        flag = displayGpsStatus();

    }

    protected Boolean checkIfNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //we are connected to a networkret
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).

                getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).

                        getState() == NetworkInfo.State.CONNECTED;
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
        return gpsStatus;
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

    private void eventBox() {

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
        loginScreen = findViewById(R.id.loginImage);

        info = findViewById(R.id.ivInfo);
        tickets = findViewById(R.id.ivTickets);
        events = findViewById(R.id.ivEvents);
        freeLance = findViewById(R.id.tvFreelance);
        story = findViewById(R.id.tvStory);
        premiumStory = findViewById(R.id.tvPremiumStory);
        collections = findViewById(R.id.tvColletions);
        time = findViewById(R.id.tvTime);

        allTheMainThings = findViewById(R.id.allMainStaff);
        layoutFreeLance = findViewById(R.id.layoutFreelance);
        layoutStory = findViewById(R.id.layoutStory);
        layoutPremiumStory = findViewById(R.id.layoutPremiumStory);
        layoutCollections = findViewById(R.id.layoutColletions);

        profileName = findViewById(R.id.tvProfileName);
        profileBodovi = findViewById(R.id.tvProfileBodovi);
        profileLifes = findViewById(R.id.tvProfileLifes);

        internetConectivity = checkIfNetworkAvailable();
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "FREESCPT.TTF");
        freeLance.setTypeface(typeface);
        story.setTypeface(typeface);
        premiumStory.setTypeface(typeface);
        collections.setTypeface(typeface);
        startAnimations();

        DetectSwipeGestureListener gestureListener = new DetectSwipeGestureListener();
        gestureListener.setActivitiy(this);
        gestureDetectorCompat = new GestureDetectorCompat(this, gestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        return true;
    }

    private void startAnimations() {
        loginScreen.startAnimation(fadein);
        fadein.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                loginScreen.setVisibility(View.INVISIBLE);
                allTheMainThings.setVisibility(View.VISIBLE);
                if (!flag) {
                    alertbox("Gps Status!!", "Your GPS is: OFF");
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        layoutFreeLance.startAnimation(atg);
        layoutStory.startAnimation(atg);
        layoutPremiumStory.setAnimation(atg);
        layoutCollections.setAnimation(atg);

    }

    protected void startFreelance(){
        startActivity(new Intent(SecondActivity.this, FreeLanceActivity.class));
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }
}
