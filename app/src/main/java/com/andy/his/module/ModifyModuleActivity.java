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

public class ModifyModuleActivity extends AppCompatActivity {

    Button btnSave;
    Button btnReset;

    FormEditText moduleCounty;
    FormEditText moduleTown;
    FormEditText moduleVillage;
    FormEditText moduleGroup;

    ModuleDetail moduleDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_module);
        ExitApplication.getInstance().addActivity(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                setResult(0);
                finish();
            }
        });

        moduleDetail = (ModuleDetail)getIntent().getSerializableExtra("moduleDetail");

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
                                    updateModule();
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        initRestButton();
        initModuleView();
    }

    public void updateModule()
    {
        SQLiteDatabase writableDatabase = HISDatabaseFactory.getWritableSqLiteDatabase(getApplicationContext());
        writableDatabase.beginTransaction();
        try
        {
            ContentValues module = getModuleContentValues();
            int result = writableDatabase.update(HISDatabaseHelper.TABLE_MODULE_NAME, module," moduleID = ? ", new String[]{ String.valueOf(moduleDetail.getModuleID()) });
            writableDatabase.setTransactionSuccessful();
            if(result == 1)
            {
                showSuccessMessage();
                setResult(0);
                finish();
            }
            else
            {
                showErrorMessage();
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
                Toast.makeText(ModifyModuleActivity.this, getText(R.string.success_updated_module), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showErrorMessage() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(ModifyModuleActivity.this, getText(R.string.failure_updated_module), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initRestButton() {

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

        moduleCounty.setText(moduleDetail.getModuleCounty());
        moduleTown.setText(moduleDetail.getModuleTown());
        moduleVillage.setText(moduleDetail.getModuleVillage());
        moduleGroup.setText(moduleDetail.getModuleGroup());

        moduleCounty.requestFocus();
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
