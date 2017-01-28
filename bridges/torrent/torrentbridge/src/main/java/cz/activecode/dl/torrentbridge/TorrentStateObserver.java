package cz.activecode.dl.torrentbridge;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import cz.activecode.dl.ibridge.DlId;
import cz.activecode.dl.ibridge.DownloadFuture;
import cz.activecode.dl.ibridge.DownloadStatus;
import cz.activecode.dl.ibridge.DownloadStatusUpdateObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;

public class TorrentStateObserver implements Observer, DownloadFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStateObserver.class);

    private DlId dlId;
    private volatile boolean canceled;
    private TorrentDownloadStatus torrentDownloadStatus;
    private long lastDownloaded;
    private long startTime;
    private long lastTime;
    private Client client;
    private DownloadStatusUpdateObservable statusUpdater;

    public TorrentStateObserver(DlId dlId, TorrentDownloadStatus torrentDownloadStatus, Client client, DownloadStatusUpdateObservable statusUpdater) {
        this.dlId = dlId;
        this.torrentDownloadStatus = torrentDownloadStatus;
        this.client = client;
        this.statusUpdater = statusUpdater;
        this.startTime = System.nanoTime();
        this.lastTime = System.nanoTime();
        this.lastDownloaded = 0L;
        this.canceled = false;
    }

    @Override
    public void update(Observable observable, Object data) {
        Client client = (Client) observable;
        Client.ClientState state = (Client.ClientState) data;
        SharedTorrent st = client.getTorrent();

        long now = System.nanoTime();
        long downloaded = st.getDownloaded();

        torrentDownloadStatus.setEstTimeNano(DownloadStatus.computeEstTime(startTime, now, downloaded, st.getLeft() + st.getDownloaded()));
        torrentDownloadStatus.setSpeedNano((downloaded - lastDownloaded) / (double) (now - lastTime));
        torrentDownloadStatus.updateValues(client);

        if(st.isFinished() || canceled || state.equals(Client.ClientState.ERROR) || state.equals(Client.ClientState.DONE)) {
            finishDwonload();
            return;
        }

        statusUpdater.notifyDownloadStatusUpdaters(torrentDownloadStatus);

        lastDownloaded = downloaded;
        lastTime = now;

    }

    private void finishDwonload() {
        canceled = true;
        torrentDownloadStatus.end();
        client.stop(false);
        client.getTorrent().stop();
        statusUpdater.finishDownloadStatusUpdaters(torrentDownloadStatus);
    }

    @Override
    public void cancel() {
        LOGGER.debug("Canceling TORRENT download");
        finishDwonload();
    }

    @Override
    public DlId getDlId() {
        return dlId;
    }
}
