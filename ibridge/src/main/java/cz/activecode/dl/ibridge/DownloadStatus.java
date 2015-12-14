package cz.activecode.dl.ibridge;

import cz.activecode.dl.templater.TemplatedBean;

import java.io.File;
import java.util.Collection;

public class DownloadStatus extends TemplatedBean {

    private DlId dlId;
    private String name;
    private String originalUrl;

    private Collection<File> files;

    private volatile long totalSize;
    private volatile long downloadedSize;

    private long startTime;
    private long finishTime;

    private volatile double speed;
    private volatile long estTime;
    private volatile float progress;

    public DlId getDlId() {
        return dlId;
    }

    public void setDlId(DlId dlId) {
        this.dlId = dlId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public Collection<File> getFiles() {
        return files;
    }

    public void setFiles(Collection<File> files) {
        this.files = files;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setStartTimeNano(long startTimeNano) {
        this.startTime = startTimeNano / 1_000_000_000L;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public void setFinishTimeNano(long finishTimeNano) {
        this.finishTime = finishTimeNano / 1_000_000_000L;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setSpeedNano(double speedNano) {
        this.speed = speedNano * 1_000_000_000d;
    }

    public long getEstTime() {
        return estTime;
    }

    public void setEstTime(long estTime) {
        this.estTime = estTime;
    }

    public void setEstTimeNano(long estTimeNano) {
        this.estTime = estTimeNano / 1_000_000_000L;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }
}
