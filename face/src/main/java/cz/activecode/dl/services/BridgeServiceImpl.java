package cz.activecode.dl.services;

import cz.activecode.dl.ibridge.IBridge;
import cz.activecode.dl.ibridge.UserActionListener;
import cz.activecode.dl.rawbridge.RawBridge;
import cz.activecode.dl.torrentbridge.TorrentBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

@Service
public class BridgeServiceImpl implements BridgeService, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeServiceImpl.class);

    private ApplicationContext applicationContext;

    private List<IBridge> bridges = new ArrayList<>();

    public void init() {
        Map<String, IBridge> bridgeBeans = applicationContext.getBeansOfType(IBridge.class);
        bridges = new ArrayList<>(bridgeBeans.values());
        bridges.sort((o1, o2) -> o2.getPriority() - o1.getPriority());
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<IBridge> getAllBridges() {
        Map<String, IBridge> bridgeBeans = applicationContext.getBeansOfType(IBridge.class);
        return new ArrayList<>(bridges);
    }

    @Override
    public void addUserActionListener(UserActionListener userActionListener) {
        bridges.stream().forEach(bridge -> bridge.addUserActionListener(userActionListener));
    }
}
