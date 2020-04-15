package com.xt.common.location;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * created by XuTi on 2019/5/27 11:51
 */
public class MyLocationUtils {

    private static final String TAG = MyLocationUtils.class.getSimpleName();

    private static double EARTH_RADIUS = 6378.137;

    public static LocationManager getLocationManager() {
        return (LocationManager) Utils.getApp().getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * 判断位置信息是否使用
     * @return
     */
    public static boolean isProviderEnabled() {
        LocationManager locationManager = getLocationManager();
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private static void go2SetLocation(Activity activity) {
        //返回开启GPS导航设置界面
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivityForResult(intent, 0);
    }

    /**
     * 使用位置信息
     *
     * @return
     */
    public static boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return getLocationManager().isLocationEnabled();
        }
        return isProviderEnabled();
    }

    /**
     * 使用位置信息
     *
     * @return
     */
    public static boolean isGpsEnabled() {
        int         locationMode = 0;
        String      locationProviders;
        Application application  = Utils.getApp();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(application.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(application.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public static String getBestProvider() {
        // 定义Criteria对象
        Criteria criteria = new Criteria();

        // 设置定位精确度 Criteria.ACCURACY_COARSE 比较粗略 Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        // 设置是否需要海拔信息 Altitude
        criteria.setAltitudeRequired(true);

        // 设置是否需要全方位信息 Bearing
        criteria.setBearingRequired(true);

        // 设置是否运行运行商收费
        criteria.setCostAllowed(true);

        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        //获取GPS信息提供者
        String bestProvider = getLocationManager().getBestProvider(criteria, true);
        LogUtils.i(TAG, "bestProvider = " + bestProvider);
        return bestProvider;
    }

    public static Location getLastKnownLocation() {
        // 通过GPS主动获取定位信息
        if (ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LogUtils.d("未获取运行时定位权限");
            return null;
        }else{
            return getLocationManager().getLastKnownLocation(getBestProvider());
        }
    }

    public static void listenGnssStatus(GnssStatus.Callback callback) {
        final LocationManager locationManager = getLocationManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.registerGnssStatusCallback(callback);
            }
        }
    }

    public static void listenGnssStatus(GpsStatus.Listener listener) {
        final LocationManager locationManager = getLocationManager();
        if (ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.addGpsStatusListener(listener);
    }

    /**
     * @param provider         {@link LocationManager#GPS_PROVIDER, LocationManager#NETWORK_PROVIDER}
     * @param minTime          持续定位周期
     * @param locationListener
     */
    public static void startLocation(String provider, long minTime, LocationListener locationListener) {
        final LocationManager locationManager = getLocationManager();
        if (ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, minTime, 0, locationListener);
    }

    public static void removeLocationListener(LocationListener locationListener) {
        getLocationManager().removeUpdates(locationListener);
    }

    public static int getSatelliteNumber() {
        //获取当前状态
        if (ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        GpsStatus gpsStatus = getLocationManager().getGpsStatus(null);
        if (gpsStatus == null) {
            return 0;
        }
        //获取卫星颗数的默认最大值
        int maxSatellites = gpsStatus.getMaxSatellites();
        //获取所有的卫星
        Iterator<GpsSatellite> satelliteIterator = gpsStatus.getSatellites().iterator();
        //卫星颗数统计
        int                count            = 0;
        StringBuilder      sb               = new StringBuilder();
        List<GpsSatellite> gpsSatelliteList = new ArrayList<>();
        while (satelliteIterator.hasNext() && count <= maxSatellites) {
            GpsSatellite gpsSatellite = satelliteIterator.next();
            gpsSatelliteList.add(gpsSatellite);
            count++;
            //卫星的信噪比
            float snr = gpsSatellite.getSnr();
            sb.append("第").append(count).append("颗").append("：").append(snr).append("\n");
        }
        return gpsSatelliteList.size();
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * Lat1 Lng1 表示A点经纬度，Lat2 Long2 表示B点经纬度； a=Lat1 – Lat2 为两点纬度之差 b=Lng1
     * -Lng1 为两点经度之差； 6378.137为地球半径，单位为千米；  计算出来的结果单位为千米。
     * 通过经纬度获取距离(单位：千米)
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double getDistance(double lat1, double lng1, double lat2,
                                     double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a       = radLat1 - radLat2;
        double b       = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                                                   + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        //乘以1000是换算成米
        s = s * 1000;
        return Math.abs(s);
    }

    public static int getSatelliteNumber(int event) {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            // 计算达标卫星个数
            int validSatelliteCount = 0;
            int satelliteCount      = 0;
            if (ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return 0;
            }
            GpsStatus              gpsStatus     = getLocationManager().getGpsStatus(null);
            int                    maxSatellites = gpsStatus.getMaxSatellites();
            Iterator<GpsSatellite> iterator      = gpsStatus.getSatellites().iterator();
            while (iterator.hasNext() && satelliteCount <= maxSatellites) {
                satelliteCount++;
                GpsSatellite satellite = iterator.next();
                float        nr        = satellite.getSnr();
                if (nr > 25) {
                    validSatelliteCount++;
                }
            }
            return satelliteCount;
        }
        return 0;
    }
}
