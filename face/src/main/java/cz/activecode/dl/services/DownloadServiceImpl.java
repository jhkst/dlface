package cz.activecode.dl.services;

import cz.activecode.dl.ibridge.DlId;
import cz.activecode.dl.ibridge.DownloadStatus;
import cz.activecode.dl.ibridge.IBridge;
import cz.activecode.dl.ibridge.exceptions.DownloadNotStartedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DownloadServiceImpl implements DownloadsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadServiceImpl.class);

    private static final AtomicInteger THREAD_ID_GEN = new AtomicInteger();
    private static final long UPDATE_INTERVAL = 1000L;
    private ScheduledExecutorService updateExecutorService = Executors.newScheduledThreadPool(1, r -> new Thread(r, "update-status-" +THREAD_ID_GEN.getAndIncrement()));
    private ExecutorService addDownloadExecutorService = Executors.newCachedThreadPool(r -> new Thread(r, "add-download-" + THREAD_ID_GEN.getAndIncrement()));

    @Autowired
    private BridgeService bridgeService;

    private Map<DlId, DownloadStatus> statuses = Collections.synchronizedMap(new LinkedHashMap<>());

    public void setBridgeService(BridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("init");
        updateExecutorService.scheduleAtFixedRate((Runnable) this::updateStatuses, 0L, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void destroy() {
        updateExecutorService.shutdownNow();
        addDownloadExecutorService.shutdownNow();
    }

    public void addDownloads(List<String> urls, String path) {
        for(String url : urls) {
            for(IBridge bridge : bridgeService.getAllBridges()) {
                if(bridge.isSupported(url)) {
                    LOGGER.info("Download taken by {}", bridge.getClass().getName());
                    addDownloadExecutorService.submit((Runnable) () -> {
                        try {
                            bridge.addDownload(url, path);
                        } catch (DownloadNotStartedException e) {
                            LOGGER.error("Error enqueuing download " + url, e);
                        }
                    });
                    break;
                }
            }

        }
    }

    private void updateStatuses() {
        for(IBridge bridge : bridgeService.getAllBridges()) {
            for(DownloadStatus status : bridge.getStatusObserver().getStatuses()) {
                statuses.put(status.getDlId(), status);
            }
        }
    }

    public List<DownloadStatus> getDownloads() {
        return new ArrayList<>(statuses.values());
    }

}
