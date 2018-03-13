package cz.activecode.dl.frdbridge;

import cz.vity.freerapid.plugins.webclient.interfaces.ConfigurationStorageSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;

public class FrdStorageSupportImpl implements ConfigurationStorageSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdStorageSupportImpl.class);

    @Override
    public <E> E loadConfigFromFile(String fileName, Class<E> type) throws Exception {
        try(XMLDecoder xmlDecoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(getFileForFilename(fileName))), null, null, type.getClassLoader())) {
            Object theObject = xmlDecoder.readObject();
            if(type.isInstance(theObject)) {
                return type.cast(theObject);
            } else {
                LOGGER.warn("Unknown file format {}. Not {}.", fileName, type.getName());
                throw new Exception("Unknown file format");
            }
        } catch (RuntimeException e) {
            throw new Exception(e);
        }
    }

    @Override
    public void storeConfigToFile(Object object, String fileName) throws Exception {
        ClassLoader threadCL = null;
        try {
            threadCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(object.getClass().getClassLoader());
            try(XMLEncoder xmlEncoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(getFileForFilename(fileName))))) {
                xmlEncoder.writeObject(object);
            }
        } finally {
            if (threadCL != null)
                Thread.currentThread().setContextClassLoader(threadCL);
        }
    }

    @Override
    public boolean configFileExists(String fileName) {
        final File file = getFileForFilename(fileName);
        return file.isFile() && file.exists();
    }

    @Override
    public File getConfigDirectory() {
        LOGGER.debug("getConfigDirectory not implemented");
        return null;
    }

    private File getFileForFilename(String filename) {
        return new File(filename);
    }
}
