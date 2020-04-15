package com.xt.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.CloseUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.widget.LinearLayout.VERTICAL;

/**
 * @author xt on 2019/12/18 14:52
 */
public class MyUtils {
    private static final String TAG = MyUtils.class.getSimpleName();

    private MyUtils() {
    }

    /**
     * 请求安装权限8.0 |<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />|
     *
     * @param context
     * @param apkFile
     */
    public static void installApk(Context context, File apkFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void saveParcelable(Parcelable parcelable) {
        FileOutputStream fos;
        try {
            fos = Utils.getApp().openFileOutput(TAG,
                                                Context.MODE_PRIVATE);
            BufferedOutputStream bos    = new BufferedOutputStream(fos);
            Parcel               parcel = Parcel.obtain();
            parcel.writeParcelable(parcelable, 0);

            bos.write(parcel.marshall());
            bos.flush();
            bos.close();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T extends Parcelable> T loadParcelable(Class<T> tClass) {
        FileInputStream fis;
        try {
            fis = Utils.getApp().openFileInput(TAG);
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(bytes, 0, bytes.length);
            parcel.setDataPosition(0);

            T data = parcel.readParcelable(tClass.getClassLoader());
            fis.close();
            return data;
        } catch (Exception e) {
            LogUtils.d(e.toString());
            return null;
        }
    }

    public String getAssetsFilePath(String fileName) {
        return String.format("%s%s", "file://android_asset/", fileName);
    }

    public InputStream openAssets(String fileName) {
        try {
            return Utils.getApp().getAssets().open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap getAssetsBitmap(String fileName) {
        InputStream inputStream = openAssets(fileName);
        if (inputStream == null) {
            return null;
        }
        Bitmap bitmap = ImageUtils.getBitmap(inputStream);
        CloseUtils.closeIO(inputStream);
        return bitmap;
    }

    public void playAssetsVideo(MediaPlayer mediaPlayer, String fileName) {
        AssetFileDescriptor afd;
        try {
            afd = Utils.getApp().getAssets().openFd(fileName);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUriFromDirRaw(int rawResourceId) {
        return String.format(Locale.getDefault(), "%s%s%s%d", "android.resource://", AppUtils.getAppPackageName(), "/", rawResourceId);
    }

    public InputStream openRawResource(int rawResourceId) {
        return Utils.getApp().getResources().openRawResource(rawResourceId);
    }

    /**
     * 选择浏览器打开
     *
     * @param url
     */
    public static void startChooser(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (intent.resolveActivity(Utils.getApp().getPackageManager()) != null) {
            Utils.getApp().startActivity(Intent.createChooser(intent, "请选择浏览器"));
        }
    }

    /**
     * 系统浏览器打开
     * @param context
     * @param url
     */
    public static void goToSystemBrower(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        context.startActivity(intent);
    }

    /**
     * @param context
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isActivityAvaible(Context context) {
        if ((context instanceof Activity) && (ActivityUtils.getActivityList().contains(context)) && !((Activity) context).isFinishing()) {
            boolean isActivityAvaible = !(((Activity) context).isDestroyed());
            if (context instanceof FragmentActivity) {
                boolean isFragmentManagerAvaible = !(((FragmentActivity) context).getSupportFragmentManager().isDestroyed());
                return (isActivityAvaible && isFragmentManagerAvaible);
            } else {
                return isActivityAvaible;
            }
        }
        return false;
    }

    public static boolean isFragmentAvaible(Fragment fragment) {
        if (fragment != null) {
            if (fragment.isAdded()) {
                if (!fragment.isRemoving()) {
                    if (!fragment.isDetached()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static int getColorValue(int colorRes) {
        return ContextCompat.getColor(Utils.getApp(), colorRes);
    }

    /**
     * @param value        double或float类型的数值
     * @param newScale     保留的位数
     * @param roundingMode {@link BigDecimal#ROUND_DOWN}直接保留，不四舍五入       {@link BigDecimal#ROUND_HALF_UP}四舍五入
     * @return
     */
    public static String keepDecimalFloat(String value, int newScale, int roundingMode) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(newScale, roundingMode).toString();
    }

    public static String bytes2String(byte[] bytes) {
        char[] chars = ConvertUtils.bytes2Chars(bytes);
        return String.valueOf(chars);
    }

    /**
     * 字符串转成十六进制字符串
     *
     * @param string
     * @return
     */
    public static String string2HexString(String string) {
        if (string == null) {
            return "";
        }
        byte[] bytes = ConvertUtils.chars2Bytes(string.toCharArray());
        return ConvertUtils.bytes2HexString(bytes);
    }

    /**
     * 使用1字节就可以表示b
     * hex8表示8位二进制
     *
     * @param b
     * @return
     */
    public static String intTo1Byte(int b) {
        //2表示需要两个16进行数
        return String.format("%02x", b);
    }

    /**
     * 需要使用2字节表示b
     * hex16表示16位二进制
     *
     * @param b
     * @return
     */
    public static String intTo2Byte(int b) {
        return String.format("%04x", b);
    }

    /**
     * 需要使用4字节表示b
     * hex32表示32位二进制
     *
     * @param b
     * @return
     */
    public static String intTo4Byte(int b) {
        return String.format("%08x", b);
    }

    /**
     *
     * @param bytes
     * @param hasSymbol true的时候有负数，false的时候没有负数
     * @return
     */
    public static int byteArrayToInt(byte[] bytes, boolean hasSymbol) {
        int value = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (i == 0) {
                if (hasSymbol) {
                    value = (value | bytes[i]);
                } else {
                    value = (value | (bytes[i] & 0xff));
                }
            } else {
                value = (value << (i * 8)) | (bytes[i] & 0xff);
            }
        }
        return value;
    }

    public static byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    public static byte[] longToByteArray(long value) {
        return new byte[]{
                (byte) ((value >> (7 * 8)) & 0xFF),
                (byte) ((value >> (6 * 8)) & 0xFF),
                (byte) ((value >> (5 * 8)) & 0xFF),
                (byte) ((value >> (4 * 8)) & 0xFF),
                (byte) ((value >> (3 * 8)) & 0xFF),
                (byte) ((value >> (2 * 8)) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    private static Bitmap zoomImage(Bitmap bm, int newWidth, int newHeight) {
        //获得图片的宽高
        int width  = bm.getWidth();
        int height = bm.getHeight();
        //计算缩放比例
        float scaleWidth  = (float) newWidth / width;
        float scaleHeight = (float) newHeight / height;
        //取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        //得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    public static Bitmap zoomImage(int restId, int newWidth, int newHeight) {
        Bitmap bm = BitmapFactory.decodeResource(
                Utils.getApp().getResources(), restId);
        return zoomImage(bm, newWidth, newHeight);

    }

    /**
     * 从.mp4的url视频中获取第一帧
     *
     * @param url
     * @return
     */
    private Bitmap getBitmapFromVideo(String url) {
        Bitmap                 bitmap    = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(url, new HashMap<>(16));
            bitmap = retriever.getFrameAtTime();
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        return bitmap;
    }

    public static String getNowTimeString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        return simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }

    public static void requestAudioFocus(){
        AudioManager audioManager = (AudioManager) Utils.getApp().getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {

            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
    }

    public static void setView(View view){
        view.setFitsSystemWindows(false);
    }
    public static void setRecyclerView(RecyclerView recyclerView){
        recyclerView.setClipToPadding(false);
        recyclerView.setClipChildren(false);
    }

    public static String timeFormatDateTime(long timeSeconds) {
        String DateTimes = null;
        long   days      = timeSeconds / (60 * 60 * 24);
        long   hours     = (timeSeconds % (60 * 60 * 24)) / (60 * 60);
        long   minutes   = (timeSeconds % (60 * 60)) / 60;
        long   seconds   = timeSeconds % 60;
        if (days > 0) {
            DateTimes = days + "天" + hours + "小时" + minutes + "分钟"
                    + seconds + "秒";
        } else if (hours > 0) {
            DateTimes = hours + "小时" + minutes + "分钟"
                    + seconds + "秒";
        } else if (minutes > 0) {
            DateTimes = minutes + "分钟"
                    + seconds + "秒";
        } else {
            DateTimes = seconds + "秒";
        }

        return DateTimes;
    }

    public static String getMinutesSeconds(long milliseconds) {
        int  minutes = (int) (milliseconds / (1000 * 60));
        long seconds = ((milliseconds / 1000) % 60);
        return String.format("%d:%d", minutes, seconds);
    }

    public static final class FragmentUtils {
        public static FragmentManager getSupportFragmentManager(@NonNull FragmentActivity fragmentActivity) {
            return fragmentActivity.getSupportFragmentManager();
        }

        public static Fragment addFragment(Context context, @NonNull FragmentManager fragmentManager, Class<? extends Fragment> fragmentClass, int containerViewId, String tag, Bundle args) {
            Fragment            fragment = Fragment.instantiate(context, fragmentClass.getName(), args);
            FragmentTransaction ft       = fragmentManager.beginTransaction();
            ft.add(containerViewId, fragment, tag).commitAllowingStateLoss();
            return fragment;
        }

        private static FragmentTransaction beginTransaction(FragmentManager fragmentManager) {
            return fragmentManager.beginTransaction();
        }

        public static Fragment hideFragment(FragmentManager fragmentManager, @NonNull Fragment fragment) {
            if (fragment.isHidden()) {
                return fragment;
            }
            beginTransaction(fragmentManager).hide(fragment).commitAllowingStateLoss();
            return fragment;
        }

        private static Fragment showFragment(FragmentManager fragmentManager, @NonNull Fragment fragment) {
            if (fragment.isHidden()) {
                beginTransaction(fragmentManager).show(fragment).commitAllowingStateLoss();
            }
            return fragment;
        }


        public static void showFragment(Context context, @NonNull FragmentManager fragmentManager, View container, int position, List<Class<? extends Fragment>> fragmentClassList, Bundle args) {
            int containerViewId = container.getId();
            for (int i = 0; i < fragmentClassList.size(); i++) {
                String   tagPrefix = fragmentClassList.get(i).getSimpleName();
                Fragment fragment  = fragmentManager.findFragmentByTag(tagPrefix + i);
                if (fragment != null) {
                    if (i != position) {
                        hideFragment(fragmentManager, fragment);
                    }
                }
            }
            {
                String   tagPrefix    = fragmentClassList.get(position).getSimpleName();
                String   showTag      = tagPrefix + position;
                Fragment showFragment = fragmentManager.findFragmentByTag(showTag);
                if (showFragment == null) {
                    showFragment = addFragment(context, fragmentManager, fragmentClassList.get(position), containerViewId, showTag, args);
                }
                showFragment(fragmentManager, showFragment);
            }
        }

        public static Fragment findFragment(int position, Class<? extends Fragment> fragmentClass, FragmentManager fragmentManager) {
            String tagPrefix = fragmentClass.getSimpleName();
            return fragmentManager.findFragmentByTag(tagPrefix + position);
        }
    }

    public static class PortraitSizeUtils {
        private PortraitSizeUtils() {
        }

        public static int getHorizontalSize720(int horizontalSize) {
            return (int) (ScreenUtils.getScreenWidth() * horizontalSize / 720f);
        }

        public static int getVerticalSize1280(int verticalSize) {
            return (int) (ScreenUtils.getScreenHeight() * verticalSize / 1280f);
        }

        public static void setViewSizePixels720x1280(View view, int w, int h) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = getHorizontalSize720(w);
            layoutParams.height = getVerticalSize1280(h);
            view.setLayoutParams(layoutParams);
        }


        public static void setViewWidthPixels720(View view, int w) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = getHorizontalSize720(w);
            view.setLayoutParams(layoutParams);
        }

        public static void setViewHeightPixels1280(View view, int height) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = getVerticalSize1280(height);
            view.setLayoutParams(layoutParams);
        }

        public static void setMarginsPixels720x1280(View view, int left, int top, int right, int bottom) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.leftMargin = getHorizontalSize720(left);
            layoutParams.topMargin = getVerticalSize1280(top);
            layoutParams.rightMargin = getHorizontalSize720(right);
            layoutParams.bottomMargin = getVerticalSize1280(bottom);
            view.setLayoutParams(layoutParams);
        }

        public static void setPaddingPixels720x1280(View view, int left, int top, int right, int bottom) {
            view.setPadding(getHorizontalSize720(left), getVerticalSize1280(top), getHorizontalSize720(left), getVerticalSize1280(bottom));
        }
    }

    public static class LandscapeSizeUtils {
        private LandscapeSizeUtils() {
        }

        public static int getHorizontalSize1280(int horizontalSize) {
            return (int) (ScreenUtils.getScreenHeight() * horizontalSize / 1280f);
        }

        public static int getVerticalSize720(int verticalSize) {
            return (int) (ScreenUtils.getScreenWidth() * verticalSize / 720f);
        }


        public static void setViewSizePixels1280x720(View view, int w, int h) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = getHorizontalSize1280(h);
            layoutParams.height = getVerticalSize720(w);
            view.setLayoutParams(layoutParams);
        }


        public static void setViewWidthPixels1280(View view, int w) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = getHorizontalSize1280(w);
            view.setLayoutParams(layoutParams);
        }

        public static void setViewHeightPixels720(View view, int height) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = getVerticalSize720(height);
            view.setLayoutParams(layoutParams);
        }

        public static void setMarginsPixels1280x720(View view, int left, int top, int right, int bottom) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.leftMargin = getHorizontalSize1280(left);
            layoutParams.topMargin = getVerticalSize720(top);
            layoutParams.rightMargin = getHorizontalSize1280(right);
            layoutParams.bottomMargin = getVerticalSize720(bottom);
            view.setLayoutParams(layoutParams);
        }

        public static void setPaddingPixels1280x720(View view, int left, int top, int right, int bottom) {
            view.setPadding(getHorizontalSize1280(left), getVerticalSize720(top), getHorizontalSize1280(left), getVerticalSize720(bottom));
        }
    }

