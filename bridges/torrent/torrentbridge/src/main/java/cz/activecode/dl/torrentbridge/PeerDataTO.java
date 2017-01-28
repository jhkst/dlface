package cz.activecode.dl.torrentbridge;

import com.turn.ttorrent.client.peer.SharingPeer;

public class PeerDataTO {
    private final String ip;
    private final int port;
    private final String hexPeerId;
    private final boolean choked;
    private final boolean interested;
    private final boolean choking;
    private final boolean interesting;
    private final int availablePiecesCount;
    private final float downloadRate;
    private final float uploadRate;
    private final boolean downloading;

    public PeerDataTO(SharingPeer sharingPeer) {
        this.ip = sharingPeer.getIp();
        this.port = sharingPeer.getPort();
        this.hexPeerId = sharingPeer.getHexPeerId();
        this.choked = sharingPeer.isChoked();
        this.interested = sharingPeer.isInterested();
        this.choking = sharingPeer.isChoking();
        this.interesting = sharingPeer.isInteresting();
        this.availablePiecesCount = sharingPeer.getAvailablePieces() != null ? sharingPeer.getAvailablePieces().cardinality() : 0;
        this.downloadRate = sharingPeer.getDLRate() != null ? sharingPeer.getDLRate().get() : 0.0f;
        this.uploadRate = sharingPeer.getULRate() != null ? sharingPeer.getULRate().get() : 0.0f;
        this.downloading = sharingPeer.isDownloading();
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getHexPeerId() {
        return hexPeerId;
    }

    public boolean isChoked() {
        return choked;
    }

    public boolean isInterested() {
        return interested;
    }

    public boolean isChoking() {
        return choking;
    }

    public boolean isInteresting() {
        return interesting;
    }

    public int getAvailablePiecesCount() {
        return availablePiecesCount;
    }

    public float getDownloadRate() {
        return downloadRate;
    }

    public float getUploadRate() {
        return uploadRate;
    }

    public boolean isDownloading() {
        return downloading;
    }
}
