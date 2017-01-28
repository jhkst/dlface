package cz.activecode.dl.ibridge;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of {@link DownloadStatusUpdateObservable}.
 * Uses memory to collect update callbacks
 */
public class DefaultDownloadStatusUpdateObservable implements DownloadStatusUpdateObservable {

    private final List<DownloadStatusUpdateCallback> updaters = new CopyOnWriteArrayList<>();

    public void addDownloadStatusUpdater(DownloadStatusUpdateCallback downloadStatusUpdateCallback) {
        updaters.add(downloadStatusUpdateCallback);
    }

    public void notifyDownloadStatusUpdaters(DownloadStatus downloadStatus) {
        updaters.forEach(downloadStatusUpdater -> downloadStatusUpdater.updateStatus(downloadStatus));
    }

    public void finishDownloadStatusUpdaters(DownloadStatus downloadStatus) {
        updaters.forEach(downloadStatusUpdater -> downloadStatusUpdater.finishStatus(downloadStatus));
    }

}
