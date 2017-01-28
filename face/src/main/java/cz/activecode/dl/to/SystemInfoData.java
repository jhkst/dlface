package cz.activecode.dl.to;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SystemInfoData {

    private String appVersion;
    private long freeSpace;
    private long upTime;
    private boolean lowSpace;
    private long totalMem;
    private long maxMem;
    private long freeMem;
    private int activeThreadCount;
    private int lockedThreadCount;
    private double systemLoad;

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public long getUpTime() {
        return upTime;
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }

    public boolean isLowSpace() {
        return lowSpace;
    }

    public void setLowSpace(boolean lowSpace) {
        this.lowSpace = lowSpace;
    }

    public long getTotalMem() {
        return totalMem;
    }

    public void setTotalMem(long totalMem) {
        this.totalMem = totalMem;
    }

    public long getMaxMem() {
        return maxMem;
    }

    public void setMaxMem(long maxMem) {
        this.maxMem = maxMem;
    }

    public long getFreeMem() {
        return freeMem;
    }

    public void setFreeMem(long freeMem) {
        this.freeMem = freeMem;
    }

    public int getActiveThreadCount() {
        return activeThreadCount;
    }

    public void setActiveThreadCount(int activeThreadCount) {
        this.activeThreadCount = activeThreadCount;
    }

    public void setLockedThreadCount(int lockedThreadCount) {
        this.lockedThreadCount = lockedThreadCount;
    }

    public int getLockedThreadCount() {
        return lockedThreadCount;
    }

    public void setSystemLoad(double systemLoad) {
        this.systemLoad = systemLoad;
    }

    public double getSystemLoad() {
        return systemLoad;
    }
}
