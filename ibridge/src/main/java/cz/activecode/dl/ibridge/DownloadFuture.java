package cz.activecode.dl.ibridge;

/**
 * Interface to manipulate with downloading task/tread
 */
public interface DownloadFuture {

    /**
     * Cancel download.
     */
    void cancel();

    /**
     * Get download id
     * @return download id.
     */
    DlId getDlId();
}
