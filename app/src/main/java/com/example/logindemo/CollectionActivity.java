package com.example.logindemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static java.lang.String.valueOf;

public class CollectionActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private ImageView info, back;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    protected static String unlockedSculptures;
    protected static ArrayList<Sculpture> sculptures, arhitekture, spomenici, fontane;
    private TextView sculptureShown, arhitektureShown, spomeniciShown, fontaneShown, profileBodovi, title;
    private ImageView showToolbar, checkboxSculptures, checkboxArhitekture, checkboxSpomenici, checkboxFontane;
    private boolean toolbarShown, isSculptureShown, isArhitektureShown, isSpomeniciShown, isFontaneShown;
    private LinearLayout toolbarLayout;
    private Animation showToolbarAnimation, unshowToolbarAnimation;
    private ScrollView leftScrollView;
    private Typeface typeFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        //if you want to lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setUiView();
        getAllThingsForMap();

        unlockedSculptures = new String();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                profileBodovi.setText("=" + valueOf(userProfile.getUserBodovi()));
                unlockedSculptures = userProfile.getOtkljucaneSkulputre();
                setShowAllArtToCollection();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CollectionActivity.this, "Couldn't connect to database", Toast.LENGTH_LONG).show();
            }
        });

        menuBar();

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo();
            }
        });
    }

    private void setUiView() {
        profileBodovi = (TextView) findViewById(R.id.tvProfileBodovi);
        title = (TextView) findViewById(R.id.tvTitle);
        linearLayout = (LinearLayout) findViewById(R.id.scrollLinearLayout);
        info = (ImageView) findViewById(R.id.ivInfo);
        back = (ImageView) findViewById(R.id.ivBack);
        toolbarLayout = (LinearLayout) findViewById(R.id.toolbarLayout);
        showToolbar = (ImageView) findViewById(R.id.ivShowToolbar);
        leftScrollView = (ScrollView) findViewById(R.id.scrollViewZaNazad);
        showToolbarAnimation = AnimationUtils.loadAnimation(this, R.anim.righttoleft);
        unshowToolbarAnimation = AnimationUtils.loadAnimation(this, R.anim.lefttoright);
        arhitektureShown = (TextView) findViewById(R.id.showArhitekture);
        sculptureShown = (TextView) findViewById(R.id.showSculptures);
        spomeniciShown = (TextView) findViewById(R.id.showSpomenici);
        fontaneShown = (TextView) findViewById(R.id.showFontane);
        checkboxArhitekture = (ImageView) findViewById(R.id.checkboxArhitekture);
        checkboxSculptures = (ImageView) findViewById(R.id.checkboxSculpureShown);
        checkboxSpomenici = (ImageView) findViewById(R.id.checkboxSpomenici);
        checkboxFontane = (ImageView) findViewById(R.id.checkboxFontante);
        typeFace = Typeface.createFromAsset(getAssets(), "FREESCPT.TTF");
        title.setTypeface(typeFace);
        toolbarShown = false;
        isSculptureShown = true;
        isArhitektureShown = true;
        isSpomeniciShown = true;
        isFontaneShown = true;
    }

    private void menuBar() {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back.setBackgroundColor(Color.parseColor("#55000000"));
                finish();
            }
        });

        showToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!toolbarShown) {
                    toolbarShown = true;
                    toolbarLayout.startAnimation(showToolbarAnimation);
                    showToolbarAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            toolbarLayout.setVisibility(View.VISIBLE);
                            leftScrollView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    toolbarShown = false;
                    toolbarLayout.startAnimation(unshowToolbarAnimation);
                    unshowToolbarAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            toolbarLayout.setVisibility(View.INVISIBLE);
                            leftScrollView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });

        spomeniciShown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSpomeniciShown) {
                    isSpomeniciShown = false;
                    checkboxSpomenici.setVisibility(View.INVISIBLE);
                    setShowAllArtToCollection();
                } else {
                    isSpomeniciShown = true;
                    checkboxSpomenici.setVisibility(View.VISIBLE);
                    setShowAllArtToCollection();
                }
            }
        });

        sculptureShown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSculptureShown) {
                    isSculptureShown = false;
                    checkboxSculptures.setVisibility(View.INVISIBLE);
                    setShowAllArtToCollection();
                } else {
                    isSculptureShown = true;
                    checkboxSculptures.setVisibility(View.VISIBLE);
                    setShowAllArtToCollection();
                }
            }
        });

        arhitektureShown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isArhitektureShown) {
                    isArhitektureShown = false;
                    checkboxArhitekture.setVisibility(View.INVISIBLE);
                    setShowAllArtToCollection();
                } else {
                    isArhitektureShown = true;
                    checkboxArhitekture.setVisibility(View.VISIBLE);
                    setShowAllArtToCollection();
                }
            }
        });

        fontaneShown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFontaneShown) {
                    isFontaneShown = false;
                    checkboxFontane.setVisibility(View.INVISIBLE);
                    setShowAllArtToCollection();
                } else {
                    isFontaneShown = true;
                    checkboxFontane.setVisibility(View.VISIBLE);
                    setShowAllArtToCollection();
                }
            }
        });

    }

    private void setShowAllArtToCollection() {
        linearLayout.removeAllViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isSculptureShown) {
                showUnlockedSculptures(sculptures, R.drawable.backgroundcollectionsculptures);
            }
            if (isSpomeniciShown) {
                showUnlockedSculptures(spomenici, R.drawable.backgroundcollectionspomenici);
            }
            if (isArhitektureShown) {
                showUnlockedSculptures(arhitekture, R.drawable.backgroundcollectionarhitekture);
            }
            if (isFontaneShown) {
                showUnlockedSculptures(fontane, R.drawable.backgroundcollectionfontane);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void showUnlockedSculptures(ArrayList<Sculpture> thing, int thingBackground) {
        String xmlString = new String();
        linearLayout.setVerticalGravity(0);
        for (Sculpture sculpture : thing) {
            if (unlockedSculptures != null && unlockedSculptures.contains(sculpture.imagePath)) {
                final String nameSculpture = sculpture.name;
                final String imagePathSculpture = sculpture.imagePath;
                final String descriptionSculpture = sculpture.description;
                final String authorSculpture = sculpture.author;
                TextView tv = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tv.setLayoutParams(params);
                tv.setPadding(40, 40, 0, 40);
                tv.setVisibility(View.VISIBLE);
                tv.setText(sculpture.name);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setTextSize(18);
                tv.setTextColor(Color.parseColor("#424242"));
                tv.setBackground(getDrawable(thingBackground));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Pop popSculpture = new Pop(nameSculpture, imagePathSculpture, descriptionSculpture, authorSculpture);
                        popSculpture.setFirstTime(false);
                        Intent i = new Intent(CollectionActivity.this, Pop.class);
                        i.putExtra("currentSculpture", popSculpture);
                        startActivity(i);
                    }
                });
                linearLayout.addView(tv);
            }
        }
        //textunlockedSculputres.setText(xmlString);
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

    @NonNull
    private void getAllThingsForMap() {
        try {
            sculptures = getXmlFiles("sculptures.xml", "Sculpture");
            spomenici = getXmlFiles("spomenici.xml", "Spomenik");
            arhitekture = getXmlFiles("arhitekture.xml", "Arhitektura");
            fontane = getXmlFiles("fontane.xml", "Fontana");
        } catch (XmlPullParserException ex) {
            Toast.makeText(CollectionActivity.this, "No data in XML", Toast.LENGTH_LONG).show();
        } catch (IOException ex) {
            Toast.makeText(CollectionActivity.this, "Couldn't open XML", Toast.LENGTH_LONG).show();
        }
    }

    private ArrayList<Sculpture> getXmlFiles(String xmlFile, String firstTag) throws IOException, XmlPullParserException {
        XmlPullParserFactory parserFactory;
        XmlPullParser parser;
        parserFactory = XmlPullParserFactory.newInstance();
        parser = parserFactory.newPullParser();
        try {
            InputStream is = getAssets().open(xmlFile);
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
