package com.ly.grabredpaperaccessibilityservice.service;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SlptServiceInstalledReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.media.AUDIO_BECOMING_NOISY")) {
                        /* 服务开机自启动 */
            Intent service = new Intent(context, MyService.class);
            context.startService(service);
        }
    }
}