package cz.activecode.dl.torrentbridge;

import cz.activecode.dl.ibridge.GlobalConfig;
import cz.activecode.dl.utils.PropUtil;
import cz.activecode.dl.utils.PropsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class TorrentBridgeConfigPropsFile implements TorrentBridgeConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(TorrentBridgeConfigPropsFile.class);

    private PropsFile propsFile;

    private Properties properties = new Properties();

    public TorrentBridgeConfigPropsFile(GlobalConfig globalConfig) {
        try {
            File configFile = new File(globalConfig.getConfigDir(), "torrent.properties");
            propsFile = new PropsFile(configFile);
        } catch (IOException e) {
            LOGGER.error("Cannot get load torrent properties file", e);
        }
    }

    @Override
    public double getMaxDownloadRate() {
        return propsFile.getDoubleProperty("bridge.torrent.maxDownloadRate", 0);
    }

    @Override
    public double getMaxUploadRate() {
        return propsFile.getDoubleProperty("bridge.torrent.maxUploadRate", 0);
    }
}
