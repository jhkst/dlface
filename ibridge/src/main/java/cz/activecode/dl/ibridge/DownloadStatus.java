package cz.activecode.dl.ibridge;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.Collection;
import java.util.Date;


/**
 * Generic donwload status TO exported to REST.
 * This TO is updated by bridge.
 * Use method {@link #start()} before downloading and {@link #end()} after download finises.
 * These methods are used to count download time.
 * Inherit this class to add more status information.
 */
@XmlRootElement
public class DownloadStatus {

    private DlId dlId;
    private String name;
    private String originalUrl;

    private Collection<File> files;

    private volatile long totalSize;
    private volatile long downloadedSize;

    private long timerStartNano;
    private long timerEndNano;

    private volatile double speed;
    private volatile long estTimeNano;

    private volatile float progress;

    private Date startTime;
    private Date endTime;

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

    public long getTimerStart() {
        return timerStartNano / 1_000_000_000L;
    }

    public void setTimerStart(long timerStart) {
        this.timerStartNano = timerStart * 1_000_000_000L;
    }

    public void setTimerStartNano(long timerStartNano) {
        this.timerStartNano = timerStartNano;
    }

    public long getTimerEnd() {
        return timerEndNano / 1_000_000_000;
    }

    public void setTimerEnd(long timerEnd) {
        this.timerEndNano = timerEnd * 1_000_000_000L;
    }

    public void setTimerEndNano(long timerEndNano) {
        this.timerEndNano = timerEndNano;
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
        return estTimeNano / 1_000_000_000L;
    }

    public void setEstTime(long estTime) {
        this.estTimeNano = estTime * 1_000_000_000L;
    }

    public void setEstTimeNano(long estTimeNano) {
        this.estTimeNano = estTimeNano;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    /**
     * Computes estimation time from start time, actual time, total and downloaded bytes
     * @param startTime start time
     * @param now actual time
     * @param downloaded actually downloaded bytes
     * @param total total bytes of downloaded file
     * @return time to finish download
     */
    public static long computeEstTime(long startTime, long now, long downloaded, long total) {
        return  (long) (((double)total - downloaded)/downloaded * (now - startTime));
    }

    /**
     * Start download time couter.
     */
    public void start() {
        this.timerStartNano = System.nanoTime();
        this.startTime = new Date();
    }

    /**
     * End download time counter.
     */
    public void end() {
        this.timerEndNano = System.nanoTime();
        this.endTime = new Date();
    }

    public void setStartTime(long startTime) {
        this.startTime = new Date(startTime);
    }

    public void setEndTime(long endTime) {
        this.endTime = new Date(endTime);
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public long timerStartNano() {
        return timerStartNano;
    }
}
