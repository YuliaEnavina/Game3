package org.gameofthree.client;

import org.gameofthree.common.event.Event;
import org.gameofthree.common.event.EventFactory;
import org.gameofthree.common.game.GameResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class ClientEventHandler
{
    private static final Logger logger = LoggerFactory.getLogger(ClientEventHandler.class);
    private final Random random;

    public ClientEventHandler(Random random) {
        this.random = random;
    }

    public synchronized void handle(Client client, Event event) {
        switch (event.getEventType()) {
            case GAME_STARTED:
                handleGameStartedEvent(client);
                break;

            case MOVE:
                handleMoveEvent(client, event);
                break;

            case GAME_ENDED:
                handleGameEndedEvent(client, event);
                break;

            case INFO:
                logger.info(event.getPayload().toString());
                break;

            default:
                logger.error("Client received unknown event: {}", event);

        }
    }

    private void handleGameStartedEvent(Client client) {
        int initialNumber = getInitialNumber();
        logger.info("Game started, sending the initial number {}", initialNumber);
        client.send(EventFactory.move(initialNumber).toStringValue());
    }

    private int getInitialNumber() {
        return random.nextInt(Integer.MAX_VALUE - 1) + 1;
    }

    private void handleMoveEvent(Client client, Event event) {
        int currentNumber = Integer.parseInt(event.getPayload().toString());
        logger.info("Received new number from opponent: {}", currentNumber);

        int nextNumber = getNextNumber(currentNumber);
        logger.info("Sending nextNumber {}", nextNumber);
        client.send(EventFactory.move(nextNumber).toStringValue());
    }

    private int getNextNumber(int currentNumber) {
        int nextNumber = currentNumber;
        if ((currentNumber - 1) % 3 == 0) {
            nextNumber = currentNumber - 1;
        } else if ((currentNumber + 1) % 3 == 0) {
            nextNumber = currentNumber + 1;
        }
        return nextNumber / 3;
    }

    private void handleGameEndedEvent(Client client, Event event) {
        GameResultStatus gameResultStatus = GameResultStatus.valueOf(event.getPayload().toString());
        logger.info("Game ended, my status: {}, disconnecting.", gameResultStatus);
        client.close();
    }
}
