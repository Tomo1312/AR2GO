package com.example.ar2go;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.regex.Pattern;

public class YearsActivity extends AppCompatActivity {

    LinearLayout scrollLinear;
    ImageView back;
    Typeface typeFace;
    TextView naslov;
    String story;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_years);

        Intent i = getIntent();
        story = i.getStringExtra("story");
        setUiView();
        findYearsInAssets();
        menuBar();
    }

    private void setUiView() {
        scrollLinear = findViewById(R.id.scrollLinear);
        back = findViewById(R.id.ivBack);
        typeFace = Typeface.createFromAsset(getAssets(), "FREESCPT.TTF");
        naslov = findViewById(R.id.TVGodine);
        naslov.setText(toTitleCase(story));
    }

    public static String toTitleCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void findYearsInAssets() {
        String[] list;
        try {
            list = getAssets().list(story);
            if (list.length > 0) {
                for (String file : list) {
                    final String fileName = file;//^\d\d\d\d
                    TextView tv = new TextView(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(10, 60, 10, 80);
                    tv.setLayoutParams(params);
                    tv.setPadding(40, 40, 0, 40);
                    tv.setTextSize(18);
                    if (story.equals("years")) {
                        tv.setText(file.replaceFirst("[.][^.]+$", ""));
                        tv.setBackground(getDrawable(R.drawable.backgroundcollectionsculptures));
                    }else{
                        String first = file;
                        String second = first.replaceFirst("[.][^.]+$", "");
                        String third = second.replace("_", " ");
                        tv.setText(toTitleCase(third));
                        tv.setBackground(getDrawable(R.drawable.backgroundcollectionspomenici));
                    }
                    tv.setTextColor(Color.parseColor("#424242"));
                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    tv.setVisibility(View.VISIBLE);
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(YearsActivity.this, FinalYearActivity.class);
                            i.putExtra("filename", fileName);
                            startActivity(i);
                        }
                    });
                    scrollLinear.addView(tv);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
