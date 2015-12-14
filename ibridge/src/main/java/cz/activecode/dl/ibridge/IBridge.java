package cz.activecode.dl.ibridge;

import cz.activecode.dl.ibridge.exceptions.DownloadNotStartedException;

import java.util.Collection;

public interface IBridge {

    int getPriority();

    boolean isSupported(String url);

    void addDownload(String url, String saveToPath) throws DownloadNotStartedException;

    StatusObserver getStatusObserver();

    void addUserActionListener(UserActionListener userActionListener);

    BridgeConfig getBridgeConfig();
}
