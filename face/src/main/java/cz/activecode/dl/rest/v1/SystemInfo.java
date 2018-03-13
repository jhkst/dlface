package cz.activecode.dl.rest.v1;

import cz.activecode.dl.AppInfo;
import cz.activecode.dl.ibridge.GlobalConfig;
import cz.activecode.dl.to.SystemInfoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Properties;

@Path("/v1/systemInfo")
public class SystemInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemInfo.class);



    private GlobalConfig globalConfig;
    private AppInfo appInfo;

    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    @Autowired
    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    @Autowired
    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    /**
     * Returns current system info.
     * @return Current system info.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public SystemInfoData getSystemInfo() {
        SystemInfoData sid = new SystemInfoData();

        sid.setAppVersion(appInfo.getFullVersion());

        File downloadPathFile = globalConfig.getDownloadPath();
        long freeSpace = downloadPathFile.getUsableSpace();
        sid.setFreeSpace(freeSpace);

        sid.setUpTime(appInfo.getUpTime());

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
            if(threadInfo != null) {
                switch (threadInfo.getThreadState()) {
                    case BLOCKED:
                        blocked++;
                        break;
                    case NEW:
                        _new++;
                        break;
                    case RUNNABLE:
                        runnable++;
                        break;
                    case TERMINATED:
                        terminated++;
                        break;
                    case TIMED_WAITING:
                        timedWaiting++;
                        break;
                    case WAITING:
                        waiting++;
                        break;
                }
            }
        }
        return blocked + waiting + timedWaiting;
    }

}
