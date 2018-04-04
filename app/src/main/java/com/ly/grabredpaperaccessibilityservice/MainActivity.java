package com.ly.grabredpaperaccessibilityservice;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ly.grabredpaperaccessibilityservice.util.Utils;

import java.math.BigDecimal;

import static com.ly.grabredpaperaccessibilityservice.GrabRedPaperAccessibilityService.TAG_WeChat;
import static com.ly.grabredpaperaccessibilityservice.GrabRedPaperAccessibilityService.activity;
import static com.ly.grabredpaperaccessibilityservice.GrabRedPaperAccessibilityService.mActivityListener;

public class MainActivity extends Activity implements GrabRedPaperAccessibilityService.OnJumpActivityListener, CompoundButton.OnCheckedChangeListener {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private float sumMoney;
    private int sumCount;
    private long currnetTime;
    private double touchTime;
    private double waitTime = 2000;
    public boolean isChecked;
    private NotificationManager manager;
    private int num = 0;
    public Switch switchToggle;
    public Switch switch_progress;
    public TextView tvCount;
    public TextView tvMoney;
    public static TextView tvVersion;
    private boolean isShowProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
//                this.deleteFile(filename);
        initDatas();

    }

    private void initViews() {
        switchToggle = (Switch) findViewById(R.id.switch_toggle);
        switch_progress = (Switch) findViewById(R.id.switch_progress);
        tvCount = (TextView) findViewById(R.id.tvCount);
        tvMoney = (TextView) findViewById(R.id.tvMoney);
        tvVersion = (TextView) findViewById(R.id.tv_version);
    }


    public void openAccessibility(View v) {
        if (tvCount != null) {
            tvCount.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            Log.d(TAG_WeChat,"发了通知");
        }
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void initDatas() {
        switchToggle.setOnCheckedChangeListener(this);
        switch_progress.setOnCheckedChangeListener(this);
        sharedPreferences = getSharedPreferences("sumMoney", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        sumMoney = sharedPreferences.getFloat("sumMoney", 0.00f);
        sumCount = sharedPreferences.getInt("sumCount", 0);
        isChecked = sharedPreferences.getBoolean("isChecked", false);
        isShowProgress = sharedPreferences.getBoolean("isShowProgress", true);
        tvCount.setText(sumCount + "");
        tvMoney.setText(sumMoney + "");
        switch_progress.setChecked(isShowProgress);
        activity = this;
        mActivityListener = this;
    }


    @Override
    public void onJumpActivity() {
        Log.d(TAG_WeChat, "activity跳转到这里来了");
//
        if (tvVersion != null) {
            tvVersion.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            Log.d(TAG_WeChat,"发了通知");
        }
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, TranslucentActivity.class);
        startActivity(intent);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onToast(String money) {
        boolean isMoney = true;
        for (int i = 0; i < money.length(); i++) {
            if (money.charAt(i) == '.') {
                continue;
            }
            if (money.charAt(i) > 57 || money.charAt(i) < 48) {
                isMoney = false;
                break;
            }
        }
        if (isMoney) {
            BigDecimal bigDecimal1 = new BigDecimal(money);
            BigDecimal bigDecimal2 = new BigDecimal(Float.toString(sumMoney));
            sumMoney = bigDecimal1.add(bigDecimal2).floatValue();
            editor.putInt("sumCount", ++sumCount);
            editor.putFloat("sumMoney", sumMoney);
            editor.commit();
            tvCount.setText(sumCount + "");
            tvMoney.setText("" + sumMoney);
            Log.d(TAG_WeChat, "这次抢到" + bigDecimal1.floatValue());
            Toast.makeText(this, "恭喜领到了" + money + "元", Toast.LENGTH_SHORT).show();
            if (isChecked) {
                Log.d(TAG_WeChat, "已开启通知 发送通知");
                manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
                nb.setContentTitle("抢到红包通知");
                nb.setSmallIcon(R.mipmap.ic_launcher);
                nb.setTicker("抢到红包了");
                nb.setContentText("恭喜领到了" + money + "元");
                nb.setContentInfo("微信信息");
                nb.setWhen(System.currentTimeMillis());
                nb.setDefaults(Notification.DEFAULT_ALL);//设置全部
                nb.setAutoCancel(true);
                manager.notify(++num, nb.build());
            }
        }
    }

    @Override
    public void onBackPressed() {
        currnetTime = System.currentTimeMillis();
        if ((currnetTime - touchTime) >= waitTime) {
            Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
            touchTime = currnetTime;
        } else {
            finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.switch_toggle) {
            this.isChecked = isChecked;
            editor.putBoolean("isChecked", isChecked).commit();
            if (isChecked) {
                Toast.makeText(this, "开启通知", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "关闭通知", Toast.LENGTH_SHORT).show();
            }
        } else {
            isShowProgress = isChecked;
            editor.putBoolean("isShowProgress", isShowProgress).commit();
            if (isChecked) {
                Toast.makeText(this, "开启获取版本时的进度条", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "关闭获取版本时的进度条", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
