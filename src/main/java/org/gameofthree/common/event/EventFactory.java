package org.gameofthree.common.event;

import org.gameofthree.common.game.GameResultStatus;

public class EventFactory {
    public static Event gameStarted() {
        return new Event(EventType.GAME_STARTED, null);
    }

    public static Event move(Integer number) {
        return new Event(EventType.MOVE, number);
    }

    public static Event gameEnded(GameResultStatus status) {
        return new Event(EventType.GAME_ENDED, status);
    }

    public static Event info(String message) {
        return new Event(EventType.INFO, message);
    }
}
