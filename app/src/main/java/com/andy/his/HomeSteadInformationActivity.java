package com.andy.his;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.andy.his.about.AboutActivity;
import com.andy.his.homestead.HomeSteadActivity;
import com.andy.his.module.ModuleActivity;

public class HomeSteadInformationActivity extends AppCompatActivity {

    ImageButton btnModule;
    ImageButton btnHomeStead;
    ImageButton btnSettings;
    ImageButton btnAbout;
    ImageButton btnExit;

    private PermissionHelper permissionHelper;

    public static String TAG = "HIS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 当系统为6.0以上时，需要申请权限
        permissionHelper = new PermissionHelper(this);
        permissionHelper.setOnApplyPermissionListener(new PermissionHelper.OnApplyPermissionListener()
        {
            @Override
            public void onAfterApplyAllPermission()
            {
                Log.i(TAG, "All of requested permissions has been granted, so run app logic.");
                runApp();
            }
        });
        if (Build.VERSION.SDK_INT < 23)
        {
            // 如果系统版本低于23，直接跑应用的逻辑
            Log.d(TAG, "The api level of system is lower than 23, so run app logic directly.");
            runApp();
        }
        else
        {
            // 如果权限全部申请了，那就直接跑应用逻辑
            if (permissionHelper.isAllRequestedPermissionGranted())
            {
                Log.d(TAG, "All of requested permissions has been granted, so run app logic directly.");
                runApp();
            }
            else
            {
                // 如果还有权限为申请，而且系统版本大于23，执行申请权限逻辑
                Log.i(TAG,
                        "Some of requested permissions hasn't been granted, so apply permissions first.");
                permissionHelper.applyPermissions();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void runApp() {

        ExitApplication.getInstance().addActivity(this);

        btnModule = findViewById(R.id.btnModule);
        btnModule.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeSteadInformationActivity.this, ModuleActivity.class);
                startActivity(intent);
            }
        });

        btnHomeStead = findViewById(R.id.btnHomeStead);
        btnHomeStead.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeSteadInformationActivity.this, HomeSteadActivity.class);
                startActivity(intent);
            }
        });

        btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(HomeSteadInformationActivity.this, getText(R.string.not_implements_module), Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(HomeSteadInformationActivity.this, HomeSteadActivity.class);
                //startActivity(intent);
            }
        });

        btnAbout = findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeSteadInformationActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(HomeSteadInformationActivity.this).create();
                alertDialog.setIcon(R.mipmap.ic_launcher);
                alertDialog.setTitle(R.string.exitDialogTitle);
                alertDialog.setMessage(getText(R.string.exitDialogMessage));

                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,getText(R.string.dialogNo),new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,getText(R.string.dialogYes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                       HomeSteadInformationHelper.deleteTempImageFiles();
                       ExitApplication.getInstance().exit();
                    }
                });
                alertDialog.show();
            }
        });
    }
}
