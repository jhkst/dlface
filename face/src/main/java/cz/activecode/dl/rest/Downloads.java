package cz.activecode.dl.rest;

import cz.activecode.dl.Util;
import cz.activecode.dl.ibridge.DownloadStatus;
import cz.activecode.dl.services.DownloadsService;
import cz.activecode.dl.to.AddDownloadData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/downloads")
@Component
public class Downloads {

    private static final Logger LOGGER = LoggerFactory.getLogger(Downloads.class);

    @Autowired
    private DownloadsService downloadsService;

    public void setDownloadsService(DownloadsService downloadsService) {
        this.downloadsService = downloadsService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<DownloadStatus> getDownloads() {
        return downloadsService.getDownloads();
    }

    @POST @Path("add")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public boolean addDownloads(AddDownloadData addDownloadData) {
        List<String> downloadListURLs = Util.splitURLS(addDownloadData.getDownloadList());
        LOGGER.info("Adding urls: {}", downloadListURLs);
        downloadsService.addDownloads(downloadListURLs, "/home/honza/Downloads");
        return true;
    }

}
