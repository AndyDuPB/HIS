package com.andy.his.about;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.andy.his.HomeSteadInformationHelper;
import com.andy.his.ExitApplication;
import com.andy.his.R;

import java.text.MessageFormat;

public class AboutActivity extends AppCompatActivity {

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ExitApplication.getInstance().addActivity(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ((TextView)findViewById(R.id.aboutDataPath)).setText(MessageFormat.format(getResources().getString(R.string.aboutDataPath), HomeSteadInformationHelper.getAltitudeCameraBaseDir()));
    }
}
