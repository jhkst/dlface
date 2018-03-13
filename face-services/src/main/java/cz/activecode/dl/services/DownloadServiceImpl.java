package cz.activecode.dl.services;

import cz.activecode.dl.ibridge.*;
import cz.activecode.dl.ibridge.exceptions.DownloadNotStartedException;
import cz.activecode.dl.to.AlertMessage;
import cz.activecode.dl.utils.IOUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DownloadServiceImpl implements DownloadsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadServiceImpl.class);

    private static final AtomicInteger THREAD_ID_GEN = new AtomicInteger();
    private ExecutorService addDownloadExecutorService = Executors.newCachedThreadPool(r -> new Thread(r, "add-download-" + THREAD_ID_GEN.getAndIncrement()));
    private DownloadStatusUpdateCallback downloadStatusUpdateCallback = new DownloadStatusUpdateCallback() {
        @Override
        public void updateStatus(DownloadStatus downloadStatus) {
            statuses.put(downloadStatus.getDlId(), downloadStatus);
        }

        @Override
        public void finishStatus(DownloadStatus downloadStatus) {
            statuses.remove(downloadStatus.getDlId(), downloadStatus);
            finished.put(downloadStatus.getDlId(), downloadStatus);
        }
    };

    private PostDownloadProcess postProcess = new PostDownloadProcess() {

        @Override
        public void postProcess(Collection<File> downloadedFiles) {
            // change ACL
            Set<PosixFilePermission> savePermissions = globalConfig.getSavePermissions();
            if(savePermissions != null) {
                for (File file : downloadedFiles) {
                    IOUtil.setFilePermission(file, savePermissions);
                }
            }
        }
    };

    private Map<DlId, DownloadFuture> downloadFutures = new ConcurrentHashMap<>();

    private BridgeService bridgeService;

    private AlertMessageService alertMessageService;

    private GlobalConfig globalConfig;

    private final Map<DlId, DownloadStatus> statuses = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<DlId, DownloadStatus> finished = Collections.synchronizedMap(new LinkedHashMap<>());

    @Autowired
    public void setBridgeService(BridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    @Autowired
    public void setAlertMessageService(AlertMessageService alertMessageService) {
        this.alertMessageService = alertMessageService;
    }

    @Autowired
    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    /**
     * Bean initialization. See spring context.
     */
    @PostConstruct
    public void init() {
    }

    /**
     * Bean destroy. See spring context.
     */
    @PreDestroy
    public void destroy() {
        addDownloadExecutorService.shutdownNow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDownloads(List<String> urls, File path) {
        for(String url : urls) {
            try {
                FileUtils.writeStringToFile(globalConfig.getArchiveLinksFile(), url + "\n", StandardCharsets.UTF_8, true);
            } catch (IOException e) {
                LOGGER.warn("Cannot archive URL {} to {}", url, globalConfig.getArchiveLinksFile());
            }

            for(IBridge bridge : bridgeService.getAllBridges()) {
                if(bridge.acceptsUrl(url)) {
                    LOGGER.info("Download taken by {}", bridge.getClass().getName());
                    addDownloadExecutorService.submit(() -> {
                        try {
                            bridge.addDownloadStatusUpdater(downloadStatusUpdateCallback);
                            DownloadFuture downloadFuture = bridge.addDownload(url, path, postProcess);
                            downloadFutures.put(downloadFuture.getDlId(), downloadFuture);
                        } catch (DownloadNotStartedException e) {
                            LOGGER.error("Error enqueuing download " + url, e);
                            alertMessageService.addMessage(new AlertMessage(AlertMessage.AlertType.WARNING, e.getMessage()));
                        }
                    });
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFiles(List<IBridge.UploadedFileInfo> files, File path) {
        //file with urls
        //torrent file
        for(IBridge.UploadedFileInfo fileInfo : files) {
            try {
                IOUtil.FileType fileType = IOUtil.getFileType(fileInfo.getLocalFile());
                for(IBridge bridge : bridgeService.getAllBridges()) {
                    if(bridge.acceptsFileType(fileType)) {
                        LOGGER.info("Download taken by {}", bridge.getClass().getName());
                        addDownloadExecutorService.submit(() -> {
                            try {
                                bridge.addDownloadStatusUpdater(downloadStatusUpdateCallback);
                                DownloadFuture downloadFuture = bridge.addDownload(fileInfo, path, postProcess);
                                downloadFutures.put(downloadFuture.getDlId(), downloadFuture);
                            } catch (DownloadNotStartedException e) {
                                LOGGER.error("Error enqueuing download " + fileInfo.getOriginalFilename(), e);
                                alertMessageService.addMessage(new AlertMessage(AlertMessage.AlertType.WARNING, e.getMessage()));
                            }
                        });
                        break;
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("Cannot determine file type for " + fileInfo.getLocalFile(), e);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelDownload(DlId dlId) {
        LOGGER.debug("Canceling download " + dlId);
        DownloadFuture downloadFuture = downloadFutures.get(dlId);
        if(downloadFuture != null) {
            LOGGER.debug("Cancel download future found: " + downloadFuture);
            downloadFuture.cancel(); //todo: remove from map, also remove after download itself
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DownloadStatus> getDownloads() {
        return new ArrayList<>(statuses.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DownloadStatus> getFinishedDownloads() {
        return new ArrayList<>(finished.values());
    }
}
