package cz.activecode.dl.frdbridge;

import org.apache.commons.io.FileUtils;
import org.java.plugin.JpfException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FrdLoaderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdLoaderTest.class);

    @Test
    @Ignore
    public void loadFrpFile() throws IOException, JpfException {
        File frdPlugins = new File("/tmp/FRDplugins");

        Assert.assertTrue((frdPlugins.exists() && frdPlugins.isDirectory()) || (frdPlugins.mkdirs()));
        try {
            FrdPluginManager fpm = new FrdPluginManager();
            fpm.loadPlugins(frdPlugins.getAbsolutePath());
            Assert.assertTrue(fpm.isSupported("http://uloz.to/live/xwaS3mQ6/bruce-willis-je-drsnak-mp4"));
            Assert.assertTrue(fpm.isSupported("https://www.youtube.com/watch?v=1SGHrNhzkk4"));
//            fpm.activatePlugins();
        } finally {
            FileUtils.deleteDirectory(frdPlugins);
        }
    }

}
