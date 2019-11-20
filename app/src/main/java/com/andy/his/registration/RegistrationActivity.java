package com.andy.his.registration;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.andreabaccega.widget.FormEditText;
import com.andy.his.ExitApplication;
import com.andy.his.HomeSteadInformationActivity;
import com.andy.his.HomeSteadInformationHelper;
import com.andy.his.PermissionHelper;
import com.andy.his.R;
import com.andy.his.license.AndroidDeviceInfoCollector;
import com.andy.his.license.Base64Utils;
import com.andy.his.license.LicenseParser;

public class RegistrationActivity extends AppCompatActivity {


    EditText deviceInfo;

    FormEditText registrationCode;

    Button btnRegistration;

    Button btnReset;

    private PermissionHelper permissionHelper;

    public static String TAG = "HIS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

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

    private void runApp() {

        try
        {
            String licenseNumber = Base64Utils.encodeFile(HomeSteadInformationHelper.getLicenseFilePath());
            if(true || LicenseParser.validationLicenseFile(licenseNumber, this))
            {
                startActivity(new Intent(RegistrationActivity.this, HomeSteadInformationActivity.class));
                finish();
            }
            else {
                ExitApplication.getInstance().addActivity(this);

                deviceInfo = findViewById(R.id.deviceInfo);
                registrationCode = findViewById(R.id.registrationCode);

                deviceInfo.setText(AndroidDeviceInfoCollector.getLicenseSerialNumber(this));
                //deviceInfo.setEnabled(false);

                btnRegistration = findViewById(R.id.btnRegistration);
                btnRegistration.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        new Thread(new Runnable() {

                            @Override
                            public void run() {

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        boolean allValid = true;
                                        if(!registrationCode.testValidity())
                                        {
                                            allValid = false;
                                        }

                                        if (allValid)
                                        {
                                            registration();
                                        }
                                    }
                                });
                            }
                        }).start();
                    }
                });

                btnReset = findViewById(R.id.btnReset);
                btnReset.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        reset();
                    }
                });

                showLicenseUnValidationMessage();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reset() {

        registrationCode.setText("");
        registrationCode.requestFocus();
    }

    private void registration()
    {
        if(LicenseParser.validationLicenseFile(registrationCode.getText().toString(), RegistrationActivity.this))
        {
            startActivity(new Intent(RegistrationActivity.this, HomeSteadInformationActivity.class));
            finish();
        }
        else {
            showLicenseUnValidationMessage();
        }
    }

    private void showLicenseUnValidationMessage() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Toast.makeText(RegistrationActivity.this, getText(R.string.failed_validation_registration_code), Toast.LENGTH_SHORT).show();
                reset();
            }
        });
    }
}
