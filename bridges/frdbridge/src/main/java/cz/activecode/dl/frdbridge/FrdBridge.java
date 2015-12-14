package cz.activecode.dl.frdbridge;

import cz.activecode.dl.ibridge.BridgeConfig;
import cz.activecode.dl.ibridge.IBridge;
import cz.activecode.dl.ibridge.StatusObserver;
import cz.activecode.dl.ibridge.UserActionListener;
import cz.activecode.dl.ibridge.exceptions.DownloadNotStartedException;
import cz.vity.freerapid.plugins.webclient.interfaces.HttpFileDownloadTask;
import cz.vity.freerapid.plugins.webclient.interfaces.ShareDownloadService;
import org.java.plugin.registry.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrdBridge implements IBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdBridge.class);

    private FrdPluginManager frdPluginManager;

    public void setFrdPluginManager(FrdPluginManager frdPluginManager) {
        this.frdPluginManager = frdPluginManager;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isSupported(String url) {
        return frdPluginManager.isSupported(url);
    }

    @Override
    public void addDownload(String url, String saveToPath) throws DownloadNotStartedException {
        LOGGER.info("adding FRD download: " + url);
        PluginDescriptor plugin = null;
        try {
            plugin = frdPluginManager.getPluginFor(url);
        } catch (UnsupportedURLException e) {
            throw new DownloadNotStartedException(e);
        }
        String pluginId = plugin.getId();
        ShareDownloadService shareDownloadService = frdPluginManager.getShareDownloadService(pluginId);
        LOGGER.info("ShareDownloadService: {}", shareDownloadService);
    }

    @Override
    public StatusObserver getStatusObserver() {
        return null;
    }

    @Override
    public void addUserActionListener(UserActionListener userActionListener) {

    }

    @Override
    public BridgeConfig getBridgeConfig() {
        return null;
    }

}
