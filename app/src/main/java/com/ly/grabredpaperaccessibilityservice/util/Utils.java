package com.ly.grabredpaperaccessibilityservice.util;

import android.app.Activity;
import android.content.Intent;

import com.ly.grabredpaperaccessibilityservice.R;

/**
 * Created by 刘样大帅B on 2018/3/17.
 */

public  class Utils {
    private static int sTheme = 1;
    public static void changeToTheme(Activity activity, int theme)
    {
        sTheme = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }
    public static void onActivityCreateSetTheme(Activity activity)
    {
        switch (sTheme)
        {
            default:
            case 1:
                activity.setTheme(R.style.AppTheme);
                break;
            case 2:
                activity.setTheme(R.style.translucent);
                break;
        }
    }
}
