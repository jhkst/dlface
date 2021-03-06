package cz.activecode.dl.frdbridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;

public class FrdNewPluginWatcher implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdNewPluginWatcher.class);
    public static final String FRP_SUFFIX = ".frp";
    private final PluginChangeProcessor pluginChangeProcessor;

    private List<Path> watchPaths;

    public interface PluginChangeProcessor {
        void create(Collection<Path> path);
        void delete(Collection<Path> path);
        void modify(Collection<Path> path);
    }

    public FrdNewPluginWatcher(PluginChangeProcessor pluginChangeProcessor, Path... watchPaths) {
        this.pluginChangeProcessor = pluginChangeProcessor;
        this.watchPaths = Arrays.asList(watchPaths);
    }

    @Override
    public void run() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            for(Path path : watchPaths) {
                if(Files.notExists(path)) {
                    Files.createDirectories(path);
                }
                path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                LOGGER.info("Frd plugin watcher for {} registered", path);
            }

            while(!Thread.currentThread().isInterrupted()) {
                WatchKey key = watcher.take();
                List<WatchEvent<?>> watchEvents = key.pollEvents();
                LOGGER.info("frd new plugin watcher event: {}", watchEvents.size());

                Set<Path> create = new LinkedHashSet<>();
                Set<Path> delete = new LinkedHashSet<>();
                Set<Path> modify = new LinkedHashSet<>();

                for(WatchEvent<?> event : watchEvents) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path parent = (Path) key.watchable();
                    if(kind == OVERFLOW) {
                        LOGGER.debug("Overflow event on frd plugin watch");

                        //reload all plugins
                        File[] files = parent.toFile().listFiles((dir, name) -> name.endsWith(FRP_SUFFIX));
                        if(files != null) {
                            modify.addAll(Arrays.stream(files).map(File::toPath).collect(Collectors.toList()));
                        }
                        continue;
                    }
                    Path context = (Path)event.context();
                    String filename = context.toString();
                    if(!filename.startsWith(".") && filename.endsWith(FRP_SUFFIX)) {
                        Path path = parent.resolve(context);

                        if(kind == ENTRY_CREATE) {
                            create.add(path);
                        } else if(kind == ENTRY_DELETE) {
                            delete.add(path);
                        } else if(kind == ENTRY_MODIFY) {
                            modify.add(path);
                        }

                    }

                    if(!key.reset()) { //important as per documentation
                        LOGGER.warn("Frd new plugin watch folder is no more accessible");
                        break;
                    }
                }

                if(pluginChangeProcessor != null) {
                    if (!create.isEmpty()) {
                        pluginChangeProcessor.create(create);
                    }
                    if (!delete.isEmpty()) {
                        pluginChangeProcessor.delete(delete);
                    }
                    if (!modify.isEmpty()) {
                        pluginChangeProcessor.modify(modify);
                    }
                }

            }
        } catch (InterruptedException e) {
            LOGGER.info("Frd new plugin watcher interrupted", e);
        } catch (IOException e) {
            LOGGER.warn("Cannot create filesystem watcher", e);
        }
        LOGGER.info("Frd new plugin watcher exited");
    }
}
