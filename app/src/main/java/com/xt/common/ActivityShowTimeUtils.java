package com.xt.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * @author xt on 2020/4/14 15:15
 * 界面展示计时工具类
 */
public class ActivityShowTimeUtils {
    private static final int  STAY_MILLIS = 1000 * 2;
    private              long mStartMillis;

    public ActivityShowTimeUtils() {
        setStartMillis();
    }

    public long getStayMillis() {
        long stayMillis = System.currentTimeMillis() - mStartMillis;
        if (stayMillis >= STAY_MILLIS) {
            stayMillis = 0;
        } else {
            stayMillis = STAY_MILLIS - stayMillis;
        }
        return stayMillis;
    }

    /**
     * 在{@link Activity#onCreate(Bundle)}中调用
     */
    public void setStartMillis() {
        mStartMillis = System.currentTimeMillis();
    }

    public void startActivityDelayed(final Activity activity, final Class<? extends Activity> cls) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.startActivity(new Intent(activity, cls));
                activity.finish();
            }
        }, getStayMillis());
    }
}
