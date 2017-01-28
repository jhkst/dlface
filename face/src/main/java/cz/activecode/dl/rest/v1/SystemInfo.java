package cz.activecode.dl.rest.v1;

import cz.activecode.dl.ibridge.GlobalConfig;
import cz.activecode.dl.to.SystemInfoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.*;
import java.util.Properties;

@Path("/v1/systemInfo")
public class SystemInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemInfo.class);

    private String appVersion;
    private long startTime;

    private GlobalConfig globalConfig;

    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    @Autowired
    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    @PostConstruct
    public void init() {
        startTime = System.currentTimeMillis();

        InputStream versionStream = this.getClass().getResourceAsStream("/version.properties");
        if(versionStream != null) {
            Properties p = new Properties();
            try {
                p.load(versionStream);
                String appVer = p.getProperty("appVersion");
                String scmVer = p.getProperty("scmVersion");
                appVersion = appVer + "-" + scmVer.substring(0, 5);
            } catch (IOException e) {
                LOGGER.error("Cannot load appVersion.properties", e);
            }
        }
    }

    /**
     * Returns current system info.
     * @return Current system info.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public SystemInfoData getSystemInfo() {
        SystemInfoData sid = new SystemInfoData();

        sid.setAppVersion(appVersion);

        File downloadPathFile = globalConfig.getDownloadPath();
        long freeSpace = downloadPathFile.getFreeSpace();
        sid.setFreeSpace(freeSpace);

        sid.setUpTime(System.currentTimeMillis() - startTime);

        sid.setLowSpace(freeSpace < globalConfig.getFreeSpaceThreshold());

        sid.setFreeMem(Runtime.getRuntime().freeMemory());
        sid.setMaxMem(Runtime.getRuntime().maxMemory());
        sid.setTotalMem(Runtime.getRuntime().totalMemory());

        sid.setActiveThreadCount(threadMXBean.getThreadCount());
        sid.setLockedThreadCount(lockedThreads());

        sid.setSystemLoad(operatingSystemMXBean.getSystemLoadAverage());

        return sid;
    }

    private int lockedThreads() {
        int blocked = 0;
        int _new = 0;
        int runnable = 0;
        int terminated = 0;
        int timedWaiting = 0;
        int waiting = 0;
        for(long tid : threadMXBean.getAllThreadIds()) {
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(tid);
            switch(threadInfo.getThreadState()) {
                case BLOCKED: blocked++; break;
                case NEW: _new++; break;
                case RUNNABLE: runnable++; break;
                case TERMINATED: terminated++; break;
                case TIMED_WAITING: timedWaiting++; break;
                case WAITING: waiting++; break;
            }
        }
        return blocked + waiting + timedWaiting;
    }

}
