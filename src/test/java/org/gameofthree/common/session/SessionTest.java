package org.gameofthree.common.session;

import org.java_websocket.WebSocket;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SessionTest {
    private WebSocket player, opponent;

    @Before
    public void beforeEach() {
        player = mock(WebSocket.class);
        opponent = mock(WebSocket.class);
    }

    @Test
    public void shouldReturnOpponent() {
        Session session = new Session();

        session.addPlayer(player);
        assertThat(session.getOpponent(player)).isNull();
        assertThat(session.getOpponent(opponent)).isEqualTo(player);

        session.addPlayer(opponent);
        assertThat(session.getOpponent(player)).isEqualTo(opponent);
    }

    @Test
    public void shouldUpdateStatusWhenAllPlayersConnected() {
        Session session = new Session();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.WAITING_FOR_OPPONENTS);

        session.addPlayer(player);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.WAITING_FOR_OPPONENTS);

        session.addPlayer(opponent);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
    }

    @Test(expected = SessionInProgressException.class)
    public void shouldNotAddPlayerWhenGameIsInProgress() {
        Session session = new Session();
        session.addPlayer(player);
        session.addPlayer(opponent);
        WebSocket client = mock(WebSocket.class);
        session.addPlayer(client);
    }

    @Test
    public void shouldUpdateStatusWhenAllPlayersDisconnected() {
        Session session = new Session();
        session.addPlayer(player);
        session.addPlayer(opponent);
        session.removePlayer(player);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);

        session.removePlayer(opponent);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.WAITING_FOR_OPPONENTS);
    }

    @Test
    public void shouldReturnPlayingClient() {
        Session session = new Session();
        session.addPlayer(player);

        assertThat(session.isPlayer(player)).isTrue();
        assertThat(session.isPlayer(opponent)).isFalse();
    }
}
