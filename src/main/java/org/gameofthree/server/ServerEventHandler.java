package org.gameofthree.server;

import org.gameofthree.common.event.Event;
import org.gameofthree.common.event.EventFactory;
import org.gameofthree.common.game.GameResultStatus;
import org.gameofthree.common.game.GameSettings;
import org.gameofthree.common.session.Session;
import org.gameofthree.common.session.SessionInProgressException;
import org.gameofthree.common.session.SessionStatus;
import org.java_websocket.WebSocket;
import org.java_websocket.enums.ReadyState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerEventHandler
{
    private static final String WAITING_FOR_THE_OPPONENT = "Waiting for the opponent";
    private static final String OPPONENT_CONNECTED_AND_STARTS_THE_GAME = "Opponent connected and starts the game";
    private static final Logger logger = LoggerFactory.getLogger(ServerEventHandler.class);
    private final Session session;

    public ServerEventHandler(Session session) {
        this.session = session;
    }

    public synchronized void onOpen(WebSocket conn) {
        try {
            session.addPlayer(conn);
        } catch (SessionInProgressException ex) {
            logger.info("Session already in-progress, disconnecting client");
            conn.close();
            return;
        }

        logger.info("Session status {}", session.getStatus());
        if (session.isReady()) {
            session.getOpponent(conn).send(
                EventFactory.info(OPPONENT_CONNECTED_AND_STARTS_THE_GAME).toStringValue()
            );
            // the second player connected sends the first number
            conn.send(EventFactory.gameStarted().toStringValue());
        } else {
            conn.send(EventFactory.info(WAITING_FOR_THE_OPPONENT).toStringValue());
        }

    }

    public synchronized void onClose(WebSocket conn) {
        if (session.isPlayer(conn)) {
            WebSocket opponent = session.getOpponent(conn);
            if (opponent != null && opponent.getReadyState() == ReadyState.OPEN) {
                opponent.send(EventFactory.gameEnded(
                    GameResultStatus.OPPONENT_DISCONNECTED).toStringValue());
            }
            session.removePlayer(conn);
        }
        logger.info("Session status {}", session.getStatus());
    }

    public synchronized void processMessage(WebSocket conn, Event event) {
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            logger.error("Client {} sent message {}, but game status is {}", conn.getRemoteSocketAddress(), event, session.getStatus());
            return;
        }

        WebSocket opponent = session.getOpponent(conn);
        if (opponent == null) {
            logger.error("Client {} sent message {}, but opponent is not connected", conn.getRemoteSocketAddress(), event);
            return;
        }

        switch (event.getEventType()) {
            case MOVE:
                handleMoveEvent(conn, event, opponent);
                break;
            default:
                logger.error("Server received unknown event: {}", event);
        }
    }

    private void handleMoveEvent(WebSocket conn, Event event, WebSocket opponent) {
        int number = Integer.parseInt(event.getPayload().toString());
        if (number == GameSettings.WINNING_NUMBER) {
            conn.send(EventFactory.gameEnded(GameResultStatus.WIN).toStringValue());
            opponent.send(EventFactory.gameEnded(GameResultStatus.LOSE).toStringValue());
        } else { // transmit to the opponent
            opponent.send(event.toStringValue());
        }
    }
}
