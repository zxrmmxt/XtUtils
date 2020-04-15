package com.xt.common;

import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Patterns;

import com.blankj.utilcode.util.LogUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * @author xt on 2020/4/7 11:20
 */
public class MyHttpUtils {
    public static long getLastModified(String urlStr) {
        try {
            URL url = new URL(urlStr);
            try {
                HttpURLConnection connection   = (HttpURLConnection) url.openConnection();
                int               responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    long lastModified = connection.getLastModified();
                    LogUtils.d("getLastModified retturn:" + lastModified);
                    return lastModified;
                }
            } catch (IOException e) {
                LogUtils.d("getLastModified error:" + e.toString());
            }
        } catch (MalformedURLException e) {
            LogUtils.d("getLastModified error:" + e.toString());
        }
        return 0;
    }

    /**
     * @param videoUrl
     * @return milliseconds
     */
    public static String getVideoDurationWithMediaMetadataRetriever(String videoUrl) {
        String                               duration = null;
        android.media.MediaMetadataRetriever mmr      = new android.media.MediaMetadataRetriever();

        try {
            if (videoUrl != null) {
                HashMap<String, String> headers = new HashMap<>(1);
                headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile Safari/533.1");
                mmr.setDataSource(videoUrl, headers);
            }

            duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception ex) {
        } finally {
            mmr.release();
        }
        return duration;
    }

    public static String getVideoDurationWithMediaPlayer(String videoUrl) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(videoUrl);
            mediaPlayer.prepare();
            return mediaPlayer.getDuration() + "";
        } catch (IOException e) {
        } finally {
            mediaPlayer.release();
        }
        return null;
    }

    public static boolean isUrlAvaible(String url) {
        return !TextUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url).matches();
    }
}
