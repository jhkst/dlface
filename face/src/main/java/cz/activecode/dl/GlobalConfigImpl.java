package cz.activecode.dl;

import cz.activecode.dl.ibridge.GlobalConfig;
import cz.activecode.dl.utils.Util;
import cz.activecode.dl.utils.PropUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Properties;

/**
 * Implementation of global config
 * TODO: Migrate to PropsFile
 */
public class GlobalConfigImpl implements GlobalConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalConfigImpl.class);

    private Proxy proxy;
    private File downloadPath;
    private long freeSpaceThreshold;
    private String[] bridgesOrder = {};
    private File archiveLinksFile;

    /**
     * Bean initialization (see spring context).
     */
    @PostConstruct
    public void init() {
        File configPath;
        try {
            configPath = getConfigDir();
        } catch (IOException e) {
            LOGGER.error("Cannot set config dir", e);
            return;
        }

        File configFile;
        try {
            configFile = getConfigFile(configPath);
        } catch (IOException e) {
            LOGGER.error("Cannot get config file", e);
            return;
        }

        Properties props = new Properties();
        try(InputStream is = new BufferedInputStream(new FileInputStream(configFile))) {
            props.load(is);
        } catch (IOException e) {
            LOGGER.error("Cannot load configuration from " + configFile, e);
            return;
        }

        updateProperties(props);
    }

    /**
     *
     * @param cfgPath
     * @return
     * @throws IOException
     */
    private File getConfigFile(File cfgPath) throws IOException {
        if(cfgPath == null) {
            throw new IllegalArgumentException("Config path is not specified");
        }

        File configFile = new File(cfgPath, "config.properties");

        if(!configFile.exists()) {
            InputStream template = GlobalConfigImpl.class.getResourceAsStream("config.properties.template");
            try {
                FileUtils.copyInputStreamToFile(template, configFile);
            } catch (IOException e) {
                LOGGER.error("Cannot create config file " + configFile, e);
                throw e;
            }
        }
        return configFile;
    }

    /**
     * Returns config folder for application.
     * The folder is chosen by this order.
     * - environment variable DLFACE_CONFIG_DIR
     * - $HOME/.dlface
     *
     * If the folder does not exist then it is created.
     *
     * @return config folder
     * @throws IOException if folder cannot be used (cannot be created or is not a directory).
     */
    public File getConfigDir() throws IOException {
        final String dlfaceConfig = System.getenv("DLFACE_CONFIG_DIR");
        File configDir;
        if(dlfaceConfig == null || dlfaceConfig.trim().isEmpty()) {
            configDir = new File(System.getProperty("user.home"), ".dlface");
        } else {
            configDir = new File(dlfaceConfig);
        }

        if(!configDir.exists()) {
            if(!configDir.mkdirs()) {
                throw new IOException("Cannot create config folder " + configDir);
            }
        }
        if(!configDir.isDirectory()) {
            throw new IOException("Not a directory: " + configDir);
        }
        return configDir;
    }

    /**
     * Updates internal members from properties.
     * @param props the properties to use for members update
     */
    private void updateProperties(Properties props) {
        updateDownloadPath(PropUtil.getStringProperty(props, "dlface.downloads.targetPath", false, System.getProperty("user.home")));
        updateFreeSpaceThreshold(PropUtil.getStringProperty(props, "dlface.freeSpaceThreshold", true, null), 2*1024*1024*1024L, Long.MAX_VALUE, Long.MAX_VALUE);
        updateProxy(props.getProperty("dlface.connection.proxyHost"), PropUtil.getIntProperty(props, "dlface.connection.proxyPort", 80));
        updateBridgesOrder(PropUtil.getStringProperty(props, "dlface.bridges.order", true, "frdBridge,torrentBridge,rawBridge").split(","));
        updateArchiveLinksFile(PropUtil.getStringProperty(props, "dlface.archiveLinksFile", false, null), new File(this.downloadPath, "links.txt"));
    }

    private void updateDownloadPath(String downloadPath) {
        this.downloadPath = new File(downloadPath);
    }

    private void updateFreeSpaceThreshold(String size, long defaultSize, long overflowSize, long emptySize) {
        if(size == null || size.trim().isEmpty()) {
            this.freeSpaceThreshold = emptySize;
        }
        this.freeSpaceThreshold = Util.parseSize(size, defaultSize, overflowSize);
    }

    private void updateProxy(String host, int port) {
        if(host == null || host.trim().isEmpty()) {
            this.proxy = null;
            return;
        }
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    private void updateBridgesOrder(String[] bridges) {
        this.bridgesOrder = Arrays.copyOf(bridges, bridges.length);
    }

    private void updateArchiveLinksFile(String filePath, File defaultFile) {
        this.archiveLinksFile = filePath == null ? defaultFile : new File(filePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Proxy getProxy() {
        return proxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getDownloadPath() {
        return downloadPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFreeSpaceThreshold() {
        return freeSpaceThreshold;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getBridgesOrder() {
        return bridgesOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getArchiveLinksFile() {
        return archiveLinksFile;
    }
}
