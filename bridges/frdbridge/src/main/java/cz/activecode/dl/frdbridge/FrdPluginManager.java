package cz.activecode.dl.frdbridge;

import cz.activecode.dl.ibridge.exceptions.InitializationException;
import cz.vity.freerapid.plugins.webclient.interfaces.ShareDownloadService;
import org.java.plugin.JpfException;
import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Ref: http://www.programcreek.com/java-api-examples/index.php?api=org.java.plugin.PluginManager.PluginLocation
 */
public class FrdPluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdPluginManager.class);

    private PluginManager pluginManager = ObjectFactory.newInstance().createManager();

    private Map<String, ShareDownloadService> shareServiceMap = new HashMap<>();

    public void init() throws InitializationException {
        String path = "/home/honza/.FRD/plugins";
        try {
            loadPlugins(path);
            activatePlugins();
        } catch (MalformedURLException | JpfException e) {
            throw new InitializationException(e);
        }
    }

    public void loadPlugins(String folder) throws MalformedURLException, JpfException {

        File pluginsDir = new File(folder);
        File[] plugins = pluginsDir.listFiles((dir, name) -> {
            return name.toLowerCase().endsWith(".frp");
        });
        if(plugins == null || plugins.length == 0) {
            LOGGER.warn("No plugins found in {}", pluginsDir);
            return;
        }
        LOGGER.info(Arrays.toString(plugins));
        PluginManager.PluginLocation[] locations = new PluginManager.PluginLocation[plugins.length];
        for(int i = 0; i < plugins.length; i++) {
            locations[i] = new StandardPluginLocation(plugins[i], "plugin.xml");
        }

        LOGGER.info(Arrays.toString(locations));
        pluginManager.publishPlugins(locations);
    }

    public void activatePlugins() {
        for(PluginDescriptor pluginDescriptor : pluginManager.getRegistry().getPluginDescriptors()) {
            try {
                LOGGER.info("Activating {} : {}", pluginDescriptor.getId(), pluginDescriptor.getPluginClassName());
                pluginManager.activatePlugin(pluginDescriptor.getId());
                ClassLoader classLoader = pluginManager.getPluginClassLoader(pluginDescriptor);
                Class<?> pluginCls = classLoader.loadClass(pluginDescriptor.getPluginClassName());
                Object pluginClsInstance = pluginCls.newInstance();
                if(pluginClsInstance instanceof ShareDownloadService) {
                    ShareDownloadService downloadService = (ShareDownloadService) pluginClsInstance;
                    shareServiceMap.put(pluginDescriptor.getId(), downloadService);
                    downloadService.pluginInit(); //TODO: somewhere call pluginStop()
                }
            } catch (PluginLifecycleException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOGGER.error("Cannot activate plugin " + pluginDescriptor.getId(), e);
            }
        }

    }

    public boolean isSupported(String url) {
        try {
            getPluginFor(url);
            return true;
        } catch (UnsupportedURLException e) {
            return false;
        }
    }

    public PluginDescriptor getPluginFor(String url) throws UnsupportedURLException {
        for(PluginDescriptor pluginDescriptor : pluginManager.getRegistry().getPluginDescriptors()) {
            PluginAttribute urlRegex = pluginDescriptor.getAttribute("urlRegex");
            if(urlRegex != null && urlRegex.getValue() != null) {
                if(url.matches(urlRegex.getValue())) {
                    LOGGER.info("URL [{}] supported by `{}`", url, pluginDescriptor.getId());
                    return pluginDescriptor;
                }
            }
        }
        throw new UnsupportedURLException(url);
    }

    public ShareDownloadService getShareDownloadService(String pluginId) {
        return shareServiceMap.get(pluginId);
    }

}
