package com.andy.his.homestead;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.andy.his.ExitApplication;
import com.andy.his.R;

import java.io.File;

public class ImagePreviewActivity extends AppCompatActivity
{
	private ImageView previewView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_preview);
		ExitApplication.getInstance().addActivity(this);

		ImageButton btnBack = findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		previewView = (ImageView) findViewById(R.id.previewView);
		String filePath = getIntent().getStringExtra("filePath");
		if (filePath != null && !filePath.equals(""))
		{
			previewView.setImageURI(Uri.fromFile(new File(filePath)));
		}
	}

}
