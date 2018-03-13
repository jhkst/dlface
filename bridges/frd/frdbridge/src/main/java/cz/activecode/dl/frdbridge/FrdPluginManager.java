package cz.activecode.dl.frdbridge;

import cz.activecode.dl.ibridge.GlobalConfig;
import cz.vity.freerapid.plugimpl.StandardPluginContextImpl;
import cz.vity.freerapid.plugins.webclient.interfaces.PluginContext;
import cz.vity.freerapid.plugins.webclient.interfaces.ShareDownloadService;
import org.java.plugin.JpfException;
import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Ref: http://www.programcreek.com/java-api-examples/index.php?api=org.java.plugin.PluginManager.PluginLocation
 */
public class FrdPluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdPluginManager.class);
    private static final String MANIFEST_PATH = "plugin.xml";

    private final PluginManager pluginManager = ObjectFactory.newInstance().createManager();
    private ExecutorService newPluginWatcherExecutor;
    private Future<?> watcherFuture;

    private final Map<String, ShareDownloadService> shareServiceMap = new HashMap<>();
    private final Map<File, String> fileToPluginId = Collections.synchronizedMap(new HashMap<>());

    private final Lock initLock = new ReentrantLock();
    private final Condition initCondition = initLock.newCondition();
    private boolean initialised = false;

    private FrdBridgeConfig config;
    private FrdMaintainQueueSupportImpl queueSupport;
    private GlobalConfig globalConfig;
    private FrdDialogSupportImpl frdDialogSupport;

    public void init() {
        loadAndActivatePluginsInBackground();
        queueSupport = new FrdMaintainQueueSupportImpl(); //todo: spring
        newPluginWatcherExecutor = Executors.newFixedThreadPool(1, r -> new Thread(r, "frd-new-plugin-watcher"));
        watcherFuture = newPluginWatcherExecutor.submit(new FrdNewPluginWatcher(new FrdNewPluginWatcher.PluginChangeProcessor() {
            @Override
            public void create(Collection<Path> path) {
                try {
                    reloadPlugins(path.stream().map(Path::toFile).collect(Collectors.toList()));
                } catch (JpfException e) {
                    LOGGER.warn("Cannot load frd plugin", e);
                }
            }

            @Override
            public void delete(Collection<Path> path) {
                deactivatePlugins(path.stream().map(Path::toFile).collect(Collectors.toList()));
            }

            @Override
            public void modify(Collection<Path> path) {
                try {
                    reloadPlugins(path.stream().map(Path::toFile).collect(Collectors.toList()));
                } catch (JpfException e) {
                    LOGGER.warn("Cannot reload frd plugin", e);
                }
            }
        }, new File(config.getPluginsPath()).toPath()));
    }

    public void destroy() {
        if (watcherFuture != null) {
            watcherFuture.cancel(true);
        }

        if (newPluginWatcherExecutor != null) {
            newPluginWatcherExecutor.shutdownNow();
        }
    }

    private void loadAndActivatePluginsInBackground() {
        new Thread(() -> {
            try {
                activatePlugins(loadPlugins(config.getPluginsPath()));
            } catch (JpfException e) {
                LOGGER.error("Cannot activate plugins", e);
            } finally {
                markInitDone();
            }
        }, "frd-plugins-init").start();
    }

    private void markInitDone() {
        initLock.lock();
        try {
            initialised = true;
            initCondition.signalAll();
        } finally {
            initLock.unlock();
        }
    }

    private void waitForInit() {
        initLock.lock();
        try {
            while (!initialised) {
                initCondition.await();
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Waiting for initialization interrupted", e);
        } finally {
            initLock.unlock();
        }
    }

    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public void setConfig(FrdBridgeConfig config) {
        this.config = config;
    }

    public void setFrdDialogSupport(FrdDialogSupportImpl frdDialogSupport) {
        this.frdDialogSupport = frdDialogSupport;
    }

    public Map<String, Identity> loadPlugins(String folder) throws JpfException {
        File pluginsDir = new File(folder);
        File[] plugins = pluginsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".frp"));
        if (plugins == null || plugins.length == 0) {
            LOGGER.warn("No plugins found in {}", pluginsDir);
            return null;
        }
        return loadPlugins(Arrays.asList(plugins));
    }

    public Map<String, Identity> loadPlugins(List<File> files) throws JpfException {
        PluginManager.PluginLocation[] locations = files.stream().map(file -> {
            try {
                StandardPluginLocation loc = new StandardPluginLocation(file, MANIFEST_PATH);
                try {
                    String pluginId = pluginIdFromLocation(loc);
                    fileToPluginId.put(file, pluginId);
                } catch (SAXException | ParserConfigurationException | IOException e) {
                    LOGGER.warn("Cannot get pluginId from " + file, e);
                }
                return loc;
            } catch (MalformedURLException e) {
                LOGGER.warn("Wrong file specification {}", e);
                return null;
            }
        }).filter(Objects::nonNull).toArray(PluginManager.PluginLocation[]::new);

        return pluginManager.publishPlugins(locations);

    }

    private String pluginIdFromLocation(PluginManager.PluginLocation loc) throws IOException, ParserConfigurationException, SAXException {
        try(InputStream is = loc.getManifestLocation().openStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            return doc.getDocumentElement().getAttribute("id");
        }
    }

    public void reloadPlugins(List<File> files) throws JpfException{
        waitForInit();

        deactivatePlugins(files);

        activatePlugins(loadPlugins(files));

    }

    private void activatePlugins(Map<String, Identity> pluginPub) {
        List<PluginDescriptor> pds = pluginPub.values().stream().map(i -> pluginManager.getRegistry().getPluginDescriptor(i.getId())).collect(Collectors.toList());
        activatePlugins(pds);
    }

    private void deactivatePlugins(List<File> files) {
        for(File file : files) {
            String pluginId = fileToPluginId.get(file);
            if (pluginId != null) {
                pluginManager.deactivatePlugin(pluginId);
            } else {
                LOGGER.warn("Cannot get pluginId for file {}", file);
            }
        }
    }

    public void activatePlugins(Collection<PluginDescriptor> pluginDescriptors) {
        List<String> failedPlugins = new LinkedList<>();

        for(PluginDescriptor pd : pluginDescriptors) {
            try {
                activatePlugin(pd);
            } catch (PluginLifecycleException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOGGER.error("Cannot activate plugin " + pd.getId(), e);
                failedPlugins.add(pd.getId());
            }
        }

        LOGGER.info("FRD plugin activation failed {} of {}: {}", failedPlugins.size(), pluginDescriptors.size(), failedPlugins);
    }

    private void activatePlugin(PluginDescriptor pluginDescriptor) throws PluginLifecycleException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        LOGGER.info("Activating {} : {}", pluginDescriptor.getId(), pluginDescriptor.getPluginClassName());
        pluginManager.activatePlugin(pluginDescriptor.getId());
        ClassLoader classLoader = pluginManager.getPluginClassLoader(pluginDescriptor);
        Class<?> pluginCls = classLoader.loadClass(pluginDescriptor.getPluginClassName());
        Object pluginClsInstance = pluginCls.newInstance();
        if (pluginClsInstance instanceof ShareDownloadService) {
            ShareDownloadService downloadService = (ShareDownloadService) pluginClsInstance;
            if (downloadService.getPluginContext() == null) {
                downloadService.setPluginContext(createPluginContext());
            }

            shareServiceMap.put(pluginDescriptor.getId(), downloadService);
            downloadService.pluginInit(); //TODO: somewhere call pluginStop()
        }
    }

    private PluginContext createPluginContext() {
        return StandardPluginContextImpl.create(frdDialogSupport, new FrdStorageSupportImpl(), queueSupport);
    }

    public boolean isSupported(String url) {
        waitForInit();
        try {
            getPluginFor(url);
            return true;
        } catch (UnsupportedURLException e) {
            return false;
        }
    }

    public PluginDescriptor getPluginFor(String url) throws UnsupportedURLException {
        waitForInit();
        for (PluginDescriptor pluginDescriptor : pluginManager.getRegistry().getPluginDescriptors()) {
            PluginAttribute urlRegex = pluginDescriptor.getAttribute("urlRegex");
            if (urlRegex != null && urlRegex.getValue() != null) {
                if (url.matches(urlRegex.getValue()) && !pluginDescriptor.getId().endsWith("_premium")) {
                    return pluginDescriptor;
                }
            }
        }
        throw new UnsupportedURLException(url);
    }

    public ShareDownloadService getShareDownloadService(String pluginId) {
        waitForInit();
        return shareServiceMap.get(pluginId);
    }

    public Collection<PluginDescriptor> getPlugins() {
        waitForInit();
        return pluginManager.getRegistry().getPluginDescriptors();
    }

}
