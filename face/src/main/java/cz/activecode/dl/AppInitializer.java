package cz.activecode.dl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Application initialization.
 */
public class AppInitializer implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        redirectJULtoSLF4J();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    /**
     * Redirect JUL from third-party libraries to SLF4J
     */
    private void redirectJULtoSLF4J() {
        LOGGER.info("Redirecting JUL to SLF4J");
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        java.util.logging.Logger.getGlobal().setLevel(Level.FINEST);
    }
}
