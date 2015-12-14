package cz.activecode.dl.ibridge.exceptions;

/**
 * Created by honza on 5.12.15.
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
