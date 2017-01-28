package cz.activecode.dl.frdbridge;

import cz.vity.freerapid.plugins.container.FileInfo;
import cz.vity.freerapid.plugins.webclient.interfaces.HttpFile;
import cz.vity.freerapid.plugins.webclient.interfaces.MaintainQueueSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.util.List;

public class FrdMaintainQueueSupportImpl implements MaintainQueueSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdMaintainQueueSupportImpl.class);

    @Override
    public boolean addLinksToQueue(HttpFile parentFile, List<URI> uriList) {
        LOGGER.info("addLinksToQueue({}, {})", parentFile, uriList);
        return false;
    }

    @Override
    public boolean addLinksToQueue(HttpFile parentFile, String data) {
        LOGGER.info("addLinksToQueue({}, {})", parentFile, data);
        return false;
    }

    @Override
    public boolean addLinksToQueueFromContainer(HttpFile parentFile, List<FileInfo> infoList) {
        LOGGER.info("addLinksToQueueFromContainer({}, {})", parentFile, infoList);
        return false;
    }

    @Override
    public boolean addLinkToQueueUsingPriority(HttpFile parentFile, List<URL> urlList) throws Exception {
        LOGGER.info("addLinkToQueueUsingPriority{{}, {})", parentFile, urlList);
        return false;
    }

    @Override
    public boolean addLinkToQueueUsingPriority(HttpFile parentFile, String data) throws Exception {
        LOGGER.info("addLinkToQueueUsingPriority({}, {})", parentFile, data);
        return false;
    }

    @Override
    public boolean addLinkToQueueFromContainerUsingPriority(HttpFile parentFile, List<FileInfo> infoList) throws Exception {
        LOGGER.info("addLinkToQueueFromContainerUsingPriority({}, {})", parentFile, infoList);
        return false;
    }
}
