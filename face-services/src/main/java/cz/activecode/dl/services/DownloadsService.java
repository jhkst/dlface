package cz.activecode.dl.services;

import cz.activecode.dl.ibridge.DlId;
import cz.activecode.dl.ibridge.DownloadStatus;
import cz.activecode.dl.ibridge.IBridge;

import java.io.File;
import java.util.List;

/**
 * Downloads lists service.
 */
public interface DownloadsService {

    /**
     * Returns all currently processed downloads.
     * @return all currently processed downloads
     */
    List<DownloadStatus> getDownloads();

    /**
     * Returns all finished downloads.
     * @return all finished downloads
     */
    List<DownloadStatus> getFinishedDownloads();

    /**
     * Adds new downloads.
     * @param urls URLs to be added.
     * @param path path where to save downloaded files
     */
    void addDownloads(List<String> urls, File path);

    /**
     * Adds new uploaded files to be processed.
     * @param files files to be processed
     * @param path path where to save downloaded files
     */
    void addFiles(List<IBridge.UploadedFileInfo> files, File path);

    /**
     * Cancels download.
     * @param dlId id of download
     */
    void cancelDownload(DlId dlId);
}
