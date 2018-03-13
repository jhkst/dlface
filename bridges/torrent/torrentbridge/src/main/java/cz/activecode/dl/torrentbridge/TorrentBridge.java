package cz.activecode.dl.torrentbridge;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import cz.activecode.dl.ibridge.*;
import cz.activecode.dl.ibridge.exceptions.DownloadNotStartedException;
import cz.activecode.dl.utils.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

public class TorrentBridge extends DefaultDownloadStatusUpdateObservable implements IBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(TorrentBridge.class);

    private static final int BUFFER = 4*1024;

    private GlobalConfig globalConfig;
    private TorrentBridgeConfig config;

    public TorrentBridge() {
    }

    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public void setConfig(TorrentBridgeConfig config) {
        this.config = config;
    }

    @Override
    public boolean acceptsUrl(String url) {
        if(url == null) {
            return false;
        }

        if(url.endsWith(".torrent")) {
            return true;
        }
        if(globalConfig.getProxy() != null) {
            LOGGER.warn("Proxy is not supported for torrent");
            return false;
        }

        try {
            URL u = new URL(url);
            return IOUtil.getFileType(u.openStream()).equals(IOUtil.FileType.TORRENT);
        } catch (MalformedURLException e) {
            LOGGER.warn("Cannot check if torrent file " + url, e);
        } catch (IOException e) {
            LOGGER.warn("Cannot open connection for " + url, e);
        }
        return false;
    }

    @Override
    public boolean acceptsFileType(IOUtil.FileType fileType) {
        return fileType.equals(IOUtil.FileType.TORRENT);
    }

    @Override
    public DownloadFuture addDownload(String url, File saveToPath, PostDownloadProcess postProcess) throws DownloadNotStartedException {
        return addDownload(torrentFromURL(url), saveToPath, url, postProcess);
    }

    @Override
    public DownloadFuture addDownload(UploadedFileInfo source, File saveToPath, PostDownloadProcess postProcess) throws DownloadNotStartedException {
        Torrent torrent = torrentFromFile(source.getLocalFile());
        if(! source.getLocalFile().delete()) {
            LOGGER.warn("Cannot remove " + source.getLocalFile());
        }
        return addDownload(torrent, saveToPath, source.getOriginalFilename(), postProcess);
    }

    private DownloadFuture addDownload(Torrent torrent, File saveToPath, String originalUrl, PostDownloadProcess postProcess) throws DownloadNotStartedException {
        if (globalConfig.getProxy() != null) {
            throw new DownloadNotStartedException("Proxy is not supported for torrents");
        }

        SharedTorrent sharedTorrent;
        try {
            sharedTorrent = new SharedTorrent(torrent, saveToPath, false);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new DownloadNotStartedException("Cannot create torrent client", e);
        }

        Client client;
        try {
            client = new Client(InetAddress.getLocalHost(), sharedTorrent);
        } catch (IOException e) {
            throw new DownloadNotStartedException("Cannot create torrent client: " + e.getMessage(), e);
        }

        client.setMaxDownloadRate(config.getMaxDownloadRate());
        client.setMaxUploadRate(config.getMaxUploadRate());
        TorrentDownloadStatus ds = new TorrentDownloadStatus();

        DlId dlId = DlId.create();
        ds.setDlId(dlId);
        ds.setOriginalUrl(originalUrl);
        ds.start();

        client.download();

        TorrentStateObserver observer = new TorrentStateObserver(dlId, ds, client, this, postProcess);

        client.addObserver(observer);

        return observer;
    }

    private Torrent torrentFromURL(String urlStr) throws DownloadNotStartedException {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new DownloadNotStartedException("Cannot recognize url " + urlStr, e);
        }
        HttpURLConnection httpConnection;
        InputStream is;
        try {
            if(globalConfig.getProxy() == null) {
                httpConnection = (HttpURLConnection) url.openConnection();
            } else {
                httpConnection = (HttpURLConnection) url.openConnection(globalConfig.getProxy());
            }
            is = httpConnection.getInputStream();
        } catch (IOException e) {
            throw new DownloadNotStartedException("Cannot download torrent file from " + urlStr, e);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedInputStream in = new BufferedInputStream(is)) {
            byte[] data = new byte[BUFFER];
            int curDow;
            while ((curDow = in.read(data, 0, BUFFER)) >= 0) {
                baos.write(data, 0, curDow);
            }
        } catch (IOException e) {
            LOGGER.error("Cannot read  from " + url, e);
        }

        try {
            return new Torrent(baos.toByteArray(), false);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new DownloadNotStartedException("Cannot read torrent file from " + urlStr, e);
        }
    }

    private Torrent torrentFromFile(File file) throws DownloadNotStartedException {
        try {
            return new Torrent(Files.readAllBytes(file.toPath()), false);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new DownloadNotStartedException("Cannot read torrent file " + file, e);
        }
    }

}
