package cz.activecode.dl.frdbridge;

import org.java.plugin.JpfException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

public class FrdLoaderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdLoaderTest.class);

    @Test
    @Ignore
    public void loadFrpFile() throws MalformedURLException, JpfException {
        try {
            FrdPluginManager fpm = new FrdPluginManager();
            fpm.loadPlugins("/home/honza/.FRD/plugins");
            Assert.assertTrue(fpm.isSupported("http://uloz.to/live/xwaS3mQ6/bruce-willis-je-drsnak-mp4"));
            Assert.assertTrue(fpm.isSupported("https://www.youtube.com/watch?v=1SGHrNhzkk4"));
            fpm.activatePlugins();
        } catch (Exception e) {
            LOGGER.error("", e);
            throw e;
        }
    }

}
