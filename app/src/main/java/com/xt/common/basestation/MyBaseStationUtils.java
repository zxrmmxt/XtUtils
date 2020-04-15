package com.xt.common.basestation;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import java.util.List;

/**
 * created by XuTi on 2019/5/8 17:25
 */
public class MyBaseStationUtils {

    public static ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) Utils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private static TelephonyManager getTelephonyManager() {
        return (TelephonyManager) Utils.getApp()
                .getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 得到当前的手机蜂窝网络信号强度
     * 获取LTE网络和3G/2G网络的信号强度的方式有一点不同，
     * LTE网络强度是通过解析字符串获取的，
     * 3G/2G网络信号强度是通过API接口函数完成的。
     * asu 与 dbm 之间的换算关系是 dbm=-113 + 2*asu
     */
    public static void getCurrentNetDbm(final OnNetDbmCallback onNetDbmCallback) {
        final TelephonyManager tm = (TelephonyManager) Utils.getApp()
                .getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener mylistener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);

                listenNoneCellLocation(this);

                LogUtils.d("监听信号强度回调");
                String   signalInfo = signalStrength.toString();
                String[] params     = signalInfo.split(" ");


                if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                    //4G网络 最佳范围   >-90dBm 越大越好
                    int iteDbm = Integer.parseInt(params[9]);
                    onNetDbmCallback.onNetDbm(iteDbm);

                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS) {

                    //3G网络最佳范围  >-90dBm  越大越好  ps:中国移动3G获取不到  返回的无效dbm值是正数（85dbm）
                    //在这个范围的已经确定是3G，但不同运营商的3G有不同的获取方法，故在此需做判断 判断运营商与网络类型的工具类在最下方

                    //获取当前运营商
                    String yys = getOperator();

                    if ("中国移动".equals(yys)) {
                        //中国移动3G不可获取，故在此返回0
                        onNetDbmCallback.onNetDbm(0);
                    } else if ("中国联通".equals(yys)) {
                        int cdmaDbm = signalStrength.getCdmaDbm();
                        onNetDbmCallback.onNetDbm(cdmaDbm);
                    } else if ("中国电信".equals(yys)) {
                        int evdoDbm = signalStrength.getEvdoDbm();
                        onNetDbmCallback.onNetDbm(evdoDbm);
                    }

                } else {
                    //2G网络最佳范围>-90dBm 越大越好
                    int asu = signalStrength.getGsmSignalStrength();
                    int dbm = -113 + 2 * asu;
                    onNetDbmCallback.onNetDbm(dbm);
                }

            }
        };
        //开始监听
        tm.listen(mylistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        LogUtils.d("监听信号强度开始");
    }

    /**
     * 获取当前的运营商
     *
     * @return 运营商名字
     */
    public static String getOperator() {
        String           providersName    = "";
        Application      application      = Utils.getApp();
        TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
        if (ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        String imsi = telephonyManager.getSubscriberId();
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007")) {
                providersName = "中国移动";
            } else if (imsi.startsWith("46001") || imsi.startsWith("46006")) {
                providersName = "中国联通";
            } else if (imsi.startsWith("46003")) {
                providersName = "中国电信";
            }
            return providersName;
        } else {
            return "没有获取到sim卡信息";
        }
    }

    /**
     * 获取基站信息
     */
    public static void getCellInfo(Context context) {
        if (ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        /** 调用API获取基站信息 */
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        CellLocation cellLocation = telephonyManager.getCellLocation();
        if (cellLocation == null) {
            LogUtils.d("基站信息null");
            return;
        }

        int mcc = 0;
        int mnc = 0;
        int lac;
        int cid;

        String operator = telephonyManager.getNetworkOperator();
        mcc = Integer.parseInt(operator.substring(0, 3));
        mnc = Integer.parseInt(operator.substring(3));
        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
            //获取cdma基站识别标号 BID
            cid = cdmaCellLocation.getBaseStationId();
            //获取cdma网络编号NID
            lac = cdmaCellLocation.getNetworkId();
            //用谷歌API的话cdma网络的mnc要用这个getSystemId()取得→SID
            int sid = cdmaCellLocation.getSystemId();
        } else {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
            //获取gsm基站识别标号
            cid = gsmCellLocation.getCid();
            //获取gsm网络编号
            lac = gsmCellLocation.getLac();
        }
    }

    /**
     * 获取邻区基站信息
     */
    public static List<CellInfo> getNeighboringCellInfo() {
        if (ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Application               application      = Utils.getApp();
        TelephonyManager          telephonyManager = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo>            allCellInfo      = telephonyManager.getAllCellInfo();
        return allCellInfo;
    }

    /**
     * 获取基站位置信息
     *
     * @param onCellLocationCallback
     */
    public static void listenCellLocation(final OnCellLocationCallback onCellLocationCallback) {
        final TelephonyManager telephonyManager = (TelephonyManager) Utils.getApp()
                .getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener mylistener = new PhoneStateListener() {
            @Override
            public void onCellLocationChanged(CellLocation cellLocation) {
                super.onCellLocationChanged(cellLocation);

                listenNoneCellLocation(this);

                if (cellLocation == null) {
                    if (onCellLocationCallback != null) {
                        onCellLocationCallback.OnCellLocationError();
                    }
                    return;
                }

                int mcc = 0;
                int mnc = 0;
                int lac;
                int cid;

                String operator = telephonyManager.getNetworkOperator();
                if (!TextUtils.isEmpty(operator) && operator.length() > 3) {
                    mcc = Integer.parseInt(operator.substring(0, 3));
                    mnc = Integer.parseInt(operator.substring(3));
                }
                if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
                    //获取cdma基站识别标号 BID
                    cid = cdmaCellLocation.getBaseStationId();
                    //获取cdma网络编号NID
                    lac = cdmaCellLocation.getNetworkId();
                    //用谷歌API的话cdma网络的mnc要用这个getSystemId()取得→SID
                    int sid = cdmaCellLocation.getSystemId();
                } else {
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                    //获取gsm基站识别标号
                    cid = gsmCellLocation.getCid();
                    //获取gsm网络编号
                    lac = gsmCellLocation.getLac();
                }

                if (onCellLocationCallback != null) {
                    onCellLocationCallback.OnCellLocation(mcc, mnc, cid, lac);
                }
            }
        };
        //开始监听
        telephonyManager.listen(mylistener, PhoneStateListener.LISTEN_CELL_LOCATION);
    }

    public static void listenNoneCellLocation(PhoneStateListener phoneStateListener) {
        TelephonyManager telephonyManager = getTelephonyManager();
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
}
