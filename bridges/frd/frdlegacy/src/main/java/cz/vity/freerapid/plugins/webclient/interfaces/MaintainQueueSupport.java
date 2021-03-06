package cz.vity.freerapid.plugins.webclient.interfaces;

import cz.vity.freerapid.plugins.container.FileInfo;

import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * @author Vity
 * @author ntoskrnl
 */
public interface MaintainQueueSupport {

    /**
     * Adds links to the queue.
     *
     * @param parentFile parent file where description is copied from
     * @param uriList    list of links which should be added to the queue
     * @return true on success, false otherwise
     */
    public boolean addLinksToQueue(HttpFile parentFile, List<URI> uriList);

    /**
     * Parses a String for supported links and adds them to the queue.
     *
     * @param parentFile parent file where description is copied from
     * @param data       data to parse for links to be added to the queue
     * @return true on success, false otherwise
     * @since 0.85
     */
    public boolean addLinksToQueue(HttpFile parentFile, String data);

    /**
     * Adds links to the queue.
     *
     * @param parentFile parent file where description is copied from
     * @param infoList   list of links which should be added to the queue
     * @return true on success, false otherwise
     * @since 0.85
     */
    public boolean addLinksToQueueFromContainer(HttpFile parentFile, List<FileInfo> infoList);

    /**
     * Adds links to the queue.
     *
     * @param parentFile parent file where description and saveToDirectory is copied from
     * @param childDir   children directory, so the actual target dir would be "parentFile.saveToDirectory/childDir".
     *                   Method caller is responsible for childDir sanity check, implementation is not reponsible
     *                   for the sanity check. This way enables multi level directory creation.
     * @param infoList   list of links which should be added to the queue
     * @return true on success, false otherwise
     */
    public boolean addLinksToQueueFromContainer(HttpFile parentFile, String childDir, List<FileInfo> infoList);

    /**
     * Adds one of the links to the queue (depending on user settings of plugin priorities).
     *
     * @param parentFile parent file where description is copied from
     * @param urlList    list of links of which one is chosen to be added to the queue
     * @return true on success, false otherwise
     * @throws Exception if something goes wrong
     * @since 0.85
     */
    public boolean addLinkToQueueUsingPriority(HttpFile parentFile, List<URL> urlList) throws Exception;

    /**
     * Parses a String for supported links and adds one of the links to the queue
     * (depending on user settings of plugin priorities).
     *
     * @param parentFile parent file where description is copied from
     * @param data       data to parse for links of which one is chosen to be added to the queue
     * @return true on success, false otherwise
     * @throws Exception if something goes wrong
     * @since 0.85
     */
    public boolean addLinkToQueueUsingPriority(HttpFile parentFile, String data) throws Exception;

    /**
     * Adds one of the links to the queue (depending on user settings of plugin priorities).
     *
     * @param parentFile parent file where description is copied from
     * @param infoList   list of links of which one is chosen to be added to the queue
     * @return true on success, false otherwise
     * @throws Exception if something goes wrong
     * @since 0.85
     */
    public boolean addLinkToQueueFromContainerUsingPriority(HttpFile parentFile, List<FileInfo> infoList) throws Exception;

    /**
     * Add links to the queue next to parentFile
     *
     * @param parentFile parent file where description is copied from, also as reference for 'NextTo'
     * @param uriList    list of links which should be added to the queue
     * @param autoStart  true on auto start, false on paused
     * @return true on success, false otherwise
     */
    public boolean addLinksToQueueNextTo(HttpFile parentFile, List<URI> uriList, boolean autoStart);

    /**
     * Add links to the queue next to parentFile,
     * whether the links are auto started or paused, depends on user preference (AUTO_START_DOWNLOADS_FROM_DECRYPTER)
     *
     * @param parentFile parent file where description is copied from, also as reference for 'NextTo'
     * @param uriList    list of links which should be added to the queue
     * @return true on success, false otherwise
     */
    public boolean addLinksToQueueNextTo(HttpFile parentFile, List<URI> uriList);

}
