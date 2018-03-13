package cz.activecode.dl.embedded;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class JettyServerMonitor extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServerMonitor.class);

    private Server[] servers;
    private ServerSocket serverSocket;

    public JettyServerMonitor(int port, Server[] servers) throws IOException {
        if(port <= 0) {
            throw new IllegalStateException("Bad stop PORT");
        }
        this.servers = servers;
        setDaemon(true);
        setName("StopJettyMonitor");
        serverSocket = new ServerSocket(port, 1, InetAddress.getLocalHost());
        serverSocket.setReuseAddress(true);
    }

    public void run() {
        while (serverSocket != null) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                socket.setSoLinger(false, 0);
                LineNumberReader lin = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
                String cmd = lin.readLine();
                if("stop".equals(cmd)) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        LOGGER.warn("Cannot close monitor socket", e);
                    }
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        LOGGER.warn("Cannot close monitor server socket", e);
                    }
                    serverSocket = null;

                    for (int i = 0; servers != null && i < servers.length; i++) {
                        try {
                            LOGGER.info("Stopping server {}", i);
                            servers[i].stop();
                        } catch (Exception e) {
                            LOGGER.error("Cannot stop server " + i, e);
                        }
                    }
                } else {
                    LOGGER.info("Unsupported monitor operation");
                }
            } catch (IOException e) {
                LOGGER.error("Error opening socket", e);
            } finally {
                if(socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        LOGGER.debug("Cannot close monitor socket", e);
                    }
                }
            }
        }
    }
}