    public static final class ShapeUtil {
        /**
         * 绘制圆角矩形 drawable
         *
         * @param fillColor    图形填充色
         * @param cornerRadius 图形圆角半径
         * @return 圆角矩形
         */
        public static GradientDrawable drawRoundRect(int fillColor, int cornerRadius) {
            GradientDrawable roundRect = new GradientDrawable();
            roundRect.setShape(GradientDrawable.RECTANGLE);
            roundRect.setColor(fillColor);
            roundRect.setCornerRadius(cornerRadius);
            return roundRect;
        }

        /**
         * 绘制圆角矩形 drawable
         *
         * @param fillColor    图形填充色
         * @param cornerRadius 图形圆角半径
         * @param strokeWidth  边框的大小
         * @param strokeColor  边框的颜色
         * @return 圆角矩形
         */
        public static GradientDrawable drawRoundRect(int fillColor, int cornerRadius, int strokeWidth, int strokeColor) {
            GradientDrawable roundRect = drawRoundRect(fillColor, cornerRadius);
            roundRect.setCornerRadius(cornerRadius);
            roundRect.setStroke(strokeWidth, strokeColor);
            return roundRect;
        }

        /**
         * 绘制圆角矩形 drawable
         *
         * @param fillColor   图形填充色
         * @param cornerRadii 图形圆角半径
         * @param strokeWidth 边框的大小
         * @param strokeColor 边框的颜色
         * @return 圆角矩形
         */
        public static GradientDrawable drawRoundRect(int fillColor, float[] cornerRadii, int strokeWidth, int strokeColor) {
            GradientDrawable roundRect = new GradientDrawable();
            roundRect.setShape(GradientDrawable.RECTANGLE);
            roundRect.setColor(fillColor);
            roundRect.setCornerRadii(cornerRadii);
            roundRect.setStroke(strokeWidth, strokeColor);
            return roundRect;
        }

