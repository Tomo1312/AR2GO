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

import com.google.common.io.LineReader;

import java.io.IOException;
import java.time.Year;
import java.util.regex.Pattern;

public class YearsActivity extends AppCompatActivity {

    LinearLayout scrollLinear;
    ImageView back;
    Typeface typeFace;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_years);
        setUiView();
        findYearsInAssets();
    }

    private void setUiView() {
        scrollLinear = findViewById(R.id.scrollLinear);
        back = findViewById(R.id.ivBack);
        typeFace = Typeface.createFromAsset(getAssets(), "FREESCPT.TTF");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void findYearsInAssets() {
        String[] list;
        try {
            list = getAssets().list("years");
            if (list.length > 0) {
                for (String file : list) {
                    Toast.makeText(this,file, Toast.LENGTH_LONG);
                    if (Pattern.matches("^\\d\\d\\d\\d.*", file)) {
                        final String fileName = file;//^\d\d\d\d
                        TextView tv = new TextView(this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(10,60,10,80);
                        tv.setLayoutParams(params);
                        tv.setPadding(40, 40, 0, 40);
                        tv.setTextSize(18);
                        tv.setText(file.replaceFirst("[.][^.]+$", ""));
                        tv.setTextColor(Color.parseColor("#424242"));
                        tv.setBackground(getDrawable(R.drawable.backgroundcollectionsculptures));
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
