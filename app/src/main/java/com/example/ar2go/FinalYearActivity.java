package com.example.ar2go;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FinalYearActivity extends AppCompatActivity {


    private LinearLayout linearLayout;
    private ImageView info, back, imageSculpture;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    protected static String unlockedSculptures;
    protected static ArrayList<Sculpture> art;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        //if you want to lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        setUiView();
        menuBar();

        Intent i = getIntent();
        String filename = i.getStringExtra("filename");
        try {
            art = getXmlFiles(filename, "Art");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        unlockedSculptures = "";
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                unlockedSculptures = userProfile.getOtkljucaneSkulputre();
                showUnlockedSculptures(art);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FinalYearActivity.this, "Couldn't connect to database", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setUiView() {
        linearLayout = findViewById(R.id.scrollLinearLayout);
        info = findViewById(R.id.ivInfo);
        back = findViewById(R.id.ivBack);
        imageSculpture = findViewById(R.id.sculptureImage);
    }

    private void menuBar() {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back.setBackgroundColor(Color.parseColor("#55000000"));
                finish();
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void showUnlockedSculptures(ArrayList<Sculpture> thing) {
        linearLayout.setVerticalGravity(0);
        for (final Sculpture sculpture : thing) {
                final String imagePathSculpture = sculpture.imagePath;
                firebaseStorage = FirebaseStorage.getInstance();
                storageReference = firebaseStorage.getReference();

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(10, 0, 20, 0);
                final RelativeLayout liner = new RelativeLayout(this);
                liner.setLayoutParams(params);
                liner.getLayoutParams().width = 400;
                liner.getLayoutParams().height = 400;
                final ImageView image = new ImageView(this);
                image.setLayoutParams(params);
                image.getLayoutParams().width = 400;

                final TextView tv = new TextView(this);
                RelativeLayout.LayoutParams tvparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                tvparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                tv.setLayoutParams(tvparams);
                final String TAG = FinalYearActivity.class.getSimpleName();
                Log.i(TAG, "UNLOCKED SCULPTURES: " + unlockedSculptures);
                if (unlockedSculptures != null && unlockedSculptures.contains(sculpture.imagePath)) {
                    try {
                        Resources res = getResources();
                        String mDrawableName = imagePathSculpture + ".jpg";
                        int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
                        storageReference.child("Images").child(mDrawableName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                try {
                                    Glide.with(FinalYearActivity.this).load(uri).centerCrop().into(image);
                                } catch (Exception ex) {
                                    //liner.setBackground(getDrawable(thingBackground));//<-TREBA NACI GDJE DODATI DA STA NEMA SLIKU SE STAVI BOJA
                                }
                            }
                        });
                        tv.setPadding(40, 0, 40, 0);
                        tv.setVisibility(View.VISIBLE);
                        tv.setText(sculpture.name);
                        tv.setGravity(Gravity.BOTTOM);
                        tv.setHeight(100);
                        tv.setTextColor(getResources().getColor(R.color.white));
                        tv.setBackgroundResource(R.color.black);
                        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        tv.setTextSize(14);
                        tv.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    } catch (Exception ex) {
                        Toast.makeText(getBaseContext(), "ex: " + ex, Toast.LENGTH_LONG).show();
                    }

                    liner.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            info.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showInfo(sculpture.name, sculpture.description, sculpture.author);
                                }
                            });
                            try {
                                Resources res = getResources();
                                String mDrawableName = imagePathSculpture + ".jpg";
                                int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
                                storageReference.child("Images").child(mDrawableName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(FinalYearActivity.this).load(uri).into(imageSculpture);
                                    }
                                });
                            } catch (Exception ex) {
                                Toast.makeText(getBaseContext(), "ex: " + ex, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    Glide.with(FinalYearActivity.this).load("https://imageog.flaticon.com/icons/png/512/36/36601.png?size=1200x630f&pad=10,10,10,10&ext=png&bg=FFFFFFFF").centerCrop().into(image);

                }
                liner.addView(image);
                liner.addView(tv);
                linearLayout.addView(liner);
        }
    }

    private void showInfo(String Name, String Description, String Author) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Description + "\n" + Author)
                .setCancelable(false)
                .setTitle(Name)
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

    private ArrayList<Sculpture> getXmlFiles(String xmlFile, String firstTag) throws IOException, XmlPullParserException {
        XmlPullParserFactory parserFactory;
        XmlPullParser parser;
        parserFactory = XmlPullParserFactory.newInstance();
        parser = parserFactory.newPullParser();
        try {
            InputStream is = getAssets().open("years/" + xmlFile);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
        } catch (IOException e) {
        }
        ArrayList<Sculpture> namesInXml = new ArrayList<>();
        int eventType = parser.getEventType();
        Sculpture currentSculpture = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String eltName = null;
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if (firstTag.equals(eltName)) {
                        currentSculpture = new Sculpture();
                        namesInXml.add(currentSculpture);
                    } else if (currentSculpture != null) {
                        if ("name".equals(eltName)) {
                            currentSculpture.name = parser.nextText();
                        } else if ("description".equals(eltName)) {
                            currentSculpture.description = parser.nextText();
                        } else if ("author".equals(eltName)) {
                            currentSculpture.author = parser.nextText();
                        } else if ("imagePath".equals(eltName)) {
                            currentSculpture.imagePath = parser.nextText();
                        } else if ("points".equals(eltName)) {
                            currentSculpture.points = parser.nextText();
                        } else if ("latitude".equals(eltName)) {
                            currentSculpture.latitude = parser.nextText();
                        } else if ("longitude".equals(eltName)) {
                            currentSculpture.longitude = parser.nextText();
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        return namesInXml;
    }
}
