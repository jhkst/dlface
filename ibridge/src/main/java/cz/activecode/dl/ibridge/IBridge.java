package cz.activecode.dl.ibridge;

import cz.activecode.dl.ibridge.exceptions.DownloadNotStartedException;
import cz.activecode.dl.utils.IOUtil;

import java.io.File;

/**
 * Main Bridge interface.
 * Every bridge have to extend this interface.
 */
public interface IBridge extends DownloadStatusUpdateObservable {

    /**
     * Uploaded file info for download processing
     */
    class UploadedFileInfo {
        private final File localFile;
        private final String originalFilename;

        public UploadedFileInfo(File localFile, String originalFilename) {
            this.localFile = localFile;
            this.originalFilename = originalFilename;
        }

        /**
         * Local file, where the uploaded data are stored
         * @return file, where the uploaded data are stored
         */
        public File getLocalFile() {
            return localFile;
        }

        /**
         * Original filename posted by client.
         * @return the original filename
         */
        public String getOriginalFilename() {
            return originalFilename;
        }
    }

    /**
     * Returns whether this bridge can handle downloading this type of url
     * @param url the url
     * @return {@code true} if bridge is able to download from given url
     */
    boolean acceptsUrl(String url);

    /**
     * Returns wheter this bridge can handle processing this type of file.
     * E.g. for uploaded torrent files.
     * @param fileType file type (see {@link IOUtil#getFileType(File)}
     * @return {@code true} if bridge is able to process give file type
     */
    boolean acceptsFileType(IOUtil.FileType fileType);

    /**
     * Adds url to process by this ibridge.
     * @param url the url to be processed
     * @param saveToPath folder, where to save the downloaded file
     * @return {@link DownloadFuture} to manipulate with download process.
     * @throws DownloadNotStartedException if the download does not started.
     */
    DownloadFuture addDownload(String url, File saveToPath) throws DownloadNotStartedException;

    /**
     * Adds this file to be processed by this ibridge.
     *
     * @param source file info source (see {@link UploadedFileInfo}
     * @param saveToPath folder, where to save the downloaded file
     * @return {@link DownloadFuture} to manipulate with download process.
     * @throws DownloadNotStartedException if the download does not started.
     */
    DownloadFuture addDownload(UploadedFileInfo source, File saveToPath) throws DownloadNotStartedException;

}
