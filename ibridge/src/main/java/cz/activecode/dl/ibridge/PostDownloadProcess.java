package cz.activecode.dl.ibridge;

import java.io.File;
import java.util.Collection;

public interface PostDownloadProcess {

    void postProcess(Collection<File> downloadedFiles);

}
