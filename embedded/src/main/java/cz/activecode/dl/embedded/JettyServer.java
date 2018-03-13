package cz.activecode.dl.embedded;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.*;

// https://github.com/arey/embedded-jetty-webapp/blob/master/src/main/java/com/javaetmoi/jetty/JettyServer.java

/**
 * TODO: add switches:
 * --port
 * --log-config
 * --dl-config
 */
public class JettyServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    private static final int DEFAULT_PORT_START = 8080;
    private static final int DEFAULT_PORT_STOP = 8090;
    private static final String WEBAPP_RESOURCES_LOCATION = "webapp";
    private static final String WEBJARS_RESOURCES_LOCATION = "META-INF/resources";

    private final int startPort;
    private final int stopPort;

    public JettyServer() {
        this(DEFAULT_PORT_START, DEFAULT_PORT_STOP);
    }

    public JettyServer(int startPort, int stopPort) {
        this.startPort = startPort;
        this.stopPort = stopPort;
    }

    public static void stop() {
        stop(DEFAULT_PORT_STOP);
    }

    public static void stop(int stopPort) {
        try {
            Socket s = new Socket(InetAddress.getLocalHost(), stopPort);
            LOGGER.info("Jetty stopping ...");
            s.setSoLinger(false, 0);
            OutputStream out = s.getOutputStream();
            out.write("stop\r\n".getBytes());
            out.flush();
            s.close();
        } catch (ConnectException e) {
            LOGGER.info("Jetty not running!");
        } catch (Exception e) {
            LOGGER.error("Cannot stop Jetty", e);
        }
    }

    public static void main(String[] args) throws Exception {
        JettyServer jettyServer;
        if(args.length == 2) {
            jettyServer = new JettyServer(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
        } else {
            jettyServer = new JettyServer();
        }
        jettyServer.start();
    }

    public void start() throws Exception {
        Server server = new Server(startPort);
        WebAppContext root = new WebAppContext();
        root.setContextPath("/");
        root.setDescriptor(WEBAPP_RESOURCES_LOCATION + "/WEB-INF/web.xml");

        URL webAppDir = Thread.currentThread().getContextClassLoader().getResource(WEBAPP_RESOURCES_LOCATION);
        if(webAppDir == null) {
            throw new IllegalStateException(String.format("No %s directory was found into the JAR file", WEBAPP_RESOURCES_LOCATION));
        }

        URL webJarsDir = Thread.currentThread().getContextClassLoader().getResource(WEBJARS_RESOURCES_LOCATION);
        if(webJarsDir == null) {
            throw new IllegalStateException(String.format("No %s directory was found into the JAR file", WEBAPP_RESOURCES_LOCATION));
        }

        ResourceCollection resources = new ResourceCollection(new String[]{
                webAppDir.toURI().toString(),
                webJarsDir.toURI().toString()
        });

        root.setBaseResource(resources);
        root.setParentLoaderPriority(true);

        server.setHandler(root);
        try {
            server.start();
        } catch (BindException e) {
            LOGGER.error("Cannot start server on port {}", startPort, e);
            System.exit(1);
        }

        LOGGER.info("Jetty server started");
        LOGGER.info("Jetty web server port: {}", startPort);
        LOGGER.info("Port to stop Jetty with the 'stop' operation: {}", stopPort);

        JettyServerMonitor monitor = new JettyServerMonitor(stopPort, new Server[]{server});
        monitor.start();

        server.join();

        LOGGER.info("Jetty server exited");
    }
}
