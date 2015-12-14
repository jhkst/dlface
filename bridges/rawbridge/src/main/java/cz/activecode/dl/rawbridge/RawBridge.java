package cz.activecode.dl.rawbridge;

import cz.activecode.dl.ibridge.*;
import cz.activecode.dl.ibridge.exceptions.DownloadNotStartedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RawBridge implements IBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawBridge.class);

    private static final int BUFFER = 16*1024;

    private static final AtomicInteger THREAD_CNTR = new AtomicInteger();
    private ExecutorService executorService = Executors.newCachedThreadPool(r -> new Thread(r, "download-thread-" + THREAD_CNTR.getAndIncrement())); //todo: dispose
    private StatusObserver statusObserver = new RawStatusObserver();
    private Map<DlId, RawDownloadStatus> downloads = new ConcurrentHashMap<>();
    private Proxy proxy;

    public RawBridge() {
    }

    public RawBridge(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public int getPriority() {
        return -100;
    }

    @Override
    public boolean isSupported(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            LOGGER.warn("Demanded url is not URL " + url);
            return false;
        }
    }

    @Override
    public void addDownload(String urlStr, String saveToPath) throws DownloadNotStartedException {
        URL url;
        RawDownloadStatus downloadStatus = new RawDownloadStatus();
        downloadStatus.setOriginalUrl(urlStr);

        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new DownloadNotStartedException(e);
        }

        executorService.submit((Runnable) () -> {
            LOGGER.info("Download initialization");
            HttpURLConnection httpConnection;
            String fileName;
            long contentLength;
            InputStream is;
            try {
                if(proxy == null) {
                    httpConnection = (HttpURLConnection) url.openConnection();
                } else {
                    httpConnection = (HttpURLConnection) url.openConnection(proxy);
                }
                String contentDisposition = httpConnection.getHeaderField("Content-Disposition");
                fileName = parseContentDisposition(contentDisposition);
                contentLength = getContentLength(httpConnection);
                is = httpConnection.getInputStream();
            } catch (IOException e) {
                LOGGER.error("Cannot connect to " + url, e);
                return;
            }

            if(contentLength == -1) {
                contentLength = tryGetFileSize(url);
            }

            if (fileName == null) {
                fileName = parseUrlFileName(url);
            }

            DlId dlId = DlId.create();
            File file = new File(saveToPath, fileName);
            downloadStatus.setName(fileName);
            downloadStatus.setFiles(Collections.singleton(file));
            downloadStatus.setTotalSize(contentLength);
            downloadStatus.setDlId(dlId);
            downloads.put(dlId, downloadStatus);

            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedInputStream in = new BufferedInputStream(is);
                 BufferedOutputStream bout = new BufferedOutputStream(fos, BUFFER)
            ) {
                byte[] data = new byte[BUFFER];
                long downloadedBytes = 0;
                int curDow = 0;
                long startTime = System.nanoTime();
                downloadStatus.setStartTime(startTime);
                long lastTime = startTime;
                while ((curDow = in.read(data, 0, BUFFER)) >= 0) {
                    downloadedBytes += curDow;
                    downloadStatus.setDownloadedSize(downloadedBytes);
                    bout.write(data, 0, curDow);
                    long now = System.nanoTime();
                    long estTime = (long) (((double)contentLength - downloadedBytes)/downloadedBytes * (now - startTime));
                    downloadStatus.setEstTimeNano(estTime);
                    downloadStatus.setSpeedNano(curDow / (double) (now - lastTime));
                    downloadStatus.setProgress(downloadedBytes * 100f / contentLength);
                    lastTime = now;
                }
                downloadStatus.setFinishTime(System.nanoTime());
                downloads.remove(dlId);
                //finishedDownloads.add(downloadedInfo);
            } catch (IOException e) {
                LOGGER.error("Cannot download file " + file + " from " + url);
            }
        });

    }

    @Override
    public BridgeConfig getBridgeConfig() {
        return null;
    }

    private static long tryGetFileSize(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.getInputStream();
            return getContentLength(conn);
        } catch (IOException e) {
            return -1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static long getContentLength(HttpURLConnection httpConnection) {
        String contentLengthField = httpConnection.getHeaderField("Content-Length");
        try {
            return contentLengthField == null ? -1 : Long.parseLong(contentLengthField);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String parseUrlFileName(URL url) {
        int idx = url.getPath().lastIndexOf('/');
        return url.getPath().substring(idx + 1);
    }

    private static String parseContentDisposition(String contentDisposition) {
        if (contentDisposition == null || !contentDisposition.contains("=")) {
            return null;
        }
        return contentDisposition.split("=")[1];
    }

    public StatusObserver getStatusObserver() {
        return statusObserver;
    }

    @Override
    public void addUserActionListener(UserActionListener userActionListener) {
        //noop
    }

    public class RawStatusObserver implements StatusObserver {

        @Override
        public Collection<DownloadStatus> getStatuses() {
            return new ArrayList<>(downloads.values());
        }
    }
}
