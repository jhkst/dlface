package cz.activecode.dl.torrentbridge;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import cz.activecode.dl.ibridge.DownloadStatus;
import cz.activecode.dl.utils.Util;

import java.util.List;
import java.util.stream.Collectors;

public class TorrentDownloadStatus extends DownloadStatus {

    private String comment;
    private int pieceCount;
    private float completion;
    private long uploaded;
    private double maxDownloadRate;
    private double maxUploadRate;
    private int peersCount;
    private List<PeerDataTO> peers;
    private int trackerCount;
    private String infoHash;
    private List<String> filenames;
    private String completedPieces;
    private String availablePieces;
    private String requestedPieces;

    public void updateValues(Client client) {
        SharedTorrent sharedTorrent = client.getTorrent();
        this.setName(sharedTorrent.getName() + " (" + sharedTorrent.getFilenames().size() + " file(s))");
        this.setTotalSize(sharedTorrent.getSize());
        this.setDownloadedSize(sharedTorrent.getDownloaded());
        this.setProgress(sharedTorrent.getCompletion());
        this.setState(String.valueOf(client.getState()));
        this.setComment(sharedTorrent.getComment());
        this.setPieceCount(sharedTorrent.getPieceCount());
        this.setCompletion(sharedTorrent.getCompletion());
        this.setUploaded(sharedTorrent.getUploaded());
        this.setMaxDownloadRate(sharedTorrent.getMaxDownloadRate());
        this.setMaxUploadRate(sharedTorrent.getMaxUploadRate());
        List<PeerDataTO> peers = client.getPeers().stream().map(PeerDataTO::new).collect(Collectors.toList());
        this.setPeers(peers);
        this.setPeersCount(client.getPeers().size());
        this.setTrackerCount(sharedTorrent.getTrackerCount());
        this.setInfoHash(sharedTorrent.getHexInfoHash());
        this.setFilenames(sharedTorrent.getFilenames());
        if (sharedTorrent.isInitialized()) {
            this.setCompletedPieces(Util.compressToUri(sharedTorrent.getCompletedPieces()));
            this.setAvailablePieces(Util.compressToUri(sharedTorrent.getAvailablePieces()));
            this.setRequestedPieces(Util.compressToUri(sharedTorrent.getRequestedPieces()));
        }
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setPieceCount(int pieceCount) {
        this.pieceCount = pieceCount;
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public void setCompletion(float completion) {
        this.completion = completion;
    }

    public float getCompletion() {
        return completion;
    }

    public void setUploaded(long uploaded) {
        this.uploaded = uploaded;
    }

    public long getUploaded() {
        return uploaded;
    }

    public void setMaxDownloadRate(double maxDownloadRate) {
        this.maxDownloadRate = maxDownloadRate;
    }

    public double getMaxDownloadRate() {
        return maxDownloadRate;
    }

    public void setMaxUploadRate(double maxUploadRate) {
        this.maxUploadRate = maxUploadRate;
    }

    public double getMaxUploadRate() {
        return maxUploadRate;
    }

    public void setPeersCount(int peersCount) {
        this.peersCount = peersCount;
    }

    public int getPeersCount() {
        return peersCount;
    }

    public void setPeers(List<PeerDataTO> peers) {
        this.peers = peers;
    }

    public List<PeerDataTO> getPeers() {
        return peers;
    }

    private String state;

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setTrackerCount(int trackerCount) {
        this.trackerCount = trackerCount;
    }

    public int getTrackerCount() {
        return trackerCount;
    }

    public void setInfoHash(String infoHash) {
        this.infoHash = infoHash;
    }

    public String getInfoHash() {
        return infoHash;
    }

    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }

    public List<String> getFilenames() {
        return filenames;
    }

    public void setCompletedPieces(String completedPieces) {
        this.completedPieces = completedPieces;
    }

    public String getCompletedPieces() {
        return completedPieces;
    }

    public void setAvailablePieces(String availablePieces) {
        this.availablePieces = availablePieces;
    }

    public String getAvailablePieces() {
        return availablePieces;
    }

    public void setRequestedPieces(String requestedPieces) {
        this.requestedPieces = requestedPieces;
    }

    public String getRequestedPieces() {
        return requestedPieces;
    }
}
