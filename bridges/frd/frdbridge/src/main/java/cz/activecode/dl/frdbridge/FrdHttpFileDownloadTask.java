package cz.activecode.dl.frdbridge;

import cz.activecode.dl.ibridge.DownloadStatus;
import cz.activecode.dl.ibridge.DownloadStatusUpdateObservable;
import cz.vity.freerapid.core.tasks.CountingOutputStream;
import cz.vity.freerapid.plugins.webclient.DownloadClient;
import cz.vity.freerapid.plugins.webclient.DownloadClientConsts;
import cz.vity.freerapid.plugins.webclient.DownloadState;
import cz.vity.freerapid.plugins.webclient.FileState;
import cz.vity.freerapid.plugins.webclient.interfaces.HttpDownloadClient;
import cz.vity.freerapid.plugins.webclient.interfaces.HttpFile;
import cz.vity.freerapid.plugins.webclient.interfaces.HttpFileDownloadTask;
import cz.vity.freerapid.plugins.webclient.interfaces.ShareDownloadService;
import cz.vity.freerapid.utilities.Utils;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FrdHttpFileDownloadTask implements HttpFileDownloadTask, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdHttpFileDownloadTask.class);

    private static final int OUTPUT_FILE_BUFFER_SIZE = 600000;
    private static final int INPUT_BUFFER_SIZE = 1024;

    private DownloadStatusUpdateObservable observable;
    private ShareDownloadService service;
    private HttpDownloadClient client;
    private FrdDownloadStatus downloadFile;

    public FrdHttpFileDownloadTask(DownloadStatusUpdateObservable observable, ShareDownloadService service, FrdDownloadStatus downloadFile, HttpDownloadClient client) {
        this.observable = observable;
        this.service = service;
        this.downloadFile = downloadFile;
        this.client = client;
    }

    @Override
    public HttpFile getDownloadFile() {
        return downloadFile;
    }

    @Override
    public HttpDownloadClient getClient() {
        return client;
    }

    @Override
    public void saveToFile(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            throw new IllegalArgumentException("cannot save to null inputStream ");
        }

        downloadFile.setFileState(FileState.CHECKED_AND_EXISTING);
        if (downloadFile.getOutputFile().exists()) {
            downloadFile.setFileName(getNewUniqueFileName(downloadFile.getOutputFile()));
        }

        String fileName = downloadFile.getFileName();
        File outputFile = downloadFile.getOutputFile();
        File saveToDirectory = downloadFile.getSaveToDirectory();

        if (!saveToDirectory.exists()) {
            if (!saveToDirectory.mkdirs()) {
                LOGGER.error("Cannot create directory {}", saveToDirectory);
                throw new IOException("Cannot create directory " + saveToDirectory);
            }
        }

        File storeFile = downloadFile.getStoreFile();
        if (storeFile == null || !storeFile.exists()) {
            storeFile = File.createTempFile(fileName + "." + ((fileName.length() < 3) ? "xx" : ""), ".part", saveToDirectory);
            downloadFile.setStoreFile(storeFile);
        }

        long fileSize = downloadFile.getFileSize();
        Long startPositionObject = (Long) downloadFile.getProperties().get(DownloadClient.START_POSITION);
        long startPosition;
        if (startPositionObject == null) {
            startPosition = 0L;
        } else {
            startPosition = startPositionObject;
            downloadFile.getProperties().remove(DownloadClient.START_POSITION);
        }

        try (CountingOutputStream cos = getFileOutputStream(storeFile, fileSize, startPosition);
             OutputStream fileOutputStream = new BufferedOutputStream(cos, OUTPUT_FILE_BUFFER_SIZE)
        ) {

            downloadFile.setState(DownloadState.DOWNLOADING);
            Long suppose = (Long) downloadFile.getProperties().get(DownloadClient.SUPPOSE_TO_DOWNLOAD);
            if (suppose == null) {
                suppose = downloadFile.getTotalSize();
            } else {
                downloadFile.getProperties().remove(DownloadClient.SUPPOSE_TO_DOWNLOAD);
            }

            LOGGER.info("FRD starting download from position " + startPosition);
            downloadFile.setDownloaded(startPosition);
            downloadFile.setRealDownload(downloadFile.getDownloaded());
            byte[] buf = new byte[INPUT_BUFFER_SIZE];

            int len;
            long counter = 0;
            downloadFile.start();
            long timerStart = downloadFile.timerStartNano();
            observable.notifyDownloadStatusUpdaters(downloadFile);
            while ((len = inputStream.read(buf)) != -1 && !isTerminated()) {
                fileOutputStream.write(buf, 0, len);
                counter += len;

                if(suppose == 0) {
                    suppose = downloadFile.getTotalSize();
                }

                downloadFile.setRealDownload(startPosition + counter);
                downloadFile.setEstTimeNano(DownloadStatus.computeEstTime(timerStart, System.nanoTime(), counter, suppose));
                observable.notifyDownloadStatusUpdaters(downloadFile);
            }
            if (client.getHTTPClient().getParams().isParameterTrue(DownloadClientConsts.NO_CONTENT_LENGTH_AVAILABLE)) {
                downloadFile.setFileSize(startPosition + counter);
            } else {
                if (counter != suppose) {
                    LOGGER.info("File size does not match - expected {} but {} was downloaded", suppose, counter);
                    throw new IOException("ErrorDuringDownload");
                }
            }

            if (!wasInterrupted(downloadFile.getStoreFile())) {
                if (!downloadFile.getStoreFile().renameTo(outputFile)) {
                    LOGGER.error("Cannot rename downloaded file {} to {}", downloadFile.getStoreFile(), outputFile);
                }
            }
        } finally {
            LOGGER.debug("FRD finally block started");
            if (!wasInterrupted(downloadFile.getStoreFile())) {
                downloadFile.setDownloaded(downloadFile.getRealDownload());
            } else {
                LOGGER.info("Deleting partial file {}", storeFile);
                if (!storeFile.delete()) {
                    LOGGER.error("Deleting partial file failed ({})", storeFile);
                }
            }
            if (downloadFile.getState() == DownloadState.CANCELLED) {
                downloadFile.setDownloaded(0);
            }
            downloadFile.end();
            LOGGER.debug("FRD finishing status updaters");
        }

    }

    private boolean wasInterrupted(File storeFile) {
        return isTerminated() && storeFile != null && storeFile.exists() && (downloadFile.getState() == DownloadState.CANCELLED || downloadFile.getState() == DownloadState.DELETED);
    }

    private CountingOutputStream getFileOutputStream(File storeFile, long fileSize, long startPosition) throws IOException {
        if (startPosition == 0L) {
            return new CountingOutputStream(new FileOutputStream(storeFile));
        } else {
            RandomAccessFile raf = new RandomAccessFile(storeFile, "rw");
            raf.seek(startPosition);
            return new CountingOutputStream(new FileOutputStream(raf.getFD()));
        }
    }

    @Override
    public void sleep(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000L);
    }

    @Override
    public boolean isTerminated() {
        return Thread.currentThread().isInterrupted();
    }

    @Override
    public void run() {
        initDownloadThread();
        if (downloadFile.getDownloaded() < 0) {
            downloadFile.setDownloaded(0);
        }

        downloadFile.setState(DownloadState.GETTING);
        try {
            service.run(this);
        } catch (Exception e) {
            LOGGER.error("Cannot run frd shared download service");
        }
        service = null;
    }


    private void initDownloadThread() {
        client.getHTTPClient().setHttpConnectionManager(new SimpleHttpConnectionManager());
        client.getHTTPClient().getHttpConnectionManager().closeIdleConnections(0);
    }

    private String getNewUniqueFileName(final File to) {
        final File dir = to.getParentFile();
        final String pureFileName = Utils.getPureFilenameWithDots(to);
        String ext = Utils.getExtension(to);
        ext = (ext != null) ? ("." + ext) : "";
        File newFile;
        int counter = 2;
        while ((newFile = new File(dir, pureFileName + "-" + String.valueOf(counter) + ext)).exists()) {
            ++counter;
        }
        return newFile.getName();
    }
}
