package cz.activecode.dl.ibridge;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;

/**
 * Global config for the application.
 */
public interface GlobalConfig {

    /**
     * Proxy for connections.
     * @return the proxy.
     */
    Proxy getProxy();

    /**
     * Target path for downloads.
     * @return path for downloads
     */
    File getDownloadPath();

    /**
     * Threshold for low space alarm (in bytes).
     * @return Threshold for low space alarm (in bytes).
     */
    long getFreeSpaceThreshold();

    /**
     * Order of bridges probe to select for download.
     * See spring beens names for name them.
     * @return Order of bridges probe to select for download.
     */
    String[] getBridgesOrder();

    /**
     * File where all chosen download urls will be stored (archived).
     * @return File where all chosen download urls will be stored (archived)
     */
    File getArchiveLinksFile();

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
    File getConfigDir() throws IOException;
}
