package cz.activecode.dl.rawbridge;

import cz.activecode.dl.ibridge.*;
import cz.activecode.dl.ibridge.exceptions.DownloadNotStartedException;
import cz.activecode.dl.utils.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class RawBridge extends DefaultDownloadStatusUpdateObservable implements IBridge{

    private static final Logger LOGGER = LoggerFactory.getLogger(RawBridge.class);

    private static final int BUFFER = 16*1024;

    private static final AtomicInteger THREAD_CNTR = new AtomicInteger();
    private ExecutorService executorService = Executors.newCachedThreadPool(r -> new Thread(r, "download-thread-" + THREAD_CNTR.getAndIncrement())); //todo: dispose
    private GlobalConfig globalConfig;

    public RawBridge() {
    }

    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    @Override
    public boolean acceptsUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            LOGGER.warn("Demanded url is not URL " + url);
            return false;
        }
    }

    @Override
    public boolean acceptsFileType(IOUtil.FileType fileType) {
        return false;
    }

    @Override
    public DownloadFuture addDownload(String urlStr, File saveToPath) throws DownloadNotStartedException {
        URL url;
        RawDownloadStatus downloadStatus = new RawDownloadStatus();
        downloadStatus.setOriginalUrl(urlStr);
        DlId dlId = DlId.create();

        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new DownloadNotStartedException(e);
        }

        Future future = executorService.submit(() -> {
            LOGGER.info("Download initialization");
            HttpURLConnection httpConnection;
            String fileName;
            long contentLength;
            InputStream is;
            try {
                if (globalConfig.getProxy() == null) {
                    httpConnection = (HttpURLConnection) url.openConnection();
                } else {
                    httpConnection = (HttpURLConnection) url.openConnection(globalConfig.getProxy());
                }
                String contentDisposition = httpConnection.getHeaderField("Content-Disposition");
                fileName = parseContentDisposition(contentDisposition);
                contentLength = getContentLength(httpConnection);
                is = httpConnection.getInputStream();
            } catch (IOException e) {
                LOGGER.error("Cannot connect to " + url, e);
                return;
            }

            if (contentLength == -1) {
                contentLength = tryGetFileSize(url);
            }

            if (fileName == null) {
                fileName = parseUrlFileName(url);
            }

            File file = IOUtil.uniqueFile(saveToPath, fileName);
            downloadStatus.setName(fileName);
            downloadStatus.setFiles(Collections.singleton(file));
            downloadStatus.setTotalSize(contentLength);
            downloadStatus.setDlId(dlId);

            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedInputStream in = new BufferedInputStream(is);
                 BufferedOutputStream bout = new BufferedOutputStream(fos, BUFFER)
            ) {
                byte[] data = new byte[BUFFER];
                long downloadedBytes = 0;
                int curDow = 0;
                downloadStatus.start();
                long timerStart = downloadStatus.timerStartNano();
                long lastTime = timerStart;
                RawBridge.this.notifyDownloadStatusUpdaters(downloadStatus);
                while ((curDow = in.read(data, 0, BUFFER)) >= 0 && !Thread.currentThread().isInterrupted()) {
                    downloadedBytes += curDow;
                    downloadStatus.setDownloadedSize(downloadedBytes);
                    bout.write(data, 0, curDow);
                    long now = System.nanoTime();
                    downloadStatus.setEstTimeNano(DownloadStatus.computeEstTime(timerStart, now, downloadedBytes, contentLength));
                    downloadStatus.setSpeedNano(curDow / (double) (now - lastTime));
                    downloadStatus.setProgress(downloadedBytes * 100f / contentLength);
                    RawBridge.this.notifyDownloadStatusUpdaters(downloadStatus);
                    lastTime = now;
                }
                downloadStatus.end();
            } catch (IOException e) {
                LOGGER.error("Cannot download file " + file + " from " + url);
            } finally {
                RawBridge.this.finishDownloadStatusUpdaters(downloadStatus);
            }
        });

        return new DownloadFuture() {
            @Override
            public void cancel() {
                LOGGER.debug("Canceling RAW download");
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
        throw new DownloadNotStartedException(new UnsupportedOperationException("RAW does not support uploaded files"));
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
}
