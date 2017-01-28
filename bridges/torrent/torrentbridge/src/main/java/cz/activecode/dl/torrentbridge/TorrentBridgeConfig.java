package cz.activecode.dl.torrentbridge;

import cz.activecode.dl.ibridge.IBridgeConfig;

public interface TorrentBridgeConfig extends IBridgeConfig {

    double getMaxDownloadRate();

    double getMaxUploadRate();

}
