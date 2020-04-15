package com.xt.common.download;


import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author XuTi on 2019/5/25 14:34
 */
public abstract class DownLoadObserver implements Observer<DownloadInfo> {
    //可以用于取消注册的监听者
    protected Disposable   d;
    protected DownloadInfo downloadInfo;

    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
    }

    @Override
    public void onNext(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        //|java.net.SocketException: Socket closed|取消下载了

        //|java.net.SocketException: Software caused connection abort|,网络断开了
    }
}
