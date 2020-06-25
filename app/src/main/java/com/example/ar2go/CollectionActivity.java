package com.example.ar2go;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.bumptech.glide.load.engine.GlideException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
    protected static ArrayList<Sculpture> sculptures, arhitekture, spomenici;
    private TextView sculptureShown, arhitektureShown, spomeniciShown, profileBodovi, title;
    private ImageView imageSculpture,showToolbar, checkboxSculptures, checkboxArhitekture, checkboxSpomenici;
    private boolean toolbarShown, isSculptureShown, isArhitektureShown, isSpomeniciShown;
    private LinearLayout toolbarLayout;
    private Animation showToolbarAnimation, unshowToolbarAnimation;
    private ScrollView leftScrollView;
    private Typeface typeFace;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    @SuppressLint("SourceLockedOrientationActivity")
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
        profileBodovi =  findViewById(R.id.tvProfileBodovi);
        title = findViewById(R.id.tvTitle);
        linearLayout =  findViewById(R.id.scrollLinearLayout);
        info = findViewById(R.id.ivInfo);
        back = findViewById(R.id.ivBack);
        toolbarLayout = findViewById(R.id.toolbarLayout);
        showToolbar = findViewById(R.id.ivShowToolbar);
        leftScrollView = findViewById(R.id.scrollViewZaNazad);
        showToolbarAnimation = AnimationUtils.loadAnimation(this, R.anim.righttolefttoolbar);
        unshowToolbarAnimation = AnimationUtils.loadAnimation(this, R.anim.lefttorighttoolbar);
        arhitektureShown =  findViewById(R.id.showArhitekture);
        sculptureShown =  findViewById(R.id.showSculptures);
        spomeniciShown =findViewById(R.id.showSpomenici);
        checkboxArhitekture =  findViewById(R.id.checkboxArhitekture);
        checkboxSculptures =  findViewById(R.id.checkboxSculpureShown);
        checkboxSpomenici =  findViewById(R.id.checkboxSpomenici);
        imageSculpture = findViewById(R.id.sculptureImage);
        typeFace = Typeface.createFromAsset(getAssets(), "FREESCPT.TTF");
        title.setTypeface(typeFace);
        toolbarShown = false;
        isSculptureShown = true;
        isArhitektureShown = true;
        isSpomeniciShown = true;
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
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void showUnlockedSculptures(ArrayList<Sculpture> thing,final int thingBackground) {
        String xmlString = new String();
        linearLayout.setVerticalGravity(0);
        for (Sculpture sculpture : thing) {
            if (unlockedSculptures != null && unlockedSculptures.contains(sculpture.imagePath)) {
                final String nameSculpture = sculpture.name;
                final String imagePathSculpture = sculpture.imagePath;
                final String descriptionSculpture = sculpture.description;
                final String authorSculpture = sculpture.author;
                firebaseStorage = FirebaseStorage.getInstance();
                storageReference = firebaseStorage.getReference();

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(10, 0 , 20, 0);
                final RelativeLayout liner = new RelativeLayout(this);
                //liner.setOrientation(LinearLayout.HORIZONTAL);
                liner.setLayoutParams(params);
                liner.getLayoutParams().width = 400;
                liner.getLayoutParams().height = 400;
                final ImageView image = new ImageView(this);
                image.setLayoutParams(params);
                image.getLayoutParams().width= 400;

                final TextView tv = new TextView(this);
                RelativeLayout.LayoutParams tvparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                tvparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                tv.setLayoutParams(tvparams);
                try {
                    Resources res = getResources();
                    String mDrawableName = imagePathSculpture + ".jpg";
                    int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
                    storageReference.child("Images").child(mDrawableName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            try{
                                Glide.with(CollectionActivity.this).load(uri).centerCrop().into(image);
                            }catch (Exception ex){
                                liner.setBackground(getDrawable(thingBackground));//<-TREBA NACI GDJE DODATI DA STA NEMA SLIKU SE STAVI BOJA
                            }
//                            Picasso.get().load(uri).into(new Target() {
//                                @Override
//                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                    liner.setBackground(new BitmapDrawable(bitmap));
//                                }
//
//                                @Override
//                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//
//                                }
//
//                                @Override
//                                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                                }
//                            });
                        }
                    });
                } catch (Exception ex) {
                    Toast.makeText(getBaseContext(), "ex: " + ex, Toast.LENGTH_LONG).show();
                }

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
                liner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Resources res = getResources();
                            String mDrawableName = imagePathSculpture + ".jpg";
                            int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
                            storageReference.child("Images").child(mDrawableName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(CollectionActivity.this).load(uri).into(imageSculpture);
                                    //Picasso.get().load(uri).into(imageSculpture);
                                }
                            });
                        } catch (Exception ex) {
                            Toast.makeText(getBaseContext(), "ex: " + ex, Toast.LENGTH_LONG).show();
                        }
//                        Pop popSculpture = new Pop(nameSculpture, imagePathSculpture, descriptionSculpture, authorSculpture);
//                        popSculpture.setFirstTime(false);
//                        Intent i = new Intent(CollectionActivity.this, Pop.class);
//                        i.putExtra("currentSculpture", popSculpture);
//                        startActivity(i);
                    }
                });
                liner.addView(image);
                liner.addView(tv);
                linearLayout.addView(liner);
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
