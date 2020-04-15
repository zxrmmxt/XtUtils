package com.xt.common.download;

import android.text.TextUtils;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.zhy.http.okhttp.https.HttpsUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * @author created by XuTi on 2019/5/25 14:33
 */
public class DownloadManager {
    private static final String                           TAG      = DownloadManager.class.getSimpleName();
    private static final AtomicReference<DownloadManager> INSTANCE = new AtomicReference<>();
    /**
     * 用来存放各个下载的请求
     */
    private              HashMap<String, Call>            downCalls;

    /**
     * OKHttpClient;
     */
    private OkHttpClient mClient;

    public static DownloadManager getInstance() {
        for (; ; ) {
            DownloadManager current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new DownloadManager();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    private DownloadManager() {
        downCalls = new HashMap<>();

        ClearableCookieJar cookieJar1 = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(Utils.getApp()));

        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
        mClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
//                .addInterceptor(new LoggerInterceptor("OkHttp"))//日志
                .cookieJar(cookieJar1)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .build();
    }

    /**
     * 开始下载
     *
     * @param url              下载请求的网址
     * @param downLoadObserver 用来回调的接口
     */
    public void download(String url, String fileDir, String fileName, DownLoadObserver downLoadObserver) {
        Observable.just(url)
                //call的map已经有了,就证明正在下载,则这次不下载
                .filter(s -> !downCalls.containsKey(s))
                .flatMap(s -> Observable.just(createDownInfo(s, fileDir, fileName)))
                //检测本地文件夹,生成新的文件名
                .map(this::getRealFileName)
                //下载
                .flatMap(downloadInfo -> Observable.create(new DownloadSubscribe(downloadInfo)))
                //在主线程回调
                .observeOn(AndroidSchedulers.mainThread())
                //在子线程执行
                .subscribeOn(Schedulers.io())
                //添加观察者
                .subscribe(downLoadObserver);

    }

    public void cancel(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            //取消
            call.cancel();
        }
        downCalls.remove(url);
    }

    public Call getCall(String url) {
        return downCalls.get(url);
    }

    /**
     * 创建DownInfo
     *
     * @param url 请求网址
     * @return DownInfo
     */
    private DownloadInfo createDownInfo(String url, String fileDir, String fileName) {
        DownloadInfo downloadInfo = new DownloadInfo(url);
        //获得文件大小
        long contentLength = getContentLength(url);
        downloadInfo.setTotal(contentLength);

        downloadInfo.setFileDir(fileDir);

        if (TextUtils.isEmpty(fileName)) {
            fileName = url.substring(url.lastIndexOf('/'));
        }
        downloadInfo.setFileName(fileName);
        return downloadInfo;
    }

    private DownloadInfo getRealFileName(DownloadInfo downloadInfo) {
        String fileName       = downloadInfo.getFileName();
        long   downloadLength = 0;
        long   contentLength  = downloadInfo.getTotal();
        File   file           = new File(downloadInfo.getFileDir(), fileName);
        if (file.exists()) {
            //找到了文件,代表已经下载过,则获取其长度
            downloadLength = file.length();
        }

        {
            //之前下载过,需要重新创建一个文件
            int i = 1;
            while (downloadLength >= contentLength) {
                int    dotIndex = fileName.lastIndexOf('.');
                String fileNameOther;
                if (dotIndex == -1) {
                    fileNameOther = fileName + "(" + i + ")";
                } else {
                    fileNameOther = fileName.substring(0, dotIndex)
                            + "(" + i + ")" + fileName.substring(dotIndex);
                }
                File newFile = new File(downloadInfo.getFileDir(), fileNameOther);
                file = newFile;
                downloadLength = newFile.length();
                i++;
            }
            //设置改变过的文件名/大小
            downloadInfo.setProgress(downloadLength);
            downloadInfo.setFileName(file.getName());
        }

        return downloadInfo;
    }

    private class DownloadSubscribe implements ObservableOnSubscribe<DownloadInfo> {
        private DownloadInfo downloadInfo;

        public DownloadSubscribe(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> e) throws Exception {
            String url = downloadInfo.getUrl();
            //已经下载好的长度
            long downloadLength = downloadInfo.getProgress();
            //文件的总长度
            long contentLength = downloadInfo.getTotal();
            //初始进度信息
            e.onNext(downloadInfo);

            Request request = new Request.Builder()
                    //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                    .addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength)
                    .url(url)
                    .build();
            Call call = mClient.newCall(request);
            //把这个添加到call里,方便取消
            downCalls.put(url, call);
            Response response = call.execute();
            if (response.body() == null) {
                return;
            }

            File file = new File(downloadInfo.getFileDir(), downloadInfo.getFileName());
            //缓冲数组2kB
            byte[] buffer = new byte[2048];
            int    len    = 0;

            try (FileOutputStream fileOutputStream = new FileOutputStream(file, true); InputStream is = response.body().byteStream()) {
                //unexpected end of stream解决
                while ((downloadLength != contentLength) && (len = is.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                    downloadLength += len;
                    downloadInfo.setProgress(downloadLength);
                    e.onNext(downloadInfo);
                }
                fileOutputStream.flush();
                downCalls.remove(url);
            }

            //完成
            e.onComplete();
        }
    }

    /**
     * @param downloadUrl 下载链接
     * @return 获取下载长度
     */
    private long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {
                long contentLength = 0;
                if (response.body() != null) {
                    contentLength = response.body().contentLength();
                }
                response.close();
                return contentLength == 0 ? DownloadInfo.TOTAL_ERROR : contentLength;
            }
        } catch (IOException e) {
            LogUtils.d(TAG, e.toString());
        }
        return DownloadInfo.TOTAL_ERROR;
    }

    /**
     * 判断文件是否已下载
     *
     * @param filePath
     * @param fileDownloadUrl
     * @return
     */
    public boolean isFileDownloaded(String filePath, String fileDownloadUrl) {
        if (FileUtils.isFileExists(filePath)) {
            long fileLength = FileUtils.getFileLength(filePath);

            long contentLength = getContentLength(fileDownloadUrl);

            //如果本地文件长度大于要下载的文件长度，则删除本地文件
            if (fileLength > contentLength) {
                FileUtils.delete(filePath);
            }
            return fileLength == contentLength;
        }
        return false;
    }
}
