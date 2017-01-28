package cz.activecode.dl.ibridge;

/**
 * Callback for download status update
 */
public interface DownloadStatusUpdateCallback {

    void updateStatus(DownloadStatus downloadStatus);

    void finishStatus(DownloadStatus downloadStatus);
}
