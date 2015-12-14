package cz.activecode.dl.torrentbridge;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import cz.activecode.dl.ibridge.*;
import cz.activecode.dl.ibridge.exceptions.DownloadNotStartedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TorrentBridge implements IBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(TorrentBridge.class);

    private static final int BUFFER = 4*1024;

    private Proxy proxy;
    private TorrentStatusObserver statusObserver = new TorrentStatusObserver();
    private Map<DlId, TorrentInfo> torrents = new ConcurrentHashMap<>();

    public TorrentBridge() {
    }

    public TorrentBridge(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public int getPriority() {
        return -50;
    }

    @Override
    public boolean isSupported(String url) {
        return url != null && url.endsWith(".torrent");
    }

    @Override
    public void addDownload(String url, String saveToPath) throws DownloadNotStartedException {

        Torrent torrent = downloadTorrent(proxy, url);

        SharedTorrent sharedTorrent;
        try {
            sharedTorrent = new SharedTorrent(
                    torrent, new File(saveToPath), false
            );
        } catch (IOException e) {
            throw new DownloadNotStartedException("Cannot create torrent client", e);
        }

        Client client;
        try {
            client = new Client(
                    InetAddress.getLocalHost(),
                    sharedTorrent
            );
        } catch (IOException e) {
            throw new DownloadNotStartedException("Cannot create torrent client", e);
        }

        client.setMaxDownloadRate(50.0);
        client.setMaxUploadRate(50.0);
        client.download();
        DlId dlId = DlId.create();

        torrents.put(dlId, new TorrentInfo(url, sharedTorrent));
        //todo: announce finish
    }

    private Torrent downloadTorrent(Proxy proxy, String urlStr) throws DownloadNotStartedException {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new DownloadNotStartedException("Cannot recognize url " + urlStr, e);
        }
        HttpURLConnection httpConnection;
        InputStream is;
        try {
            if(proxy == null) {
                httpConnection = (HttpURLConnection) url.openConnection();
            } else {
                httpConnection = (HttpURLConnection) url.openConnection(proxy);
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
        } catch (IOException e) {
            throw new DownloadNotStartedException("Cannot read torrent file", e);
        }
    }

    @Override
    public StatusObserver getStatusObserver() {
        return statusObserver;
    }

    @Override
    public void addUserActionListener(UserActionListener userActionListener) {

    }

    private class TorrentStatusObserver implements StatusObserver  {

        @Override
        public Collection<DownloadStatus> getStatuses() {
            List<DownloadStatus> dss = new LinkedList<>();

            for (Map.Entry<DlId, TorrentInfo> entry : torrents.entrySet()) {
                DlId dlId = entry.getKey();
                SharedTorrent torrent = entry.getValue().getTorrent();
                String url = entry.getValue().getUrl();
                TorrentDownloadStatus ds = new TorrentDownloadStatus();
                ds.setDlId(dlId);
                ds.setName(torrent.getName() + " (" + torrent.getFilenames().size() + " file(s))");
                ds.setTotalSize(torrent.getSize());
                ds.setOriginalUrl(url);
                ds.setDownloadedSize(torrent.getDownloaded());
                ds.setSpeed(-1);
                ds.setEstTime(-1);
                ds.setProgress(torrent.getCompletion());
                dss.add(ds);
            }

            return dss;
        }

    }

    @Override
    public BridgeConfig getBridgeConfig() {
        return null;
    }

    private static class TorrentInfo {
        private String url;
        private SharedTorrent torrent;

        public TorrentInfo(String url, SharedTorrent torrent) {
            this.url = url;
            this.torrent = torrent;
        }

        public String getUrl() {
            return url;
        }

        public SharedTorrent getTorrent() {
            return torrent;
        }
    }

}
