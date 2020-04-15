package com.xt.common.notification;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.blankj.utilcode.util.Utils;
import com.xt.common.MyUtils;

import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by xuti on 2017/9/28.
 */

public class MyNotificationUtils {
    private static final String TAG = MyNotificationUtils.class.getSimpleName();

    /**
     *
     * @param realIntent 点击后打开activity，可携带数据
     * @param title
     * @param content
     * @param ticker
     * @param smallIconRes
     * @param smallIconColorRes
     * @param largeIconRes
     * @param number
     * @param layoutId
     * @param viewId
     */
    public static void showNotification(Intent realIntent, String title, String content, String ticker, Integer smallIconRes, Integer smallIconColorRes, Integer largeIconRes, Integer number, Integer layoutId, Integer viewId) {
        Application         context             = Utils.getApp();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        String              appPackageName      = context.getPackageName();

        Intent clickIntent = new Intent(context, ClickNotificationReceiver.class);
        clickIntent.putExtra(ClickNotificationReceiver.DESIRED_ITEM_NAME, realIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, appPackageName);
        if (smallIconRes != null && smallIconRes > 0) {
            //设置通知小ICON
            builder.setSmallIcon(smallIconRes);
        }
        if (largeIconRes != null && largeIconRes > 0) {
            //设置大图标
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), largeIconRes));
        }

        if (number != null) {
            //设置数字 右下角
            builder.setNumber(number);
        }

        if (layoutId != null) {
            RemoteViews contentViews = new RemoteViews(appPackageName, layoutId);
            if (viewId != null) {
                contentViews.setTextViewText(viewId, title);
            }
            builder.setContent(contentViews);
        }
        //通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
        builder.setWhen(System.currentTimeMillis())
                //设置该通知优先级
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                //设置点击 自动消失
                .setAutoCancel(true)
                //ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setOngoing(false)
                //向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合。Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 requires VIBRATE permission
                .setDefaults(Notification.DEFAULT_ALL);


        //设置标题
        builder.setContentTitle(title)
                //设置通知内容
                .setContentText(content)
                //通知首次出现在通知栏，带上升动画效果的
                .setTicker(ticker);
        //点击的意图ACTION是跳转到Intent，解决了intent传入不同的值，但是收到的都是同一个值
        clickIntent.setData(Uri.parse("custom://" + System.currentTimeMillis()));
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(100), clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        //true,将Notification变为悬挂式Notification,小米手机上未点击就跳到目标activity了
        builder.setFullScreenIntent(pendingIntent, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Utils.getApp().getPackageName(),
                    TAG,
                    NotificationManager.IMPORTANCE_DEFAULT);


            notificationManager.createNotificationChannel(channel);

        }
        builder.setChannelId(appPackageName);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            /**
             * android5.0加入了一种新的模式Notification的显示等级，共有三种：
             * 1.VISIBILITY_PUBLIC只有在没有锁屏时会显示通知
             * 2.VISIBILITY_PRIVATE任何情况都会显示通知
             * 3.VISIBILITY_SECRET在安全锁和没有锁屏的情况下显示通知
             */
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    //设置该通知优先级
                    .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                    //设置通知类别
                    .setCategory(Notification.CATEGORY_MESSAGE);

            if (smallIconColorRes != null) {
                //设置smallIcon的背景色
                builder.setColor(MyUtils.getColorValue(smallIconColorRes));
            }
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void create(int id, Intent intent, int smallIcon, String contentTitle, String contentText) {
        Application app = Utils.getApp();
        NotificationManager manager =
                (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent para disparar o broadcast
        PendingIntent p = PendingIntent.getActivity(Utils.getApp(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Cria a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(app, app.getPackageName())
                .setContentIntent(p)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(smallIcon)
                .setAutoCancel(true);

        // Dispara a notification
        Notification n = builder.build();
        manager.notify(id, n);
    }

    public static void createStackNotification(int id, String groupId, Intent intent, int smallIcon, String contentTitle, String contentText) {
        Application app = Utils.getApp();
        NotificationManager manager =
                (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent para disparar o broadcast
        PendingIntent p = intent != null ? PendingIntent.getActivity(Utils.getApp(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT) : null;

        // Cria a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(app, app.getPackageName())
                .setContentIntent(p)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(smallIcon)
                .setGroup(groupId)
                .setAutoCancel(true);

        // Dispara a notification
        Notification n = builder.build();
        manager.notify(id, n);
    }

    // Notificação simples sem abrir intent (usada para alertas, ex: no wear)
    public static void create(int smallIcon, String contentTitle, String contentText) {
        Application app = Utils.getApp();
        NotificationManager manager =
                (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

        // Cria a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(app, app.getPackageName())
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(smallIcon)
                .setAutoCancel(true);

        // Dispara a notification
        Notification n = builder.build();
        manager.notify(0, n);
    }

    public static void cancel(@Nullable String tag, final int id) {
        NotificationManagerCompat.from(Utils.getApp()).cancel(tag, id);
    }

    public static void cancel(final int id) {
        NotificationManagerCompat.from(Utils.getApp()).cancel(id);
    }

    public static void cancelAll() {
        NotificationManagerCompat.from(Utils.getApp()).cancelAll();
    }

    public static boolean areNotificationsEnabled() {
        NotificationManagerCompat manager = NotificationManagerCompat.from(Utils.getApp());
        return manager.areNotificationsEnabled();
    }

    public static void go2OpenNotifications() {
        Intent  intent  = new Intent();
        Context context = Utils.getApp();
        if (Build.VERSION.SDK_INT >= 26) {
            // android 8.0引导
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
        } else if (Build.VERSION.SDK_INT >= 21) {
            // android 5.0-7.1
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else {
            // 其他
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
