package com.ly.grabredpaperaccessibilityservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import static com.ly.grabredpaperaccessibilityservice.GrabRedPaperAccessibilityService.TAG_WeChat;

public class TranslucentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translucent);
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                    finish();
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setAction(Intent.ACTION_MAIN);
                    startActivity(intent);
                    Log.d(TAG_WeChat, "返回微信");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
