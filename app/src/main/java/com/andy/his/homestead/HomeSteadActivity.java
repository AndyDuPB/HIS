package com.andy.his.homestead;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.andy.his.ExitApplication;
import com.andy.his.HomeSteadInformationHelper;
import com.andy.his.R;
import com.andy.his.module.ModuleSpinnerDetail;
import com.andy.his.sql.HISDatabaseFactory;
import com.andy.his.sql.HISDatabaseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeSteadActivity extends AppCompatActivity {

    Button btnAdd;
    Button btnDelete;
    Button btnModify;
    Button btnRefresh;

    ListView homeSteadItemView;
    HomeSteadAdapter homeSteadAdapter;

    Spinner moduleSpinner;

    private Map<HomeSteadTableDetail, Boolean> homeSteadCheckedMap;

    private static final int MODULE_SPINNER_ALL_ITEM = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_stead);
        ExitApplication.getInstance().addActivity(this);

        this.homeSteadCheckedMap = new HashMap<HomeSteadTableDetail, Boolean>();
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

                Intent intent = new Intent(HomeSteadActivity.this, NewHomeSteadActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(HomeSteadActivity.this).create();
                alertDialog.setIcon(R.mipmap.ic_launcher);
                alertDialog.setTitle(R.string.deleteHomesteadDialogTitle);
                alertDialog.setMessage(getText(R.string.deleteHomesteadDialogMessage));

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

                                deleteSelectedHomeSteads();
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

                Intent intent = new Intent(HomeSteadActivity.this, ModifyHomeSteadActivity.class);
                HomeSteadTableDetail checkedKey = getCheckedHomeSteadKey();
                intent.putExtra("moduleID", String.valueOf(checkedKey.getModuleID()));
                intent.putExtra("homeSteadID", checkedKey.getHomeSteadID());
                startActivityForResult(intent, 1);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                initHomeSteadUI();
            }
        });

        initModuleSpinnerView();

        homeSteadItemView = (ListView) findViewById(R.id.homestead_item_list);
        final LayoutInflater inflater = LayoutInflater.from(HomeSteadActivity.this);
        View headView = inflater.inflate(R.layout.homestead_headers, null, false);
        homeSteadItemView.addHeaderView(headView);

        initHomeSteadUI();
    }

    private void initModuleSpinnerView() {

        moduleSpinner = (Spinner) findViewById(R.id.moduleInfoSpinner);
        List<ModuleSpinnerDetail> moduleSpinnerDetails = getModuleSpinnerDetails();
        ArrayAdapter<ModuleSpinnerDetail> adapter = new ArrayAdapter<ModuleSpinnerDetail>(HomeSteadActivity.this, R.layout.module_spinner_item, moduleSpinnerDetails);
        adapter.setDropDownViewResource(R.layout.module_list_item_single_choice);
        moduleSpinner.setAdapter(adapter);

        moduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initHomeSteadUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private ArrayList<ModuleSpinnerDetail> getModuleSpinnerDetails() {

        ArrayList<ModuleSpinnerDetail> moduleSpinnerDetails = new ArrayList<ModuleSpinnerDetail>();
        ModuleSpinnerDetail allModuleSpinner = new ModuleSpinnerDetail();
        allModuleSpinner.setModuleID(MODULE_SPINNER_ALL_ITEM);
        allModuleSpinner.setShowText(getString(R.string.module_spinner_all_item));
        moduleSpinnerDetails.add(allModuleSpinner);

        SQLiteDatabase readableDatabase = HISDatabaseFactory.getReadableSqLiteDatabase(getApplicationContext());
        try
        {
            Cursor cursor = readableDatabase.rawQuery("SELECT moduleID, (moduleCounty || '-' || moduleTown ||  '-' || moduleVillage || '-' ||  moduleGroup) AS showText FROM MODULE ",new String[]{});
            if(cursor != null && cursor.getCount() > 0){
                while (cursor.moveToNext()){
                    ModuleSpinnerDetail bean = new ModuleSpinnerDetail();
                    bean.setModuleID(cursor.getInt(0));
                    bean.setShowText(cursor.getString(1));
                    moduleSpinnerDetails.add(bean);
                }
                cursor.close();
            }
        }
        catch(Exception ex)
        {
            Toast.makeText(HomeSteadActivity.this, getText(R.string.failure_query_module), Toast.LENGTH_SHORT).show();
        }
        finally
        {
            readableDatabase.close();
        }

        return moduleSpinnerDetails;
    }

    private void deleteSelectedHomeSteads() {

        ArrayList<String> homeSteadIDList = new ArrayList<String>();
        ArrayList<String> homeSteadDirectoryList = new ArrayList<String>();

        Set<HomeSteadTableDetail> keySet = homeSteadCheckedMap.keySet();
        Iterator<HomeSteadTableDetail> iterator = keySet.iterator();
        while (iterator.hasNext()){
            HomeSteadTableDetail key = iterator.next();
            if(homeSteadCheckedMap.get(key).booleanValue())
            {
                homeSteadIDList.add(key.getHomeSteadID());
                homeSteadDirectoryList.add(HomeSteadInformationHelper.getHomeSteadDirectoryPath(key));
            }
        }

        String[] sqlInParameters = getStringForSQLInParameter(homeSteadIDList);
        if(sqlInParameters.length > 0)
        {
            deleteHomeSteads(sqlInParameters, homeSteadDirectoryList);
        }
    }

    private void deleteHomeSteads(String[] sqlInParameters, ArrayList<String> homeSteadDirectoryList) {

        SQLiteDatabase writableDatabase = HISDatabaseFactory.getWritableSqLiteDatabase(getApplicationContext());
        writableDatabase.beginTransaction();
        try
        {
            String whereClause = getWhereClause(sqlInParameters);
            int result = writableDatabase.delete(HISDatabaseHelper.TABLE_HOMESTEAD_NAME, whereClause, sqlInParameters);
            writableDatabase.setTransactionSuccessful();

            if(result <= 0)
            {
                showErrorMessage();
            }
            else
            {
                showSuccessMessageAndUpdateUI(homeSteadDirectoryList);
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

    private void showErrorMessage() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(HomeSteadActivity.this, getText(R.string.failure_deleted_homestead), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessMessageAndUpdateUI(ArrayList<String> homeSteadDirectoryList) {

        for (String homeSteadBasePath : homeSteadDirectoryList)
        {
            HomeSteadInformationHelper.deleteDirectory(new File(homeSteadBasePath));
        }

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(HomeSteadActivity.this, getText(R.string.success_deleted_homestead), Toast.LENGTH_SHORT).show();
                initHomeSteadUI();
            }
        });
    }

    private String getWhereClause(String[] sqlInParameters) {

        StringBuffer buffer = new StringBuffer();
        buffer.append(" homeSteadID IN( ");
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

    private ArrayList<HomeSteadDetail> getHomeSteadDetails() {

        ArrayList<HomeSteadDetail> homeSteadDetails = new ArrayList<HomeSteadDetail>();
        SQLiteDatabase readableDatabase = HISDatabaseFactory.getReadableSqLiteDatabase(getApplicationContext());
        try
        {
            Cursor cursor;
            int moduleID = ((ModuleSpinnerDetail)moduleSpinner.getSelectedItem()).getModuleID();
            if (moduleID == MODULE_SPINNER_ALL_ITEM)
            {
                cursor = readableDatabase.rawQuery("SELECT moduleID, homesteadID, homeSteadType, homeSteadKey, homeSteadValue FROM HOMESTEAD WHERE HOMESTEADTYPE = ? ",new String[]{HomeSteadConstant.HOME_STEAD_TYPE_BASIC});
            }
            else {
                cursor = readableDatabase.rawQuery("SELECT moduleID, homesteadID, homeSteadType, homeSteadKey, homeSteadValue FROM HOMESTEAD WHERE MODULEID = ? AND HOMESTEADTYPE = ?  ",new String[]{String.valueOf(moduleID), HomeSteadConstant.HOME_STEAD_TYPE_BASIC});
            }

            if(cursor != null && cursor.getCount() > 0){
                while (cursor.moveToNext()){
                    HomeSteadDetail bean = new HomeSteadDetail();
                    bean.setModuleID(cursor.getInt(0));
                    bean.setHomeSteadID(cursor.getString(1));
                    bean.setHomeSteadType(cursor.getString(2));
                    bean.setHomeSteadKey(cursor.getString(3));
                    bean.setHomeSteadValue(cursor.getString(4));
                    homeSteadDetails.add(bean);
                }
                cursor.close();
            }

            Toast.makeText(HomeSteadActivity.this, getText(R.string.success_query_homestead), Toast.LENGTH_SHORT).show();
        }
        catch(Exception ex)
        {
            Toast.makeText(HomeSteadActivity.this, getText(R.string.failure_query_homestead), Toast.LENGTH_SHORT).show();
        }
        finally
        {
            readableDatabase.close();
        }

        return homeSteadDetails;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //For Add HomeStead Operation / Modify HomeStead Operation
        if((requestCode == 0 || requestCode == 1) && resultCode == 0)
        {
            HomeSteadInformationHelper.deleteTempImageFiles();
            initHomeSteadUI();
        }
    }

    private void initHomeSteadUI() {

        homeSteadCheckedMap.clear();
        disableDeleteAndModifyBtn();

        ArrayList<HomeSteadDetail> homeSteadDetails = getHomeSteadDetails();
        ArrayList<HomeSteadTableDetail> homeSteadTableDetails = transform2HomeSteadTableDetails(homeSteadDetails);
        homeSteadAdapter = new HomeSteadAdapter(homeSteadTableDetails, HomeSteadActivity.this);
        homeSteadItemView.setAdapter(homeSteadAdapter);
        HomeSteadInformationHelper.setListViewHeightBasedOnChildren(homeSteadItemView);
    }

    private ArrayList<HomeSteadTableDetail> transform2HomeSteadTableDetails(ArrayList<HomeSteadDetail> homeSteadDetails) {

        ArrayList<HomeSteadTableDetail> homeSteadTableDetails = new ArrayList<HomeSteadTableDetail>();

        for(int index = 0; index < homeSteadDetails.size(); index +=4)
        {
            HomeSteadTableDetail tableDetail = new HomeSteadTableDetail();

            tableDetail.setModuleID(homeSteadDetails.get(index).getModuleID());
            tableDetail.setHomeSteadID(homeSteadDetails.get(index).getHomeSteadID());
            tableDetail.setModuleInfo(homeSteadDetails.get(index).getHomeSteadValue());
            tableDetail.setHomeSteadNumber(homeSteadDetails.get(index + 1).getHomeSteadValue());
            tableDetail.setHouseHolder(homeSteadDetails.get(index + 2).getHomeSteadValue());

            homeSteadTableDetails.add(tableDetail);
        }

        return homeSteadTableDetails;
    }

    private void disableDeleteAndModifyBtn() {

        enableOrDisableDeleteBtn(false);
        enableOrDisableModifyBtn(false);
    }

    public void putCheckboxStatus(HomeSteadTableDetail homeSteadTableDetail, Boolean value)
    {
        homeSteadCheckedMap.put(homeSteadTableDetail, value);
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
        Collection<Boolean> values = homeSteadCheckedMap.values();
        for (Boolean val : values)
        {
            if(val.booleanValue())
            {
                number ++;
            }
        }

        return number;
    }

    private HomeSteadTableDetail getCheckedHomeSteadKey() {

        Set<Map.Entry<HomeSteadTableDetail, Boolean>> entrySet = homeSteadCheckedMap.entrySet();
        for (Map.Entry<HomeSteadTableDetail, Boolean> entry : entrySet)
        {
            if(entry.getValue().booleanValue())
            {
                return entry.getKey();
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
