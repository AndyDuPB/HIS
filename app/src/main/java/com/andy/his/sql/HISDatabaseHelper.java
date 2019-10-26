package com.andy.his.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class HISDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_SCRIPT_4_MODULE = "CREATE TABLE MODULE ("
            + "moduleID INTEGER PRIMARY KEY Autoincrement ,"
            + "moduleCounty varchar(50) ,"
            + "moduleTown varchar(50) ,"
            + "moduleVillage varchar(50) ,"
            + "moduleGroup varchar(50) )";

    public static final String CREATE_SCRIPT_4_MODULE_TEMP = "CREATE TABLE MODULE_TEMP ("
            + "moduleID INTEGER PRIMARY KEY Autoincrement ,"
            + "moduleCounty varchar(50) ,"
            + "moduleTown varchar(50) ,"
            + "moduleVillage varchar(50) ,"
            + "moduleGroup varchar(50) )";

    public static final String CREATE_SCRIPT_4_HOMESTEAD = "CREATE TABLE HOMESTEAD ("
            + "homesteadID varchar(16) , "
            + "moduleID INTEGER , "
            + "homeSteadType varchar(10) , "
            + "homeSteadKey varchar(100) , "
            + "homeSteadValue varchar(1000) , "
            + " PRIMARY KEY (homesteadID, moduleID, homeSteadType, homeSteadKey) )";

    public static final String CREATE_SCRIPT_4_HOMESTEAD_TEMP = "CREATE TABLE HOMESTEAD_TEMP ("
            + "homesteadID varchar(16) , "
            + "moduleID INTEGER , "
            + "homeSteadType varchar(10) , "
            + "homeSteadKey varchar(100) , "
            + "homeSteadValue varchar(1000) , "
            + " PRIMARY KEY (homesteadID, moduleID, homeSteadType, homeSteadKey) )";

    public static final String DB_NAME = "HIS_DB";

    public static final String TABLE_MODULE_NAME = "MODULE";

    public static final String TABLE_HOMESTEAD_NAME = "HOMESTEAD";

    private Context context;

    public HISDatabaseHelper(Context context) {
        super(context, DB_NAME, null, 11);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_SCRIPT_4_MODULE);
        database.execSQL(CREATE_SCRIPT_4_HOMESTEAD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        //database.execSQL("DROP TABLE MODULE");
        //database.execSQL("DROP TABLE HOMESTEAD");
        //database.execSQL(CREATE_SCRIPT_4_MODULE);

        //Home Stead
        database.execSQL(CREATE_SCRIPT_4_HOMESTEAD_TEMP);

        database.execSQL("INSERT INTO HOMESTEAD_TEMP SELECT homesteadID,moduleID,homeSteadType,homeSteadKey,homeSteadValue FROM HOMESTEAD; ");
        database.execSQL("DROP TABLE HOMESTEAD");

        database.execSQL(CREATE_SCRIPT_4_HOMESTEAD);

        database.execSQL("INSERT INTO HOMESTEAD SELECT homesteadID,moduleID,homeSteadType,homeSteadKey,homeSteadValue FROM HOMESTEAD_TEMP; ");
        database.execSQL("DROP TABLE HOMESTEAD_TEMP ");

        //Module
        database.execSQL(CREATE_SCRIPT_4_MODULE_TEMP);

        database.execSQL("INSERT INTO MODULE_TEMP SELECT moduleID,moduleCounty,moduleTown,moduleVillage,moduleGroup FROM MODULE; ");
        database.execSQL("DROP TABLE MODULE");

        database.execSQL(CREATE_SCRIPT_4_MODULE);

        database.execSQL("INSERT INTO MODULE SELECT moduleID,moduleCounty,moduleTown,moduleVillage,moduleGroup FROM MODULE_TEMP; ");
        database.execSQL("DROP TABLE MODULE_TEMP ");
    }
}