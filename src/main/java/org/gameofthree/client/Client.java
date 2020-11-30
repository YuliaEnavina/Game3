package org.gameofthree.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.gameofthree.common.event.Event;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

public class Client extends WebSocketClient {
    private static final String DEFAULT_SERVER_URI = "ws://localhost:8887";
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final Random random = new Random();
    private static final ClientEventHandler eventHandler = new ClientEventHandler(random);

    public Client(URI serverURI) {
        super(serverURI);
    }

    public static void main(String[] args) throws URISyntaxException {
        String uri = args.length > 0
                ? args[0]
                : DEFAULT_SERVER_URI;

        Client client = new Client(new URI(uri));
        client.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        logger.info("Connected to org.gameofthree.server");
    }

    @Override
    public synchronized void onMessage(String message) {
        try {
            eventHandler.handle(this, Event.fromString(message));
        } catch (JsonProcessingException ex) {
            logger.error("Exception while parsing json", ex);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info(
                "Connection closed by {}. Code: {}. Reason: {}",
                remote ? "remote peer" : "us",
                code,
                reason
        );
    }

    @Override
    public void onError(Exception ex) {
        logger.error("Exception", ex);
    }

}
