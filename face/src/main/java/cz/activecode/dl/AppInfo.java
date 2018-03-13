package cz.activecode.dl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppInfo.class);

    private final String appName = "dlFace";
    private String appVersion;
    private String scmVersion;
    private String buildTimestamp;
    private long startTime;

    public void init() {
        startTime = System.currentTimeMillis();

        try(InputStream prop = this.getClass().getResourceAsStream("/version.properties")) {
            load(prop);
        } catch (IOException e) {
            LOGGER.warn("Cannot load application information");
        }
        LOGGER.info("===> STARTING {} {} build: {} timestamp: {}", appName, appVersion, scmVersion, buildTimestamp);
    }

    private void load(InputStream is) {
        if(is == null) {
            LOGGER.warn("Cannot get application information. InputStream is null");
        }

        Properties p = new Properties();
        try {
            p.load(is);
            appVersion = p.getProperty("appVersion");
            scmVersion = p.getProperty("scmVersion");
            buildTimestamp = p.getProperty("buildTimestamp");
        } catch (IOException e) {
            LOGGER.error("Cannot load appVersion.properties", e);
        }
    }

    public String getFullVersion() {
        if(scmVersion.startsWith("${")) {
            return appVersion + "-?";
        }
        return appVersion + "-" + scmVersion.substring(0, 5);
    }

    public long getUpTime() {
        return System.currentTimeMillis() - startTime;
    }
}
