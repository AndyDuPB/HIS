package com.andy.his.module;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.andy.his.ExitApplication;
import com.andy.his.R;
import com.andy.his.HomeSteadInformationHelper;
import com.andy.his.sql.HISDatabaseFactory;
import com.andy.his.sql.HISDatabaseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ModuleActivity extends AppCompatActivity {

    Button btnAdd;
    Button btnDelete;
    Button btnModify;
    Button btnRefresh;

    ListView moduleListView;
    ModuleAdapter moduleAdapter;

    private Map<ModuleDetail, Boolean> moduleCheckedMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);
        ExitApplication.getInstance().addActivity(this);

        this.moduleCheckedMap = new HashMap<ModuleDetail, Boolean>();
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAdd = findViewById(R.id.btnAdd);
        btnDelete = findViewById(R.id.btnDelete);
        btnModify = findViewById(R.id.btnModify);
        btnRefresh = findViewById(R.id.btnRefresh);

        disableDeleteAndModifyBtn();

        btnAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ModuleActivity.this, NewModuleActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(ModuleActivity.this).create();
                alertDialog.setIcon(R.mipmap.ic_launcher);
                alertDialog.setTitle(R.string.deleteModuleDialogTitle);
                alertDialog.setMessage(getText(R.string.deleteModuleDialogMessage));

                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,getText(R.string.dialogNo),new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,getText(R.string.dialogYes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                deleteSelectedModules();
                            }
                        }).start();
                    }
                });
                alertDialog.show();
            }
        });

        btnModify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ModuleActivity.this, ModifyModuleActivity.class);
                intent.putExtra("moduleDetail", moduleAdapter.getModuleDetailByID(getCheckedModuleID()));
                startActivityForResult(intent, 1);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                reloadModules();
            }
        });

        moduleListView = (ListView) findViewById(R.id.module_item_list);
        final LayoutInflater inflater = LayoutInflater.from(ModuleActivity.this);
        View headView = inflater.inflate(R.layout.module_headers, null, false);
        moduleListView.addHeaderView(headView);

        reloadModules();
    }

    private void deleteSelectedModules() {

        ArrayList<String> moduleIDList = new ArrayList<String>();
        ArrayList<String> moduleDirectoryList = new ArrayList<String>();

        Set<ModuleDetail> keySet = moduleCheckedMap.keySet();
        Iterator<ModuleDetail> iterator = keySet.iterator();
        while (iterator.hasNext()){
            ModuleDetail key = iterator.next();
            if(moduleCheckedMap.get(key).booleanValue())
            {
                moduleIDList.add(String.valueOf(key.getModuleID()));
                moduleDirectoryList.add(HomeSteadInformationHelper.getModuleInfoDirectoryPath(key));
            }
        }

        String[] sqlInParameters = getStringForSQLInParameter(moduleIDList);
        if(sqlInParameters.length > 0)
        {
            deleteModules(sqlInParameters, moduleDirectoryList);
        }
    }

    private void deleteModules(String[] sqlInParameters, ArrayList<String> moduleDirectoryList) {

        SQLiteDatabase writableDatabase = HISDatabaseFactory.getWritableSqLiteDatabase(getApplicationContext());
        writableDatabase.beginTransaction();
        int result = 0;
        try
        {
            String whereClause = getWhereClause(sqlInParameters);
            result += writableDatabase.delete(HISDatabaseHelper.TABLE_HOMESTEAD_NAME, whereClause, sqlInParameters);
            result += writableDatabase.delete(HISDatabaseHelper.TABLE_MODULE_NAME, whereClause, sqlInParameters);
            writableDatabase.setTransactionSuccessful();

            if(result <= 0)
            {
                showErrorMessage();
            }
            else
            {
                showSuccessMessageAndUpdateUI(moduleDirectoryList);
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

    private void showSuccessMessageAndUpdateUI(ArrayList<String> moduleDirectoryList) {

        for (String moduleBasePath : moduleDirectoryList)
        {
            HomeSteadInformationHelper.deleteDirectory(new File(moduleBasePath));
        }

        runOnUiThread(new Runnable() {

            @Override

            public void run() {
                Toast.makeText(ModuleActivity.this, getText(R.string.success_deleted_module), Toast.LENGTH_SHORT).show();
                reloadModules();
            }
        });
    }

    private void showErrorMessage() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(ModuleActivity.this, getText(R.string.failure_deleted_module), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getWhereClause(String[] sqlInParameters) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("moduleID IN( ");
        for (int index = 0; index < sqlInParameters.length; index++)
        {
            buffer.append("?");
            if(index < sqlInParameters.length - 1){
                buffer.append(", ");
            }
        }
        buffer.append(" ) ");

        return buffer.toString();
    }

    public String[] getStringForSQLInParameter(ArrayList<String> values){

         String[] parameters = new String[values.size()];
         for(int index = 0; index < values.size(); index++){
             parameters[index] = values.get(index);
         }

         return parameters;
    }

    private ArrayList<ModuleDetail> getModuleDetails() {

        ArrayList<ModuleDetail> moduleDetails = new ArrayList<ModuleDetail>();
        SQLiteDatabase readableDatabase = HISDatabaseFactory.getReadableSqLiteDatabase(getApplicationContext());
        try
        {
            Cursor cursor = readableDatabase.rawQuery("SELECT moduleID, moduleCounty, moduleTown, moduleVillage, moduleGroup from MODULE ",new String[]{});
            if(cursor != null && cursor.getCount() > 0){
                while (cursor.moveToNext()){
                    ModuleDetail bean = new ModuleDetail();
                    bean.setModuleID(cursor.getInt(0));
                    bean.setModuleCounty(cursor.getString(1));
                    bean.setModuleTown(cursor.getString(2));
                    bean.setModuleVillage(cursor.getString(3));
                    bean.setModuleGroup(cursor.getString(4));
                    moduleDetails.add(bean);
                }
                cursor.close();
            }
            Toast.makeText(ModuleActivity.this, getText(R.string.success_query_module), Toast.LENGTH_SHORT).show();
        }
        catch(Exception ex)
        {
            Toast.makeText(ModuleActivity.this, getText(R.string.failure_query_module), Toast.LENGTH_SHORT).show();
        }
        finally
        {
            readableDatabase.close();
        }

        return moduleDetails;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //For Add Module Operation / Modify Module Operation
        if((requestCode == 0 || requestCode == 1) && resultCode == 0)
        {
            reloadModules();
        }
    }

    private void reloadModules() {

        this.moduleCheckedMap.clear();
        disableDeleteAndModifyBtn();

        ArrayList<ModuleDetail> moduleDetails = getModuleDetails();
        moduleAdapter = new ModuleAdapter(moduleDetails, ModuleActivity.this);
        moduleListView.setAdapter(moduleAdapter);
        HomeSteadInformationHelper.setListViewHeightBasedOnChildren(moduleListView);
    }

    private void disableDeleteAndModifyBtn() {

        enableOrDisableDeleteBtn(false);
        enableOrDisableModifyBtn(false);
    }

    public void putCheckboxStatus(ModuleDetail detail, Boolean value)
    {
        moduleCheckedMap.put(detail, value);
        int checkedNumber = getCheckedNumber();
        if(checkedNumber == 0)
        {
            disableDeleteAndModifyBtn();
        }
        else if (checkedNumber == 1)
        {
            enableOrDisableDeleteBtn(true);
            enableOrDisableModifyBtn(true);
        }
        else {
            disableDeleteAndModifyBtn();
            enableOrDisableDeleteBtn(true);
        }
    }

    private int getCheckedNumber() {

        int number = 0;
        Collection<Boolean> values = moduleCheckedMap.values();
        for (Boolean val : values)
        {
            if(val.booleanValue())
            {
                number ++;
            }
        }

        return number;
    }

    private Integer getCheckedModuleID() {

        Set<Map.Entry<ModuleDetail, Boolean>> entrySet = moduleCheckedMap.entrySet();
        for (Map.Entry<ModuleDetail, Boolean> entry : entrySet)
        {
            if(entry.getValue().booleanValue())
            {
                return entry.getKey().getModuleID();
            }
        }

        return null;
    }

    private void enableOrDisableDeleteBtn(boolean b) {

        btnDelete.setEnabled(b);
    }

    private void enableOrDisableModifyBtn(boolean b) {

        btnModify.setEnabled(b);
    }
}