        /**
         * 绘制圆形
         *
         * @param fillColor   图形填充色
         * @param size        图形的大小
         * @param strokeWidth 边框的大小
         * @param strokeColor 边框的颜色
         * @return 圆形
         */
        public static GradientDrawable drawCircle(int fillColor, int size, int strokeWidth, int strokeColor) {
            GradientDrawable circle = drawCircle(fillColor, strokeWidth, strokeColor);
            circle.setSize(size, size);
            return circle;
        }

        /**
         * 绘制圆形
         *
         * @param fillColor   图形填充色
         * @param strokeWidth 边框的大小
         * @param strokeColor 边框的颜色
         * @return 圆形
         */
        public static GradientDrawable drawCircle(int fillColor, int strokeWidth, int strokeColor) {
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(fillColor);
            circle.setStroke(strokeWidth, strokeColor);
            return circle;
        }

        public static ShapeDrawable drawCircleShape(int fillColor, int size, float strokeWidth, int strokeColor) {
            ShapeDrawable circle = new ShapeDrawable(new OvalShape());
            Paint         paint  = circle.getPaint();
            paint.setColor(fillColor);
            circle.setIntrinsicHeight(size);
            circle.setIntrinsicWidth(size);

            paint.setStrokeWidth(strokeWidth);
            return circle;
        }
    }

