package com.xt.common.download;

/**
 * @author XuTi on 2019/5/25 14:35
 */
public class DownloadInfo {
    /**
     * 获取进度失败
     */
    public static final long   TOTAL_ERROR = -1;
    private             String url;
    private             long   total;
    private             long   progress;
    private             String fileName;
    private             String fileDir;

    public DownloadInfo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }
}
