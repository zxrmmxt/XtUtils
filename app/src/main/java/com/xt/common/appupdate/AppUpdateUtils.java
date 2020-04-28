package com.xt.common.appupdate;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.xt.common.MyHttpUtils;
import com.xt.common.MyTextUtils;
import com.xt.common.MyThreadUtils;
import com.xt.common.MyUtils;
import com.xt.common.R;
import com.xt.common.download.DownLoadObserver;
import com.xt.common.download.DownloadInfo;
import com.xt.common.download.DownloadManager;

import java.io.File;
import java.util.Locale;

import io.reactivex.disposables.Disposable;

/**
 * @author xt on 2019/8/9 15:43
 * APP更新
 */
public class AppUpdateUtils {
    private static final String APK_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static boolean hasUpdate() {
        String androidPppVersion = "V1.0";
        androidPppVersion = MyTextUtils.lastSubString(androidPppVersion, "V");
        return !TextUtils.isEmpty(androidPppVersion) && !TextUtils.equals(AppUtils.getAppVersionName(), androidPppVersion) && MyHttpUtils.isUrlAvaible(getAppUrl());
    }

    /**
     * 需要权限|Manifest.permission.WRITE_EXTERNAL_STORAGE|
     *
     * @param iProgressUpdate
     * @param iComplete
     * @param iError
     */
    public static void appUpdate(IProgressUpdate iProgressUpdate, IComplete iComplete, IError iError) {
        MyThreadUtils.doBackgroundWork(new Runnable() {
            @Override
            public void run() {
                try {
                    if (NetworkUtils.isAvailable()) {
                        String appurl = getAppUrl();
                        if (FileUtils.createOrExistsDir(new File(APK_DIR))) {

                            String apkName      = getApkName(appurl);
                            String absolutePath = new File(APK_DIR, apkName).getAbsolutePath();

                            DownLoadObserver downLoadObserver = new DownLoadObserver() {

                                @Override
                                public void onSubscribe(Disposable d) {
                                    super.onSubscribe(d);
                                }

                                @Override
                                public void onNext(DownloadInfo downloadInfo) {
                                    super.onNext(downloadInfo);
                                    if (iProgressUpdate != null) {
                                        iProgressUpdate.onProgressUpdate(downloadInfo);
                                    }
                                }

                                @Override
                                public void onComplete() {
                                    if (iComplete != null) {
                                        iComplete.onComplete(absolutePath);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    //|java.net.SocketException: Software caused connection abort|,网络断开

                                    //需要申请权限
                                    if (e.toString().contains("Permission denied")) {
                                        ToastUtils.showShort("请到权限设置界面申请权限");
                                        return;
                                    }

                                    //|java.net.SocketException: Socket closed|取消下载了
                                    if (DownloadManager.getInstance().getCall(appurl) == null && e.toString().contains("Socket closed")) {
                                        return;
                                    }

                                    ToastUtils.showShort("APP下载更新包失败：" + e.toString());

                                    if (iError != null) {
                                        iError.onError(e);
                                    }
                                }
                            };

                            if (DownloadManager.getInstance().isFileDownloaded(absolutePath, appurl)) {
                                //已下载完成
                                downLoadObserver.onComplete();
                                return;
                            }
                            DownloadManager.getInstance().download(appurl, APK_DIR, apkName, downLoadObserver);
                        }
                    }
                } catch (Exception e) {
                    if (iError != null) {
                        iError.onError(e);
                    }
                }
            }
        });
    }

    @NonNull
    private static String getAppUrl() {
        return "http://admin.iot.cntotal.com/tools/110_V1.14_20190718151401_4897.apk";
    }

    public static void cancelUpdate() {
        DownloadManager.getInstance().cancel(getAppUrl());
    }

    public static int getProgress(DownloadInfo downloadInfo, ProgressBar progressBar) {
        return getProgress(downloadInfo, progressBar.getMax());
    }

    public static int getProgress(DownloadInfo downloadInfo, int max) {
        double percent = ((double) downloadInfo.getProgress()) / downloadInfo.getTotal();
        return (int) (max * percent);
    }

    public static String getPercentStr(double percent) {
        return String.format(Locale.getDefault(), "%d%s", ((int) (percent * 100d)), "%");
    }

    public static double getPercent(DownloadInfo downloadInfo) {
        return ((double) downloadInfo.getProgress()) / downloadInfo.getTotal();
    }

    public static String getPercent100() {
        return String.format(Locale.getDefault(), "%d%s", 100, "%");
    }


    /**
     * 显示不可以取消的对话框
     *
     * @param progressBar
     * @param textViewUpdating
     * @param onUpdateClickListener
     */
    public static void showForceUpdate(ProgressBar progressBar, TextView textViewUpdating, DialogInterface.OnClickListener onUpdateClickListener) {
        Context context = progressBar.getContext();
        AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle(getAppName(context) + "又更新咯！")
                .setMessage("")
                .setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                        textViewUpdating.setVisibility(View.VISIBLE);

                        if (onUpdateClickListener != null) {
                            onUpdateClickListener.onClick(dialog, which);
                        }
                    }
                }).setCancelable(false).create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }

    /**
     * 显示可以取消的对话框
     */
    public static void showUpdate(ProgressBar progressBar, TextView textViewUpdating, DialogInterface.OnClickListener onUpdateClickListener, DialogInterface.OnClickListener onNotUpdateClickListener) {
        Context context = progressBar.getContext();
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(getAppName(context) + "又更新咯！")
                .setMessage("")
                .setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                        textViewUpdating.setVisibility(View.VISIBLE);
                        if (onUpdateClickListener != null) {
                            onUpdateClickListener.onClick(dialog, which);
                        }
                    }
                }).setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (onNotUpdateClickListener != null) {
                            onNotUpdateClickListener.onClick(dialog, which);
                        }
                    }
                }).setCancelable(false).create();
        if (MyUtils.isActivityAvaible(context)) {
            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            TextView titleView = (TextView) alertDialog.getWindow().findViewById(R.id.alertTitle);
            titleView.setTextColor(Color.BLACK);
        }
    }

    /**
     * |<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />|
     * |<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />|
     * 8.0及以上系统需要|<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />|权限
     * <p>
     * 安卓7.0及以上需配置provider
     * <provider android:name="android.support.v4.content.FileProvider" android:authorities="${applicationId}.FileProvider" android:exported="false" android:grantUriPermissions="true"><meta-data android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/file_paths_public" /></provider>
     * <p>
     * ${appPackage}
     * ${applicationId}
     * file_paths_public.xml 文件内容如下
     * <paths><!--指定共享的文件夹范围--><external-path name="external_storage_root" path="." /></paths>
     *
     * @param context
     * @param apkFile
     */
    public static void installApk(Context context, File apkFile) {
        Uri uri;
        if (!FileUtils.isFileExists(apkFile)) {
            ToastUtils.showShort("文件不存在！");
            return;
        }

        //判断是否是AndroidN(7.0)以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", apkFile);
        } else {
            uri = Uri.fromFile(apkFile);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 检查是否有未知应用来源的权限
     *
     * @param activity
     */
    public void checkInstallPermissionAndInstall(Activity activity) {
        // 如果是8.0系统
        if (Build.VERSION.SDK_INT >= 26) {
            Application application = Utils.getApp();
            boolean     b           = application.getPackageManager().canRequestPackageInstalls();
            // 如果已经打开了安装未知来源的开关
            if (b) {

            } else {
                // 请求打开安装未知应用来源的界面,非运行时权限
                Uri    packageURI                          = Uri.parse("package:" + application.getPackageName());
                Intent intent                              = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                int    REQUESTCODE_GET_UNKNOWN_APP_SOURCES = 1;
                activity.startActivityForResult(intent, REQUESTCODE_GET_UNKNOWN_APP_SOURCES);
            }
        } else {

        }
    }

    /**
     * 获取APP名称
     *
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo    packageInfo    = packageManager.getPackageInfo(context.getPackageName(), 0);
            int            labelRes       = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getApkName(String apkUrl) {
        return MyTextUtils.lastSubString(apkUrl, "/");
    }

    public interface IProgressUpdate {
        void onProgressUpdate(DownloadInfo downloadInfo);
    }

    public interface IComplete {
        void onComplete(String absolutePath);
    }

    public interface IError {
        void onError(Throwable e);
    }
}