    /**
     * 生成seekbar进度Drawable
     *
     * @param backgroundColor
     * @param secondaryProgressColor
     * @param progressColor
     * @return
     */
    public static Drawable generateSeekProgressDrawable(int backgroundColor, int secondaryProgressColor, int progressColor, int cornerRadius) {
        Drawable[] drawables = new Drawable[3];
        {
            GradientDrawable roundRect = ShapeUtil.drawRoundRect(backgroundColor, cornerRadius);
            roundRect.setSize(1, ConvertUtils.dp2px(10));
            drawables[0] = roundRect;
        }

        {
            GradientDrawable roundRect = ShapeUtil.drawRoundRect(secondaryProgressColor, cornerRadius);
            roundRect.setSize(1, ConvertUtils.dp2px(10));
            ClipDrawable clipDrawable = new ClipDrawable(roundRect, Gravity.LEFT, VERTICAL);
            drawables[1] = clipDrawable;
        }

        {
            GradientDrawable roundRect = ShapeUtil.drawRoundRect(progressColor, cornerRadius);
            roundRect.setSize(1, ConvertUtils.dp2px(10));
            ClipDrawable clipDrawable = new ClipDrawable(roundRect, Gravity.LEFT, VERTICAL);
            drawables[2] = clipDrawable;
        }


        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.secondaryProgress);
        layerDrawable.setId(2, android.R.id.progress);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layerDrawable.setLayerHeight(0, ConvertUtils.dp2px(10));
            layerDrawable.setLayerHeight(1, ConvertUtils.dp2px(10));
            layerDrawable.setLayerHeight(2, ConvertUtils.dp2px(10));
            layerDrawable.setLayerGravity(0, Gravity.CENTER_VERTICAL);
            layerDrawable.setLayerGravity(1, Gravity.CENTER_VERTICAL);
            layerDrawable.setLayerGravity(2, Gravity.CENTER_VERTICAL);
        }

        return layerDrawable;
    }

    /**
     * 用java代码的方式动态生成状态选择器
     */
    public static Drawable creatEnabledSelector(Context context, int enabled, int normal) {
        StateListDrawable drawable = new StateListDrawable();
        //状态,设置按下的图片
        drawable.addState(new int[]{android.R.attr.state_enabled}, ContextCompat.getDrawable(context, enabled));
        //默认状态,默认状态下的图片
        drawable.addState(new int[]{}, ContextCompat.getDrawable(context, normal));

        //根据SDK版本设置状态选择器过度动画/渐变选择器/渐变动画
        drawable.setEnterFadeDuration(500);
        drawable.setExitFadeDuration(500);

        return drawable;
    }

    /**
     * 用java代码的方式动态生成状态选择器
     */
    public static Drawable creatPressedSelector(Context context, int pressed, int normal) {
        StateListDrawable drawable = new StateListDrawable();
        //状态,设置按下的图片
        drawable.addState(new int[]{android.R.attr.state_pressed}, ContextCompat.getDrawable(context, pressed));
        //默认状态,默认状态下的图片
        drawable.addState(new int[]{}, ContextCompat.getDrawable(context, normal));

        //根据SDK版本设置状态选择器过度动画/渐变选择器/渐变动画
        drawable.setEnterFadeDuration(500);
        drawable.setExitFadeDuration(500);

        return drawable;
    }

    public static final class ActivityOrientationUtils {

        public static void setActivityLandscape(Activity activity) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        public static void setActivityPortrait(Activity activity) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        public static boolean isLandscape(AppCompatActivity appCompatActivity) {
            return getConfiguration(appCompatActivity).orientation == Configuration.ORIENTATION_LANDSCAPE;
        }

        public static boolean isPortrait(AppCompatActivity appCompatActivity) {
            return getConfiguration(appCompatActivity).orientation == Configuration.ORIENTATION_PORTRAIT;
        }

        public static Configuration getConfiguration(AppCompatActivity appCompatActivity) {
            return appCompatActivity.getResources().getConfiguration();
        }
    }

    public static ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) Utils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private static TelephonyManager getTelephonyManager() {
        return (TelephonyManager) Utils.getApp()
                .getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 需要权限|<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />|
     *
     * @return
     */
    private static NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager cm = getConnectivityManager();
        if (cm == null) {
            return null;
        }
        return cm.getActiveNetworkInfo();
    }

    /**
     * @return
     */
    public static int getNetworkType() {
        NetworkInfo networkInfo = getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return networkInfo.getSubtype();
            }
        }
        return TelephonyManager.NETWORK_TYPE_UNKNOWN;
    }
}
