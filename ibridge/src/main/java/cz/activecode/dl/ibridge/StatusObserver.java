package cz.activecode.dl.ibridge;

import java.util.Collection;

public interface StatusObserver {

    Collection<DownloadStatus> getStatuses();

}
