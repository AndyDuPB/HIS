package com.andy.his.module;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.andreabaccega.widget.FormEditText;
import com.andy.his.ExitApplication;
import com.andy.his.R;
import com.andy.his.sql.HISDatabaseFactory;
import com.andy.his.sql.HISDatabaseHelper;

public class NewModuleActivity extends AppCompatActivity {

    Button btnSave;
    Button btnReset;

    FormEditText moduleCounty;
    FormEditText moduleTown;
    FormEditText moduleVillage;
    FormEditText moduleGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_module);
        ExitApplication.getInstance().addActivity(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                setResult(0);
                finish();
            }
        });

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                            boolean allValid = true;
                            FormEditText[] allFields = { moduleCounty, moduleTown, moduleVillage, moduleGroup };
                            for (FormEditText field: allFields) {
                                if(!field.testValidity())
                                {
                                    allValid = false;
                                    break;
                                }
                            }

                            if (allValid)
                            {
                                saveModule();
                            }
                            }
                        });
                    }
                }).start();
            }
        });

        initResetButton();
        initModuleView();
    }

    public void saveModule()
    {
        SQLiteDatabase writableDatabase = HISDatabaseFactory.getWritableSqLiteDatabase(getApplicationContext());
        writableDatabase.beginTransaction();
        try
        {
            ContentValues module = getModuleContentValues();
            long result = writableDatabase.insert(HISDatabaseHelper.TABLE_MODULE_NAME, null, module);
            writableDatabase.setTransactionSuccessful();
            if(result == -1L)
            {
                showErrorMessage();
            }
            else
            {
                showSuccessMessage();
                setResult(0);
                finish();
            }
        }
        catch(Exception ex)
        {
            showErrorMessage();
        }
        finally
        {
            writableDatabase.endTransaction();
            writableDatabase.close();
        }
    }

    private void showSuccessMessage() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(NewModuleActivity.this, getText(R.string.success_created_module), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showErrorMessage() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(NewModuleActivity.this, getText(R.string.failure_created_module), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initResetButton() {

        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                resetAllFields();
            }
        });
    }

    private void initModuleView() {

        moduleCounty = findViewById(R.id.moduleCounty);
        moduleTown = findViewById(R.id.moduleTown);
        moduleVillage = findViewById(R.id.moduleVillage);
        moduleGroup = findViewById(R.id.moduleGroup);
    }

    private ContentValues getModuleContentValues() {

        String moduleCountyStr = moduleCounty.getText().toString();
        String moduleTownStr = moduleTown.getText().toString();
        String moduleVillageStr = moduleVillage.getText().toString();
        String moduleGroupStr = moduleGroup.getText().toString();

        ContentValues module = new ContentValues();
        module.put("moduleCounty", moduleCountyStr);
        module.put("moduleTown", moduleTownStr);
        module.put("moduleVillage", moduleVillageStr);
        module.put("moduleGroup", moduleGroupStr);

        return module;
    }

    public void resetAllFields()
    {
        moduleGroup.setText("");
        moduleVillage.setText("");
        moduleTown.setText("");
        moduleCounty.setText("");

        moduleCounty.requestFocus();
    }
}
