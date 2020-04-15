package com.xt.common.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author xuti on 2017/10/17.
 * 要在清单文件注册
 */

public class ClickNotificationReceiver extends BroadcastReceiver {
    public static final String DESIRED_ITEM_NAME = "realIntent";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent realIntent = intent.getParcelableExtra(DESIRED_ITEM_NAME);
        realIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(realIntent);
    }
}
