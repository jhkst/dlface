package cz.activecode.dl.frdbridge;

import cz.activecode.dl.ibridge.*;
import cz.activecode.dl.ibridge.exceptions.DownloadNotStartedException;
import cz.activecode.dl.utils.IOUtil;
import cz.vity.freerapid.plugins.webclient.ConnectionSettings;
import cz.vity.freerapid.plugins.webclient.DownloadClient;
import cz.vity.freerapid.plugins.webclient.DownloadState;
import cz.vity.freerapid.plugins.webclient.interfaces.ShareDownloadService;
import org.java.plugin.registry.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class FrdBridge extends DefaultDownloadStatusUpdateObservable implements IBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdBridge.class);

    private static final int CONNECTION_TIMEOUT_DEFAULT = 120 * 1000;

    private FrdPluginManager frdPluginManager;
    private GlobalConfig globalConfig;

    private static AtomicInteger THREAD_CNT = new AtomicInteger();

    private ExecutorService downloadExecutorService;

    public void setFrdPluginManager(FrdPluginManager frdPluginManager) {
        this.frdPluginManager = frdPluginManager;
    }

    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public void init() {
        downloadExecutorService = Executors.newCachedThreadPool(r -> new Thread(r, "frd-download-" + THREAD_CNT.getAndIncrement()));
    }

    public void destroy() {
        if(downloadExecutorService != null) {
            downloadExecutorService.shutdown();
        }
    }

    @Override
    public boolean acceptsUrl(String url) {
        return frdPluginManager.isSupported(url);
    }

    @Override
    public boolean acceptsFileType(IOUtil.FileType fileType) {
        return false;
    }

    @Override
    public DownloadFuture addDownload(String url, File saveToPath) throws DownloadNotStartedException {
        LOGGER.info("adding FRD download: " + url);
        PluginDescriptor plugin;
        try {
            plugin = frdPluginManager.getPluginFor(url);
            LOGGER.info("URL {} supported by: {}", url, plugin.getId());
        } catch (UnsupportedURLException e) {
            throw new DownloadNotStartedException(e);
        }
        String pluginId = plugin.getId();
        ShareDownloadService shareDownloadService = frdPluginManager.getShareDownloadService(pluginId);
        LOGGER.info("ShareDownloadService: {}", shareDownloadService);

        FrdDownloadStatus status;
        try {
            status = new FrdDownloadStatus(new URL(url), saveToPath, url);
            status.setState(DownloadState.QUEUED);
        } catch (MalformedURLException e) {
            throw new DownloadNotStartedException(e);
        }

        ConnectionSettings connectionSettings = new ConnectionSettings();
        if(globalConfig.getProxy() != null) {
            SocketAddress socketAddress = globalConfig.getProxy().address();
            if(socketAddress instanceof InetSocketAddress) {
                InetSocketAddress isa = (InetSocketAddress)socketAddress;
                connectionSettings.setProxy(isa.getHostName(), isa.getPort(), globalConfig.getProxy().type());
                //TODO: proxy user/passwd
            }
        }

        DownloadClient downloadClient = new DownloadClient();
        downloadClient.setConnectionTimeOut(CONNECTION_TIMEOUT_DEFAULT);

        downloadClient.initClient(connectionSettings);

        DlId dlId = DlId.create();

        status.setDlId(dlId);
        status.setEstTime(-1L);
        status.setProgress(-1f);
        this.notifyDownloadStatusUpdaters(status);

        Future future = downloadExecutorService.submit(() -> {
            try {
                FrdHttpFileDownloadTask frdHttpFileDownloadTask = new FrdHttpFileDownloadTask(this, shareDownloadService, status, downloadClient);

                shareDownloadService.run(frdHttpFileDownloadTask);
            } catch (Exception e) {
                LOGGER.error("FRD Download failed", e); //TODO: Download not started exception
            } finally {
                LOGGER.info("FRD download of {} finished", url);
                this.finishDownloadStatusUpdaters(status);
            }
        });

        return new DownloadFuture() {
            @Override
            public void cancel() {
                LOGGER.debug("Canceling FRD download");
                future.cancel(true);
            }

            @Override
            public DlId getDlId() {
                return dlId;
            }
        };
    }

    @Override
    public DownloadFuture addDownload(UploadedFileInfo source, File saveToPath) throws DownloadNotStartedException {
        throw new DownloadNotStartedException(new UnsupportedOperationException("FRD does not support uploaded files"));
    }
}
