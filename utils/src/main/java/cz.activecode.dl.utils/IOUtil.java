package cz.activecode.dl.utils;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class IOUtil {

    private static final String TORRENT_HEADER = "d8:announce";

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtil.class);


    public enum FileType {
        TORRENT,
        URL_LIST;
    }
    public static File uniqueFile(File path, String fileName) {
        File probed;
        String currentFilename = fileName;
        String base = FilenameUtils.removeExtension(fileName);
        String ext = FilenameUtils.getExtension(fileName);
        int i = 1;
        while((probed = new File(path, currentFilename)).exists()) {
            currentFilename = base + "-" + (i++) + (ext.isEmpty() ? "" : "." + ext);
        }
        return probed;
    }

    public static FileType getFileType(InputStream is) throws IOException {

        try(BufferedInputStream stream = new BufferedInputStream(is)) {
            byte[] head = IOUtil.readBytes(stream, TORRENT_HEADER.length());
            if(TORRENT_HEADER.equals(new String(head))) {
                return FileType.TORRENT;
            }
        }

        return FileType.URL_LIST; //TODO:
    }

    public static FileType getFileType(File file) throws IOException {
        return getFileType(new FileInputStream(file));
    }

    public static void setFilePermission(File file, Set<PosixFilePermission> savePermissions) {
        try {
            Files.setPosixFilePermissions(file.toPath(), savePermissions);
        } catch (UnsupportedOperationException | IOException | SecurityException e) {
            LOGGER.warn("Cannot change file permissions for " + file , e);
        }
    }

    private static byte[] readBytes(BufferedInputStream stream, int size) throws IOException {
        byte[] buf = new byte[size];
        int readCnt = 0;
        while(readCnt < size) {
            int read = stream.read(buf, readCnt, size - readCnt);
            if(read == -1) {
                byte[] subbuf = new byte[readCnt];
                System.arraycopy(buf, 0, subbuf, 0, readCnt);
                return subbuf;
            }
            readCnt += read;
        }
        return buf;
    }


}
