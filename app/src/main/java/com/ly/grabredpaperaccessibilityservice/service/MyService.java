package com.ly.grabredpaperaccessibilityservice.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.ly.grabredpaperaccessibilityservice.GrabRedPaperAccessibilityService;
import com.ly.grabredpaperaccessibilityservice.R;
import com.ly.grabredpaperaccessibilityservice.TranslucentActivity;
import com.ly.grabredpaperaccessibilityservice.entity.WeChat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.ly.grabredpaperaccessibilityservice.GrabRedPaperAccessibilityService.TAG_WeChat;
import static com.ly.grabredpaperaccessibilityservice.GrabRedPaperAccessibilityService.weChat;
import static com.ly.grabredpaperaccessibilityservice.MainActivity.tvVersion;

/**
 * Created by 刘样大帅B on 2018/3/19.
 */

public class MyService extends Service implements GrabRedPaperAccessibilityService.OnJumpActivityListener {

    private boolean isFlag;
    private String strVersion;
    private String filename = "wechat.txt";
    private ProgressDialog dialog;
    CheckIsOpenAccessibilityService checkIsOpenAccessibilityService;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private float sumMoney;
    private int sumCount;
    private NotificationManager manager;
    private int num;
    private boolean isChecked;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    if (tvVersion != null) {
                        tvVersion.setText("\n" + strVersion);
                    }
                    break;
                }
                case 2: {
                    Toast.makeText(MyService.this, "APP升级中，不允许进行下一步", Toast.LENGTH_SHORT).show();
                    break;
                }
                case 3: {
                    Toast.makeText(MyService.this, "辅助功能未开启，请前往辅助功能中开启“抢微信红包插件”", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
                }
                case 5: {
                    dialog.cancel();
                    Log.d(TAG_WeChat,"读取版本失败，转换成本地读取");
                    Toast.makeText(MyService.this, "读取版本失败，转换成本地读取", Toast.LENGTH_SHORT).show();
                    readerByFile();
                    break;
                }
            }
        }
    };
    private GetDataByIntentThread getDataByIntentThread;
    private MyShowDialogCheckDismissThread myShowDialogCheckDismissThread;


    /**
     * 绑定服务时才会调用
     * 必须要实现的方法
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或 onBind() 之前）。
     * 如果服务已在运行，则不会调用此方法。该方法只被调用一次
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG_WeChat, "onCreate invoke");
    }

    /**
     * 每次通过startService()方法启动Service时都会被回调。
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG_WeChat, "onStartCommand invoke");
        init();
        return super.onStartCommand(intent, flags, startId);
    }

    private void init() {
        sharedPreferences = getSharedPreferences("sumMoney", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        firstOutputFile();
        GrabRedPaperAccessibilityService.mServiceListener = this;
        if (checkIsOpenAccessibilityService == null) {
            checkIsOpenAccessibilityService = new CheckIsOpenAccessibilityService();
            checkIsOpenAccessibilityService.start();
        }
        if (getDataByIntentThread == null) {
            getDataByIntentThread = new GetDataByIntentThread();
            getDataByIntentThread.start();
            if (sharedPreferences.getBoolean("isShowProgress", true)) {
                initProgressDialog();
            }
        } else {
            if (getDataByIntentThread.isExit) {
                getDataByIntentThread = new GetDataByIntentThread();
                getDataByIntentThread.start();
                if (sharedPreferences.getBoolean("isShowProgress", true)) {
                    initProgressDialog();
                }
            }
        }
    }

    /**
     * 服务销毁时的回调
     */
    @Override
    public void onDestroy() {
        Log.d(TAG_WeChat, "onDestroy invoke");
        super.onDestroy();
    }

    private void firstOutputFile() {
        try {
            openFileInput("wechat.txt");
        } catch (IOException e) {
            Log.d(TAG_WeChat, "不存在");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("wechat.txt")));
                String s = "";
                StringBuffer buffer = new StringBuffer();
                while ((s = reader.readLine()) != null) {
                    buffer.append(s + "\n");
                }
                reader.close();
                FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(buffer.toString().getBytes());
                outputStream.close();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void initProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("正在获取版本中，请稍后······");
        dialog.setCancelable(false);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        if (!dialog.isShowing()) {
            dialog.show();
        }
            myShowDialogCheckDismissThread = new MyShowDialogCheckDismissThread();
            myShowDialogCheckDismissThread.start();
    }

    private void readerByIntent(BufferedReader reader) {
        try {
            String line;
            Log.d(TAG_WeChat,"从网络中读取版本信息");
            String split = "%%%%%";
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("%%%%%\n");
            boolean isAdd = false;
            boolean flag = false;
            strVersion = "";
            while ((line = reader.readLine()) != null) {
                if (line.length() == 5 && line.equals(split)) {
                    isAdd = !isAdd;
                }
                if (isAdd) {
                    if (line.length() == 5 && line.equals(split)) {
                        flag = !flag;
                        if (flag) {
                            continue;
                        } else if (weChat!= null){
                            strVersion = strVersion.substring(0, strVersion.length() - 2);
                            Log.d(TAG_WeChat,"当前从网络中拿到的版本：" + strVersion);
                            handler.sendEmptyMessage(1);
                            stringBuffer.append("%%%%%");
                            FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                            outputStream.write(stringBuffer.toString().getBytes());
                            Log.d(TAG_WeChat, stringBuffer.toString());
                            outputStream.close();
                            return;
                        }
                    }
                    if (!isFlag) {
                        Log.d(TAG_WeChat,line);
                        stringBuffer.append(line + "\n");
                        fun(line);
                    } else {
                        return;
                    }
                }
            }
            Log.d(TAG_WeChat,""+(flag)+(weChat.getStrVersion1()));
            if (flag && weChat != null) {
                strVersion = strVersion.substring(0, strVersion.length() - 2);
                Log.d(TAG_WeChat,"当前从网络中拿到的版本：" + strVersion);
                handler.sendEmptyMessage(1);
                stringBuffer.append("%%%%%");
                FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(stringBuffer.toString().getBytes());
                Log.d(TAG_WeChat, stringBuffer.toString());
                outputStream.close();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readerByFile() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(filename)));
            String s = "";
            strVersion = "";
            Log.d(TAG_WeChat,"从本地文件中读取版本信息");
            while ((s = reader.readLine()) != null) {
                if (!isFlag) {
                    fun(s);
                } else {
                    return;
                }
            }
            strVersion = strVersion.substring(0, strVersion.length() - 2);
            handler.sendEmptyMessage(1);
            Log.d(TAG_WeChat,"当前从本地文件拿到的版本：" + strVersion);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fun(String s) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo("com.tencent.mm", 0);
            String[] strings = s.split("\\ ");
            if (strings[0].equals("")) {
                return;
            }
            if (strings[0].charAt(0) == '`') {
                return;
            }
            if (strings[0].length() >= 5 && strings[0].substring(0, 5).equals("%%%%%")) {
                return;
            }
            if (strings[0].length() == 1 && strings[0].equals("1")) {
                return;
            }
            if (strings[0].length() == 1 && strings[0].equals("0")) {
                isFlag = true;
                handler.sendEmptyMessage(2);
                FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write("0".getBytes());
                outputStream.close();
                return;
            }
            strVersion += strings[0] + ", ";
            if (strings[0].equals(packageInfo.versionName) && strings[1].equals(packageInfo.versionCode + "")) {
                weChat = new WeChat(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8], strings[9], strings[10], strings[11], strings[12]);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onJumpActivity() {
        Log.d(TAG_WeChat, "服务跳转到这里来了");
        Intent intent = new Intent();
        intent.setClass(MyService.this, TranslucentActivity.class);
        startActivity(intent);
    }

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
            sumMoney = sharedPreferences.getFloat("sumMoney", 0.00f);
            sumCount = sharedPreferences.getInt("sumCount", 0);
            isChecked = sharedPreferences.getBoolean("isChecked", false);
            BigDecimal bigDecimal1 = new BigDecimal(money);
            BigDecimal bigDecimal2 = new BigDecimal(Float.toString(sumMoney));
            sumMoney = bigDecimal1.add(bigDecimal2).floatValue();
            editor.putInt("sumCount", ++sumCount);
            editor.putFloat("sumMoney", sumMoney);
            editor.commit();
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


    class CheckIsOpenAccessibilityService extends Thread {
        @Override
        public void run() {
//            try {
//                Thread.sleep(4000);
//                if (tvVersion != null) {
//                    tvVersion.sendAccessibilityEventUnchecked();
//                    tvVersion.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
//                    Log.d(TAG_WeChat,"服务发了通知");
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            while (true) {
                try {
                    Thread.sleep(4000);
                    if (!isAccessibilitySettingsOn(MyService.this) && (weChat != null) && !isFlag) {
                        handler.sendEmptyMessage(3);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + GrabRedPaperAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG_WeChat, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG_WeChat, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    class GetDataByIntentThread extends Thread {
        public boolean isExit = false;

        @Override
        public void run() {
            try {
                URL url = new URL("http://blog.csdn.net/q2477046978/article/details/79598372");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                Log.d(TAG_WeChat, "连接网络中");
                int responsecode = urlConnection.getResponseCode();
                if (responsecode == 200) {
                    Log.d(TAG_WeChat, "响应成功，正在读取中");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                    readerByIntent(reader);
                } else {
                    Log.d(TAG_WeChat,"获取不到网页的源码，服务器响应代码为：" + responsecode);
                    readerByFile();
                }
            } catch (Exception e) {
                readerByFile();
            } finally {
                isExit = true;
            }
        }
    }
    class MyShowDialogCheckDismissThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                sleep(4000);
                if (dialog.isShowing()) {
                    handler.sendEmptyMessage(5);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
