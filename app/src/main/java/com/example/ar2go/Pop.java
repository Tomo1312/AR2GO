package com.example.ar2go;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.Serializable;

public class Pop extends Activity implements Serializable {

    public String nameOfSculpture, descriptionOfSculpture, imageLocationOfSculpture, authorOfSculpture;
    private ConstraintLayout firstLayout, secondLayout;
    private TextView nameSculpture, destripticonSculpture, authorSculpture, cestitamo, naisli;
    private Button showSculpture;
    private ImageView imageSculpture, conffeti, close;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private boolean firstTime;
    private Animation confetiAnimation, atg, fadein;

    public Pop() {
    }

    public Pop(String NameOfSculpture, String ImageLocationOfSculpture, String DescriptionOfSculpture,
               String AuthorOfSculpture) {
        this.nameOfSculpture = NameOfSculpture;
        this.imageLocationOfSculpture = ImageLocationOfSculpture;
        this.descriptionOfSculpture = DescriptionOfSculpture;
        this.authorOfSculpture = AuthorOfSculpture;
    }

    public String getNameOfSculpture() {
        return nameOfSculpture;
    }

    public void setNameOfSculpture(String nameOfSculpture) {
        this.nameOfSculpture = nameOfSculpture;
    }

    public String getImageLocationOfSculpture() {
        return imageLocationOfSculpture;
    }

    public void setImageLocationOfSculpture(String imageLocationOfSculpture) {
        this.imageLocationOfSculpture = imageLocationOfSculpture;
    }

    public String getDescriptionOfSculpture() {
        return descriptionOfSculpture;
    }

    public void setDescriptionOfSculpture(String descriptionOfSculpture) {
        this.descriptionOfSculpture = descriptionOfSculpture;
    }

    public String getAuthorOfSculpture() {
        return authorOfSculpture;
    }

    public void setAuthorOfSculpture(String authorOfSculpture) {
        this.authorOfSculpture = authorOfSculpture;
    }

    public void setFirstTime(boolean time) {
        this.firstTime = time;
    }

    public Boolean getFirstTime() {
        return firstTime;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sculpturepopup);

        //if you want to lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setUiViews();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        Intent i = getIntent();
        Pop popSculpture = (Pop) i.getSerializableExtra("currentSculpture");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        storageReference = firebaseStorage.getReference();

        try {
            Resources res = getResources();
            String mDrawableName = popSculpture.getImageLocationOfSculpture() + ".jpg";
            int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
            storageReference.child("Images").child(mDrawableName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(imageSculpture);
                }
            });
            nameSculpture.setText(popSculpture.getNameOfSculpture());
            authorSculpture.setText(popSculpture.getAuthorOfSculpture());
            destripticonSculpture.setText(popSculpture.getDescriptionOfSculpture());
        } catch (Exception ex) {
            Toast.makeText(getBaseContext(), "ex: " + ex, Toast.LENGTH_LONG).show();
        }

        if (popSculpture.getFirstTime()) {
            conffeti.startAnimation(confetiAnimation);
            naisli.setAnimation(atg);
            showSculpture.startAnimation(atg);
            conffeti.animate().alpha(0f).setDuration(600).setStartDelay(100).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    conffeti.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            showSculpture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstLayout.animate().alpha(0f).setDuration(300).setStartDelay(200).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            firstLayout.setVisibility(View.GONE);
                            secondLayout.startAnimation(fadein);
                            fadein.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    secondLayout.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                }
            });
        } else {
            firstLayout.setVisibility(View.INVISIBLE);
            secondLayout.startAnimation(fadein);
            fadein.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    secondLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getWindow().setLayout((int) (width * .7), (int) (height * .8));
    }

    private void setUiViews() {

        nameSculpture = findViewById(R.id.tvSculptureName);
        authorSculpture = findViewById(R.id.tvSculptureAuthor);
        destripticonSculpture = findViewById(R.id.tvSculptureDescription);
        imageSculpture = findViewById(R.id.imageSculpture);
        conffeti = findViewById(R.id.conffeti);
        naisli = findViewById(R.id.tvNaisli);
        showSculpture = findViewById(R.id.btnShowSculpture);
        firstLayout = findViewById(R.id.firstCard);
        secondLayout = findViewById(R.id.secondCard);
        confetiAnimation = AnimationUtils.loadAnimation(this, R.anim.confetianimation);
        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        close = findViewById(R.id.ivClose);
    }

}
