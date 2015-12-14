package cz.activecode.dl.services;

import cz.activecode.dl.ibridge.DownloadStatus;

import java.util.List;

public interface DownloadsService {

    List<DownloadStatus> getDownloads();

    void addDownloads(List<String> urLs, String path);
}
