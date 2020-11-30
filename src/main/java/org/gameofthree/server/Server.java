package org.gameofthree.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.gameofthree.common.event.Event;
import org.gameofthree.common.session.Session;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Server extends WebSocketServer {
    private static final int DEFAULT_PORT = 8887;
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final ServerEventHandler eventHandler = new ServerEventHandler(new Session());

    public Server(int port) {
        super(new InetSocketAddress(port));
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ignored) {
        }

        Server server = new Server(port);
        server.start();
        logger.info("Game org.gameofthree.server started on port: {}", server.getPort());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("New connection: {} {}",
                handshake.getResourceDescriptor(),
                conn.getRemoteSocketAddress().getAddress().getHostAddress()
        );
        eventHandler.onOpen(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("{} has disconnected!", conn);
        eventHandler.onClose(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("{}: {}", conn, message);
        try {
            eventHandler.processMessage(conn, Event.fromString(message));
        } catch (JsonProcessingException e) {
            logger.error("Error processing message", e);
        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("Exception", ex);
    }

    @Override
    public void onStart() {
        logger.info("Server started!");
        setConnectionLostTimeout(100);
    }
}
