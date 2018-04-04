package com.ly.grabredpaperaccessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.ly.grabredpaperaccessibilityservice.entity.WeChat;
import com.ly.grabredpaperaccessibilityservice.service.MyService;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by 刘样大帅B on 2018/3/16.
 */

public class GrabRedPaperAccessibilityService extends AccessibilityService {
    public static Activity activity;
    public static final String TAG_WeChat = "微信红包";
    public static final String TAG_QQ = "QQ红包";
    public static WeChat weChat;
    public static OnJumpActivityListener mActivityListener;
    public static OnJumpActivityListener mServiceListener;
    int state = 0;
    int flag = 0;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG_WeChat, "辅助功能开启");
        Log.d(TAG_WeChat, "启动了服务");
        Intent intent = new Intent(GrabRedPaperAccessibilityService.this, MyService.class);
        startService(intent);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG_WeChat, "辅助功能被关闭");
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {    //通知栏事件
            Log.d(TAG_WeChat, "通知栏事件"+event.toString() );
            if (activity != null) {
                checkScreen(activity);
            }
            if (flag != 1) {
                handleNotification(event);
            }
        } else {    //非通知栏事件    处理其他事件
            CharSequence packageName = event.getPackageName();
            if (packageName.equals("com.tencent.mobileqq")) {
//                checkQQ(event);
            }
            if (packageName.equals("com.tencent.mm")) {
                Log.d(TAG_WeChat,"微信"+ (getRootInActiveWindow() == null) + event.toString());
                checkWeChat(event, true);
            }
            if (packageName.equals("com.ly.grabredpaperaccessibilityservice")) {
//                Log.d(TAG_WeChat, "本程序"+(getRootInActiveWindow() == null) + event.toString());
                checkWeChat(event, false);
            }

        }
    }

    private void checkQQ(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        Log.d(TAG_QQ, event.toString());
        List<AccessibilityNodeInfo> childInfo = nodeInfo.findAccessibilityNodeInfosByText("QQ红包");
        if (!childInfo.isEmpty()) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : childInfo) {
                if (!accessibilityNodeInfo.isLongClickable()) {
                    Log.d(TAG_QQ, accessibilityNodeInfo.toString());
                    AccessibilityNodeInfo parent = accessibilityNodeInfo.getParent();
                    if (parent.isClickable()) {
                        performViewClick(parent);
                    }
                }
            }
        }
        childInfo = nodeInfo.findAccessibilityNodeInfosByText("元");
        if (!childInfo.isEmpty() && childInfo.get(0).getParent().getChildCount() == 3) {
            CharSequence money = childInfo.get(0).getParent().getChild(1).getText();
            Log.d(TAG_QQ, "抢到" + money + "元");
            performBackClick();
        }
        childInfo = nodeInfo.findAccessibilityNodeInfosByText("来晚一步，领完啦~");
        if (!childInfo.isEmpty() && childInfo.get(0).getParent().getChildCount() == 2) {
            Log.d(TAG_QQ, "来晚一步，被领完啦");
            performBackClick();
        }

    }

    public void checkWeChat(AccessibilityEvent event, boolean isCheck) {

        if (isCheck && weChat == null) {
            Log.d(TAG_WeChat, "结束" + (weChat == null));
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "当前获取不到微信版本信息，或者不适配此版本", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return;
        }
        if (weChat == null) {
            return;
        }
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> chatNodes = nodeInfo.findAccessibilityNodeInfosByViewId(weChat.getBtnRightAdd());
            if (!chatNodes.isEmpty()) {   //在聊天页面
                Log.d(TAG_WeChat, "在聊天页面，查找是否可以进入打开红包界面" + flag);
                actionChat(nodeInfo);    //进入打开红包界面
            } else {
                List<AccessibilityNodeInfo> listItemNodes = nodeInfo.findAccessibilityNodeInfosByViewId(weChat.getTvMainItem());
                if (!listItemNodes.isEmpty()) {   //在首页
                    Log.d(TAG_WeChat, "在首页，查找是否可以进入聊天页面" + flag);
                    flag = 0;
                    actionMain(nodeInfo);    //进入聊天页面
                }
            }
        }
        String className = event.getClassName().toString();
        if (weChat.getStrAppearRedPaper().equals(className)) {  //如果是 出现红包类   那当前页面只可能是 打开红包的页面  搜索‘开’
            Log.d(TAG_WeChat, "进行抢红包");
            if (activity != null) {
                checkScreen(activity);
            }
            if (nodeInfo != null) {
                openRedPackage(nodeInfo);
                flag = 0;
            } else {
                if (MainActivity.tvVersion == null) {
                    mServiceListener.onJumpActivity();
                } else {
                    mActivityListener.onJumpActivity();
                }
            }
        }
        if (weChat.getStrDetailRedPaper().equals(className)) {   // 当前如果被抢过了   就拿到抢的金额吐司   并返回
            if (nodeInfo != null) {
                lookMoney(nodeInfo);
            }
        }
    }

    /*
       查看红包金额
        */
    public void lookMoney(AccessibilityNodeInfo node) {
        List<AccessibilityNodeInfo> moneyInfo = node.findAccessibilityNodeInfosByViewId(weChat.getTvMoney());
        if (!moneyInfo.isEmpty()) {
            if (moneyInfo.get(0).getText() != null) {
                if (state == 1) {
                    if (MainActivity.tvVersion == null) {
                        mServiceListener.onToast(moneyInfo.get(0).getText().toString());
                    } else {
                        mActivityListener.onToast(moneyInfo.get(0).getText().toString());
                    }
                    Log.d(TAG_WeChat, "抢到了" + moneyInfo.get(0).getText().toString() + "元  进行返回");
                    state = 0;
                    performBackClick();
                } else {
                    Log.d(TAG_WeChat, "红包被抢过了，" + moneyInfo.get(0).getText().toString() + "元  进行返回");
                    Toast.makeText(activity, "这红包已经被你抢过了，已经抢了" + moneyInfo.get(0).getText().toString() + "元 ", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }


    private void openRedPackage(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> kaiNodes = nodeInfo.findAccessibilityNodeInfosByViewId(weChat.getBtnOpen());         //获取开按钮
        List<AccessibilityNodeInfo> slowNodes = nodeInfo.findAccessibilityNodeInfosByViewId(weChat.getTvFingerSlow());       //获取 手慢了 提示语句的控件
        if (!kaiNodes.isEmpty()) {      //获取到开按钮 点击此按钮
            if (state == 0) {
                state = 1;
            }
            performViewClick(kaiNodes.get(0));
        } else {
            if (!slowNodes.isEmpty()) {
                Log.d(TAG_WeChat, "抢的太慢了没了");
                Toast.makeText(activity, "抢的太慢了没了", Toast.LENGTH_SHORT).show();
            }
            performBackClick();
        }
    }

    public void actionChat(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> titleNodes =
                nodeInfo.findAccessibilityNodeInfosByViewId(weChat.getTvChatTitle());   //获得聊天窗口标题
        if (!titleNodes.isEmpty()) {      //判断标题最后是否是一个括号，
            String title = titleNodes.get(0).getText().toString();
            if (!TextUtils.isEmpty(title)) {
                if (title.contains("(")) {
                    int indexLeft = title.lastIndexOf("(");
                    String end = title.substring(indexLeft);
                    end = end.substring(1, end.length() - 1);
                    try {
                        Integer.parseInt(end);  //群聊
                        yesOrNoPackage(nodeInfo, false);
                    } catch (Exception e) {   //私聊
                        yesOrNoPackage(nodeInfo, true);
                    }
                } else {   //私聊 默认私聊
                    yesOrNoPackage(nodeInfo, true);
                }
            }
        }
    }

    /*
        看在聊天页面中 有没有红包 有就点进去 没有就返回；
     */
    private void yesOrNoPackage(AccessibilityNodeInfo nodeInfo, boolean isSecFlag) {
        //在聊天页面
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if (list == null)
            return;
        if (list.isEmpty() && !isSecFlag) {      //没有 直接返回
            list = nodeInfo.findAccessibilityNodeInfosByText("查看红包");
            if (list == null)
                return;
            if (list.isEmpty()) {
                Log.d(TAG_WeChat, "找不到 领取红包 和查看红包四个字");
//                performBackClick();
            } else {
                lookRedPackage(list, "查看红包");   //自己发的红包 如果是群聊就可以领取  如果是私聊就不能领取
            }
        } else {  //有 但是要检查是不是红包
            Log.d(TAG_WeChat, "找到了领取红包四个字");
            lookRedPackage(list, "领取红包");    //找领取红包

        }
    }

    public void lookRedPackage(List<AccessibilityNodeInfo> list, String str) {
        for (int i = list.size() - 1; i >= 0; i--) {
            AccessibilityNodeInfo node = list.get(i);
            AccessibilityNodeInfo parent = node.getParent();
            if (parent != null) {
                List<AccessibilityNodeInfo> wxhbNodes = parent.findAccessibilityNodeInfosByViewId(weChat.getTvOpenRedPackage());
                if (!wxhbNodes.isEmpty()) {
                    if (str.equals(wxhbNodes.get(0).getText().toString())) {      //是的 没错  领取红包
                        Log.d(TAG_WeChat, "点击领取红包 ");
                        if (flag == 0) {
                            flag = 1;
                        }
                        performViewClick(node);
                        return;
                    }
                }
            }
        }

    }

    public void actionMain(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByViewId(weChat.getTvMainChat());
        if (nodes != null) {
            for (AccessibilityNodeInfo node : nodes) {
                if (node.getText() != null && node.getText().toString().contains("[微信红包]")) {   //还要判断是否有未读消息
                    AccessibilityNodeInfo parent = node.getParent();
                    if (parent != null) {
                        List<AccessibilityNodeInfo> numsNodes =
                                parent.findAccessibilityNodeInfosByViewId(weChat.getTvNoReadNum()); //获得 首页聊天窗口中的未读数目
                        if (!numsNodes.isEmpty()) {
                            CharSequence text = numsNodes.get(0).getText();
                            if (text != null) {
                                if (Integer.parseInt(text.toString()) != 0) {
                                    performViewClick(parent);
                                }
                            }
                        }
                    }
                    return;
                }
            }
        }
    }

    /*
    检查屏幕 并唤醒
     */
    private void checkScreen(Context context) {
        // TODO Auto-generated method stub
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
            Log.d(TAG_WeChat, "屏幕熄灭了");
            wakeUpAndUnlock(context);
        }
    }

    private void wakeUpAndUnlock(Context context) {
        // 获取电源管理器对象
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        if (!screenOn) {

            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire(10000); // 点亮屏幕
            wl.release(); // 释放
            Log.d(TAG_WeChat, "点亮屏幕");
        }
        // 屏幕解锁
        KeyguardManager keyguardManager = (KeyguardManager) context
                .getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        // 屏幕锁定
        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard(); // 解锁
        Log.d(TAG_WeChat, "解锁屏幕");
    }

    /**
     * 处理通知栏信息
     * <p>
     * 如果是微信红包的提示信息,则模拟点击
     *
     * @param event
     */
    private void handleNotification(AccessibilityEvent event) {
        List<CharSequence> texts = event.getText();
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String content = text.toString();
                //如果微信红包的提示信息,则模拟点击进入相应的聊天窗口
                if (content.contains("[微信红包]")) {
                    if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) event.getParcelableData();
                        PendingIntent pendingIntent = notification.contentIntent;
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onInterrupt() {
        Log.d(TAG_WeChat, "结束了服务");
    }

    /*
   * 模拟点击事件
   * @param nodeInfo nodeInfo
   */
    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            Log.d(TAG, "点击事件nodeInfo为空");
            return;
        }
        while (nodeInfo != null) {
            System.out.println(nodeInfo);
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 模拟返回操作
     */
    public void performBackClick() {
        Log.d(TAG_WeChat, "进行返回操作");
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    public interface OnJumpActivityListener {
        public void onJumpActivity();

        public void onToast(String money);
    }

}
