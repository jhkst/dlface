package cz.activecode.dl.ibridge;

/**
 * Interface handling all downloads. Every bridge uses methods from this interface
 * to inform about downloading progress.
 */
public interface DownloadStatusUpdateObservable {

    /**
     * Adds new download. This is ususally called by the core application and is not needed
     * to be used by the bridge.
     * TODO: check if this is necessary in this interface (method not used by bridges)
     * @param downloadStatusUpdateCallback the callback which manages updating the download status
     */
    void addDownloadStatusUpdater(DownloadStatusUpdateCallback downloadStatusUpdateCallback);

    /**
     * Notify about download status change.
     * @param downloadStatus the download status
     */
    void notifyDownloadStatusUpdaters(DownloadStatus downloadStatus);

    /**
     * Notify about finished download.
     * @param downloadStatus the download status
     */
    void finishDownloadStatusUpdaters(DownloadStatus downloadStatus);
}
