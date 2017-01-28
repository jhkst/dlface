package cz.activecode.dl.services;

import cz.activecode.dl.ibridge.GlobalConfig;
import cz.activecode.dl.ibridge.IBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class BridgeServiceImpl implements BridgeService, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeServiceImpl.class);

    private ApplicationContext applicationContext;

    private GlobalConfig globalConfig;

    private List<IBridge> bridges = new ArrayList<>();

    @Autowired
    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    /**
     * Bean initialization. See spring context.
     */
    @PostConstruct
    public void init() {
        String[] bridgesOrder = globalConfig.getBridgesOrder();
        Map<String, IBridge> bridgeBeans = applicationContext.getBeansOfType(IBridge.class);

        bridges = new ArrayList<>(bridgeBeans.size());
        for (String bridgeName : bridgesOrder) {
            if(bridgeName.trim().isEmpty()) {
                continue;
            }

            IBridge bridge = bridgeBeans.get(bridgeName);
            if(bridge == null) {
                LOGGER.warn("Cannot find bridge [{}] defined in configuration file", bridgeName);
            } else {
                bridges.add(bridge);
                bridgeBeans.remove(bridgeName);
            }
        }

        //add the rest
        bridges.addAll(bridgeBeans.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IBridge> getAllBridges() {
        return new ArrayList<>(bridges);
    }

}
