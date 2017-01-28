package cz.activecode.dl.frdbridge;

import cz.activecode.dl.ibridge.IBridgeConfig;

public interface FrdBridgeConfig extends IBridgeConfig {

    String getPluginsPath();

    long getCaptchaWaitingTime();
}
