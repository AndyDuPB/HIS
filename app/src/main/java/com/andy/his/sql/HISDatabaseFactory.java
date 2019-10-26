package com.andy.his.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.andy.his.HomeSteadInformationActivity;

public class HISDatabaseFactory {

    public static HISDatabaseHelper getHISDatabaseHelper(Context context)
    {
        return new HISDatabaseHelper(context);
    }

    public static SQLiteDatabase getWritableSqLiteDatabase(Context context)
    {
        return getHISDatabaseHelper(context).getWritableDatabase();
    }

    public static SQLiteDatabase getReadableSqLiteDatabase(Context context)
    {
        return getHISDatabaseHelper(context).getReadableDatabase();
    }
}
