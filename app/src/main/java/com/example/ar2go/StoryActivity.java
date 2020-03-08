package com.example.ar2go;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class StoryActivity extends AppCompatActivity {
    private Button year, author;
    private Animation leftToRight, rightToLeft;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        year = (Button) findViewById(R.id.btnStoryYears);
        author = (Button) findViewById(R.id.btnStoryAuthors);
        back = (ImageView) findViewById(R.id.ivBack);

        leftToRight = AnimationUtils.loadAnimation(this, R.anim.lefttoright);
        rightToLeft = AnimationUtils.loadAnimation(this, R.anim.righttoleft);

        year.startAnimation(leftToRight);
        author.startAnimation(rightToLeft);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back.setBackgroundColor(Color.parseColor("#55000000"));
                finish();
            }
        });
    }
}
