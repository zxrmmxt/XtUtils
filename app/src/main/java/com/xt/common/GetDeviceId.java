package com.xt.common;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.SPUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * @author xt on 2019/12/18 10:18
 */
public class GetDeviceId {
    /**
     * 保存文件的路径
     */
    private static final String CACHE_IMAGE_DIR   = "steel/mate/cache/devices";
    /**
     * 保存的文件 采用隐藏文件的形式进行保存
     */
    private static final String DEVICES_FILE_NAME = ".DEVICE_UNIQUE_ID";
    private static final String SP_KEY_UNIQUE_ID  = "uniqueId";

    /**
     * 获取设备唯一标识符
     * 运行时权限
     * {@link Manifest.permission#READ_PHONE_STATE}
     * {@link Manifest.permission#READ_EXTERNAL_STORAGE}
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE}
     * <p>
     * 清单文件权限
     * <uses-permission android:name="android.permission.INTERNET" />
     * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        String deviceId = "";
        {
            //读取sharedpreference值
            try {
                String spValue = SPUtils.getInstance().getString(SP_KEY_UNIQUE_ID, "");
                if (ConvertUtils.hexString2Bytes(spValue).length == 8) {
                    deviceId = spValue;
                }
            } catch (Exception e) {

            }
            if (!TextUtils.isEmpty(deviceId)) {
                if (!TextUtils.equals(deviceId, readDeviceID(context))) {
                    saveDeviceID(deviceId, context);
                }
                return deviceId;
            }
        }
        {
            //读取保存的在sd卡中的唯一标识符
            deviceId = readDeviceID(context);
            //判断是否已经生成过,
            if (!TextUtils.isEmpty(deviceId)) {
                SPUtils.getInstance().put(SP_KEY_UNIQUE_ID, deviceId,true);
                return deviceId;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        {
            //用于生成最终的唯一标识符
            try {
                //获取IMES(也就是常说的DeviceId)
                deviceId = getImei();
                stringBuilder.append(deviceId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                //获取设备的MACAddress地址 去掉中间相隔的冒号
                deviceId = getMacAddress();
                stringBuilder.append(deviceId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                deviceId = getAndroidId();
                stringBuilder.append(deviceId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            {
                boolean avaible = false;
                for (int i = 0; i < stringBuilder.length(); i++) {
                    if (stringBuilder.charAt(i) != '0') {
                        avaible = true;
                        break;
                    }
                }
                if (!avaible) {
                    stringBuilder.delete(0, stringBuilder.length());
                }
            }
        }
        {
            //如果以上搜没有获取相应的则自己生成相应的UUID作为相应设备唯一标识符
            if (stringBuilder.length() <= 0) {
                UUID uuid = UUID.randomUUID();
                deviceId = uuid.toString().replace("-", "");
                stringBuilder.append(deviceId);
            }
        }

        //为了统一格式对设备的唯一标识进行md5加密 最终生成32位字符串
        String md5 = getMd5(stringBuilder.toString(), true);
        if ((!TextUtils.isEmpty(md5)) && (md5.length() == 32)) {
            //持久化操作, 进行保存到SD卡中
            deviceId = md5.substring(8, md5.length() - 8);
            saveDeviceID(deviceId, context);
        }
        return deviceId;
    }


    /**
     * 读取固定的文件中的内容,这里就是读取sd卡中保存的设备唯一标识符
     *
     * @param context
     * @return
     */
    public static String readDeviceID(Context context) {
        File          file          = getDevicesDir(context);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream   fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            Reader            in  = new BufferedReader(isr);
            int               i;
            while ((i = in.read()) > -1) {
                stringBuilder.append((char) i);
            }
            in.close();
            String deviceId = stringBuilder.toString();
            if (ConvertUtils.hexString2Bytes(deviceId).length == 8) {
                return deviceId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    /**
     * 保存 内容到 SD卡中,  这里保存的就是 设备唯一标识符
     *
     * @param str
     * @param context
     */
    public static void saveDeviceID(String str, Context context) {
        File file = getDevicesDir(context);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file); Writer out = new OutputStreamWriter(fileOutputStream, "UTF-8")) {
            out.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getImei() {
        return PhoneUtils.getIMEI();
    }

    /**
     * 获取设备MAC 地址 由于 6.0 以后 WifiManager 得到的 MacAddress得到都是 相同的没有意义的内容
     * 所以采用以下方法获取Mac地址
     *
     * @return
     */
    private static String getMacAddress() {
        String macAddress = DeviceUtils.getMacAddress();
        if (!TextUtils.isEmpty(macAddress)) {
            macAddress = macAddress.replace(":", "");
        }
        return macAddress;
    }

    private static String getAndroidId() {
        return DeviceUtils.getAndroidID();
    }

    /**
     * 对挺特定的 内容进行 md5 加密
     *
     * @param message   加密明文
     * @param upperCase 加密以后的字符串是是大写还是小写  true 大写  false 小写
     * @return
     */
    public static String getMd5(String message, boolean upperCase) {
        String md5str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] input = message.getBytes();

            byte[] buff = md.digest(input);

            md5str = bytesToHex(buff, upperCase);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5str;
    }


    public static String bytesToHex(byte[] bytes, boolean upperCase) {
        StringBuffer md5str = new StringBuffer();
        int          digital;
        for (int i = 0; i < bytes.length; i++) {
            digital = bytes[i];

            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        if (upperCase) {
            return md5str.toString().toUpperCase();
        }
        return md5str.toString().toLowerCase();
    }

    /**
     * 统一处理设备唯一标识 保存的文件的地址
     *
     * @param context
     * @return
     */
    private static File getDevicesDir(Context context) {
        File mCropFile;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File cropdir = new File(Environment.getExternalStorageDirectory(), CACHE_IMAGE_DIR);
            if (!cropdir.exists()) {
                cropdir.mkdirs();
            }
            // 用当前时间给取得的图片命名
            mCropFile = new File(cropdir, DEVICES_FILE_NAME);
        } else {
            File cropdir = new File(context.getFilesDir(), CACHE_IMAGE_DIR);
            if (!cropdir.exists()) {
                cropdir.mkdirs();
            }
            mCropFile = new File(cropdir, DEVICES_FILE_NAME);
        }
        return mCropFile;
    }
}
