package com.andy.his;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

public class ExitApplication {

    private List<Activity> activityList = new LinkedList();

    public static ExitApplication instance = new ExitApplication();

    private ExitApplication(){}

    public static ExitApplication getInstance()
    {
        return instance;
    }

    public void addActivity(Activity activity)
    {
        activityList.add(activity);
    }

    public void exit()
    {
        for(Activity activity:activityList)
        {
            activity.finish();
        }
        System.exit(0);
    }
}
