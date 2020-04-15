package com.xt.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * @author xt on 2020/1/6 13:09
 * 英文增加value-en文件夹
 * 中文简体增加values-zh文件夹
 * <p>
 * 清单文件中错误配置：
 * <activity
 * android:name=".activity.dvr.FactoryResetActivity"
 * android:configChanges="locale|orientation|keyboard|layoutDirection|screenSize"
 * android:screenOrientation="portrait" />
 * 不应该配置locale：
 * <activity
 * android:name=".activity.dvr.FactoryResetActivity"
 * android:configChanges="orientation|keyboard|layoutDirection|screenSize"
 * android:screenOrientation="portrait" />
 */
public class MyLanguageUtils {

    public static Context updateLanguage(Context context) {

        Locale locale = getCurrentLocale(context);

        if (locale == null) {
            return context;
        }

        update(context, locale);

        //设置值的代码，高版本的代码和过时的代码一起执行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context = context.createConfigurationContext(context.getResources().getConfiguration());
            update(context, locale);
        }

        return context;
    }

    private static void update(Context context, Locale locale) {
        Resources      resources      = context.getResources();
        Configuration  configuration  = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        }
        resources.updateConfiguration(configuration, displayMetrics);
    }

    /**
     * 默认:0
     * 简体中文:1
     * 英文:2
     *
     * @param context
     * @param locale
     */
    public static void saveLanguage(Context context, Locale locale) {
        SharedPreferences        preferences = context.getSharedPreferences("language", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor      = preferences.edit();
        int                      language    = 0;
        if (locale != null) {
            if (locale.equals(Locale.SIMPLIFIED_CHINESE)) {
                language = 1;
            } else if (locale.equals(Locale.ENGLISH)) {
                language = 2;
            }
        }
        editor.putInt("language", language);
        editor.commit();
    }

    /**
     * 中文分简体和繁体，英文不分是UK、US、CANADA,目前只支持简体中文和英文
     *
     * @param context
     * @return
     */
    public static Locale getCurrentLocale(Context context) {
        Locale locale = getSystemLocale(context);

        {
            SharedPreferences preferences = context.getSharedPreferences("language", Context.MODE_PRIVATE);
            int               language    = preferences.getInt("language", 0);
            switch (language) {
                case 1:
                    locale = Locale.SIMPLIFIED_CHINESE;
                    break;
                case 2:
                    locale = Locale.ENGLISH;
                    break;
                default:
                    break;
            }
        }

        if (isSimplifiedChinese(locale)) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            locale = Locale.ENGLISH;
        }

        /*{
            //当返回的语言是不支持的语言时就返回默认语言，当前为英语
            if ((!locale.equals(Locale.SIMPLIFIED_CHINESE) && !locale.equals(Locale.ENGLISH))) {
                locale = Locale.ENGLISH;
            }
        }*/

        return locale;
    }

    private static Locale getSystemLocale(Context context) {
        Locale locale;
        //返回值的代码，高版本的代码和过时的代码条件执行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //7.0有多语言设置获取顶部的语言
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        return locale;
    }

    /**
     * 打开新的activity后，关闭新的activity前面的所有activity
     *
     * @param context
     * @param activityClass
     */
    public static void restartApp(final Context context, final Class activityClass) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, activityClass);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
//                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    /**
     * 是否是简体中文
     *
     * @param context
     * @return
     */
    public static boolean isCurrentLocaleSimplifiedChinese(Context context) {
        Locale currentLocale = getCurrentLocale(context);
        return isSimplifiedChinese(currentLocale);
    }

    private static boolean isSimplifiedChinese(Locale currentLocale) {
        return TextUtils.equals(currentLocale.getLanguage(), "zh") && TextUtils.equals(currentLocale.getCountry(), "CN");
    }

    /**
     * activity onConfigurationChanged方法中调用
     *
     * @param activity
     * @param newConfig
     */
    public static void onActivityConfigurationChanged(Activity activity, Configuration newConfig) {
        activity.getResources().getConfiguration().orientation = newConfig.orientation;
        activity.getResources().getDisplayMetrics().setTo(activity.getResources().getDisplayMetrics());

        activity.getApplication().getResources().getConfiguration().orientation = newConfig.orientation;
        activity.getApplication().getResources().getDisplayMetrics().setTo(activity.getResources().getDisplayMetrics());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            activity.getResources().getConfiguration().setLocales(newConfig.getLocales());
        }
        activity.getResources().getConfiguration().locale = newConfig.locale;

        updateLanguage(activity);
    }

    /**
     * application onConfigurationChanged方法中调用
     *
     * @param application
     * @param newConfig
     */
    public static void onAppConfigurationChanged(Application application, Configuration newConfig) {
        application.getResources().getConfiguration().orientation = newConfig.orientation;
        application.getResources().getDisplayMetrics().setTo(application.getResources().getDisplayMetrics());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            application.getResources().getConfiguration().setLocales(newConfig.getLocales());
        }
        application.getResources().getConfiguration().locale = newConfig.locale;

        updateLanguage(application);
    }
}
