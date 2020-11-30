package org.gameofthree.common.session;

import lombok.NoArgsConstructor;
import org.java_websocket.WebSocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
public class Session {
    private static final Integer REQUIRED_PLAYERS_NUMBER = 2;
    private final Set<WebSocket> players = ConcurrentHashMap.newKeySet();
    private SessionStatus status = SessionStatus.WAITING_FOR_OPPONENTS;

    public WebSocket getOpponent(WebSocket conn) {
        return players.stream()
                .filter(storedConn -> !storedConn.equals(conn))
                .findFirst()
                .orElse(null);
    }

    public void addPlayer(WebSocket conn) {
        if (status == SessionStatus.IN_PROGRESS) {
            throw new SessionInProgressException("Another game session is in-progress");
        }

        players.add(conn);

        updateStatusIfNeeded();
    }

    private void updateStatusIfNeeded() {
        if (players.size() < REQUIRED_PLAYERS_NUMBER) {
            setStatus(SessionStatus.WAITING_FOR_OPPONENTS);
        } else if (players.size() == REQUIRED_PLAYERS_NUMBER) {
            setStatus(SessionStatus.IN_PROGRESS);
        }
    }

    public void removePlayer(WebSocket conn) {
        players.remove(conn);
        if (players.size() == 0) {
            setStatus(SessionStatus.WAITING_FOR_OPPONENTS);
        }
    }

    public boolean isReady() {
        return status == SessionStatus.IN_PROGRESS;
    }

    public SessionStatus getStatus() {
        return status;
    }

    private void setStatus(SessionStatus status) {
        this.status = status;
    }

    public boolean isPlayer(WebSocket conn) {
        return players.contains(conn);
    }


}
