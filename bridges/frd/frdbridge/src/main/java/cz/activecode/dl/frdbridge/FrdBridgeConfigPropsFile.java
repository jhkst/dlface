package cz.activecode.dl.frdbridge;

import cz.activecode.dl.ibridge.GlobalConfig;
import cz.activecode.dl.utils.PropUtil;
import cz.activecode.dl.utils.PropsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class FrdBridgeConfigPropsFile implements FrdBridgeConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdBridgeConfigPropsFile.class);

    private PropsFile propsFile;

    public FrdBridgeConfigPropsFile(GlobalConfig globalConfig) {
        try {
            File configFile = new File(globalConfig.getConfigDir(), "frd.properties");
            propsFile = new PropsFile(configFile);
        } catch (IOException e) {
            LOGGER.error("Cannot get load frd properties file", e);
        }
    }


    public String getPluginsPath() {
        return propsFile.getStringProperty("bridge.frd.pluginsPath", false, System.getProperty("user.home") + "/.FRD/plugins");
    }

    public void setPluginsPath(String pluginsPath) {
        propsFile.setProperty("bridge.frd.pluginsPath", pluginsPath);
    }

    public long getCaptchaWaitingTime() {
        return propsFile.getLongProperty("bridge.frd.captchaWaitingTime", 2 * 60 * 1000L);
    }

    public void setCaptchaWaitingTime(long captchaWaitingTime) {
        propsFile.setProperty("bridge.frd.captchaWaitingTime", Long.toString(captchaWaitingTime));
    }

}
