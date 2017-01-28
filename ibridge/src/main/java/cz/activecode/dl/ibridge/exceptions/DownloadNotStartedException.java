package cz.activecode.dl.ibridge.exceptions;

/**
 * Exception during download initialization, or before the download itself starts.
 */
public class DownloadNotStartedException extends Exception {

    public DownloadNotStartedException(Throwable cause) {
        super(cause);
    }

    public DownloadNotStartedException(String message) {
        super(message);
    }

    public DownloadNotStartedException(String message, Throwable cause) {
        super(message, cause);
    }


}
