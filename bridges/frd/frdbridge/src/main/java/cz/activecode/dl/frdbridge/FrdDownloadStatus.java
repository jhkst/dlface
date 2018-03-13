package cz.activecode.dl.frdbridge;

import cz.activecode.dl.ibridge.DownloadStatus;
import cz.activecode.dl.utils.Util;
import cz.vity.freerapid.plugins.webclient.DownloadState;
import cz.vity.freerapid.plugins.webclient.FileState;
import cz.vity.freerapid.plugins.webclient.interfaces.HttpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FrdDownloadStatus extends DownloadStatus implements HttpFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdDownloadStatus.class);

    private static final int ERROR_ATTEMPTS_COUNT_DEFAULT = 5;
    private volatile DownloadState state = DownloadState.PAUSED;
    private volatile FileState fileState;
    private volatile URL fileUrl;
    private String fileName;
    private int errorAttemptsCount;
    private int sleep;
    private float averageSpeed;
    private volatile boolean resumeSupported;
    private volatile int timeToQueued;
    private volatile String fileType;
    private volatile String pluginID;
    private volatile String description;
    private volatile File saveToDirectory;
    private volatile Map<String, Object> properties = new HashMap<>();
    private volatile File storeFile;
    private volatile long downloaded;

    private long lastDownloaded;
    private long lastDownloadedTimeNano;
    private String localPluginConfig;


    public FrdDownloadStatus(URL fileUrl, File saveToDirectory, String description) {
        this.fileUrl = fileUrl;
        this.saveToDirectory = saveToDirectory;
        this.description = description;

        super.setOriginalUrl(String.valueOf(fileUrl));
    }

    @Override
    public long getFileSize() {
        return super.getTotalSize();
    }

    @Override
    public void setFileSize(long fileSize) {
        super.setTotalSize(fileSize);
    }

    @Override
    public DownloadState getState() {
        return state;
    }

    @Override
    public void setState(DownloadState state) {
        this.state = state;
    }

    @Override
    public FileState getFileState() {
        return fileState;
    }

    @Override
    public void setFileState(FileState state) {
        this.fileState = state;
    }

    @Override
    public URL getFileUrl() {
        return fileUrl;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
        super.setName(fileName);
        super.setFiles(Collections.singletonList(getFile()));
    }

    @Override
    public void setNewURL(URL fileUrl) {
        this.fileUrl = fileUrl;
        super.setTotalSize(-1);
        final String urlStr = fileUrl.toExternalForm();
        this.fileName = Util.identifyFileName(urlStr);
        setDownloaded(0);
        this.errorAttemptsCount = ERROR_ATTEMPTS_COUNT_DEFAULT;
        this.sleep = -1;
        this.averageSpeed = 0;
        super.setSpeed(0);
        this.resumeSupported = true;
        this.fileState = FileState.NOT_CHECKED;
        this.timeToQueued = -1;
        this.fileType = Util.identifyFileType(fileName);

        super.setName(this.fileName);
        super.setFiles(Collections.singletonList(getFile()));
        super.setOriginalUrl(String.valueOf(fileUrl));
    }

    @Override
    public void setPluginID(String pluginID) {
        this.pluginID = pluginID;
    }

    @Override
    public String getPluginID() {
        return pluginID;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public long getDownloaded() {
        return downloaded;
    }

    @Override
    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    @Override
    public File getSaveToDirectory() {
        return saveToDirectory;
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    @Override
    public File getStoreFile() {
        return this.storeFile;
    }

    @Override
    public void setStoreFile(File storeFile) {
        this.storeFile = storeFile;
    }

    @Override
    public long getRealDownload() {
        return super.getDownloadedSize();
    }

    @Override
    public void setResumeSupported(boolean resumeSupported) {
        this.resumeSupported = resumeSupported;
    }

    @Override
    public boolean isResumeSupported() {
        return resumeSupported;
    }

    @Override
    public String getLocalPluginConfig() {
        return localPluginConfig;
    }

    @Override
    public void setLocalPluginConfig(String localPluginConfig) {
        this.localPluginConfig = localPluginConfig;
    }

    public File getOutputFile() {
        return getFile();
    }

    public void setRealDownload(long realDownload) {
        super.setDownloadedSize(realDownload);
        super.setProgress(realDownload * 100f/ super.getTotalSize());

        long currentNano = System.nanoTime();
        long deltaTime = currentNano - lastDownloadedTimeNano;
        long deltaDownload = realDownload - lastDownloaded;

        if(deltaTime > 1000_000_000L) {
            super.setSpeedNano(((double) deltaDownload) / deltaTime);
            lastDownloaded = realDownload;
            lastDownloadedTimeNano = currentNano;
        }
    }

    private File getFile() {
        if(fileName == null) {
            return null;
        }
        return new File(this.saveToDirectory, fileName);
    }
}
