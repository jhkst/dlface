package cz.activecode.dl.rest.v1;

import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import cz.activecode.dl.ibridge.DlId;
import cz.activecode.dl.ibridge.DownloadStatus;
import cz.activecode.dl.ibridge.GlobalConfig;
import cz.activecode.dl.ibridge.IBridge;
import cz.activecode.dl.services.DownloadsService;
import cz.activecode.dl.to.AddDownloadData;
import cz.activecode.dl.utils.Util;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * REST entry-point for all downloads related operations.
 */
@Path("/v1/downloads")
@Component
public class Downloads {

    private static final Logger LOGGER = LoggerFactory.getLogger(Downloads.class);

    private DownloadsService downloadsService;

    private GlobalConfig globalConfig;

    @Autowired
    public void setDownloadsService(DownloadsService downloadsService) {
        this.downloadsService = downloadsService;
    }

    @Autowired
    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    /**
     * Returns all currently processed downloads.
     * @return all currently processed downloads
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<DownloadStatus> getDownloads() {
        return downloadsService.getDownloads();
    }

    /**
     * Adds new downloads.
     * @param addDownloadData data for new download
     */
    @POST @Path("add")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void addDownloads(AddDownloadData addDownloadData) {
        List<String> downloadListURLs = Util.splitURLS(addDownloadData.getDownloadList());
        LOGGER.info("Adding urls: {}", downloadListURLs);

        downloadsService.addDownloads(downloadListURLs, globalConfig.getDownloadPath());
    }

    @POST @Path("add/files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void addFiles(FormDataMultiPart body) {
        List<IBridge.UploadedFileInfo> files = new LinkedList<>();
        for(BodyPart part : body.getBodyParts()) {
            InputStream is = part.getEntityAs(InputStream.class);
            ContentDisposition meta = part.getContentDisposition();

            try {
                File tmpFile = File.createTempFile("dlup", ".dat");
                tmpFile.deleteOnExit();
                FileUtils.copyInputStreamToFile(is, tmpFile);
                LOGGER.info("File {} uploaded to {}", meta.getFileName(), tmpFile);
                files.add(new IBridge.UploadedFileInfo(tmpFile, meta.getFileName()));
            } catch (IOException e) {
                LOGGER.error("Cannot create temporary file for upload", e);
            }
        }

        downloadsService.addFiles(files, globalConfig.getDownloadPath());
    }

    /**
     * Cancels download.
     * @param dlId id of download
     */
    @POST @Path("cancel/{dlId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void cancelDownload(@PathParam("dlId") String dlId) {
        downloadsService.cancelDownload(DlId.fromString(dlId));
    }

    /**
     * Returns all finished downloads.
     * @return all finished downloads
     */
    @GET @Path("finished")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<DownloadStatus> getFinishedDownloads() {
        return downloadsService.getFinishedDownloads();
    }

}
