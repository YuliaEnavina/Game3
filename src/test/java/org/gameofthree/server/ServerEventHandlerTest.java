package org.gameofthree.server;

import org.gameofthree.common.event.Event;
import org.gameofthree.common.event.EventFactory;
import org.gameofthree.common.event.EventType;
import org.gameofthree.common.game.GameResultStatus;
import org.gameofthree.common.session.Session;
import org.gameofthree.common.session.SessionInProgressException;
import org.gameofthree.common.session.SessionStatus;
import lombok.SneakyThrows;
import org.java_websocket.WebSocket;
import org.java_websocket.enums.ReadyState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ServerEventHandlerTest
{
    private ArgumentCaptor<String> eventStringCaptor;
    private WebSocket player;
    private ServerEventHandler serverEventHandler;
    private Session session;

    @Before
    public void beforeEach() {
        eventStringCaptor = ArgumentCaptor.forClass(String.class);
        session = mock(Session.class);
        serverEventHandler = new ServerEventHandler(session);
        player = mock(WebSocket.class);
    }

    @Test
    public void shouldDisconnectOtherClientsJoiningInTheMiddleOfTheGame() {
        doThrow(new SessionInProgressException("")).when(session).addPlayer(eq(player));
        serverEventHandler.onOpen(player);
        verify(player, times(1)).close();
        verify(player, never()).send(anyString());
    }

    @Test
    @SneakyThrows
    public void shouldSendInfoEventWhenWaitingForOpponent() {
        when(session.isReady()).thenReturn(false);
        serverEventHandler.onOpen(player);

        verify(player, times(1)).send(eventStringCaptor.capture());
        assertThat(Event.fromString(eventStringCaptor.getValue()).getEventType()).isEqualTo(EventType.INFO);
    }

    @Test
    @SneakyThrows
    public void shouldSendStartGameEventWhenAllPlayersConnected() {
        WebSocket opponent = mock(WebSocket.class);
        when(session.isReady()).thenReturn(true);
        when(session.getOpponent(player)).thenReturn(opponent);
        serverEventHandler.onOpen(player);

        verify(player, times(1)).send(EventFactory.gameStarted().toStringValue());
        verify(opponent, times(1)).send(eventStringCaptor.capture());
        assertThat(Event.fromString(eventStringCaptor.getValue()).getEventType()).isEqualTo(EventType.INFO);
    }

    @Test
    @SneakyThrows
    public void shouldNotRemoveNotPlayingClientFromSessionWhenClientDisconnects() {
        when(session.isPlayer(eq(player))).thenReturn(false);
        serverEventHandler.onClose(player);
        verify(session, never()).removePlayer(player);
    }

    @Test
    @SneakyThrows
    public void shouldRemovePlayerFromSessionWhenPlayerDisconnects() {
        when(session.isPlayer(eq(player))).thenReturn(true);
        when(session.getOpponent(eq(player))).thenReturn(null);

        serverEventHandler.onClose(player);
        verify(session, times(1)).removePlayer(player);
    }

    @Test
    @SneakyThrows
    public void shouldSendGameEndedToOpponentWhenPlayerDisconnects() {
        WebSocket opponent = mock(WebSocket.class);
        when(opponent.getReadyState()).thenReturn(ReadyState.OPEN);
        when(session.isPlayer(eq(player))).thenReturn(true);
        when(session.getOpponent(eq(player))).thenReturn(opponent);

        serverEventHandler.onClose(player);
        verify(opponent, times(1)).send(eventStringCaptor.capture());

        Event event = Event.fromString(eventStringCaptor.getValue());
        assertThat(event.getEventType()).isEqualTo(EventType.GAME_ENDED);

        GameResultStatus gameResultStatus = GameResultStatus.valueOf(event.getPayload().toString());
        assertThat(gameResultStatus).isEqualTo(GameResultStatus.OPPONENT_DISCONNECTED);
    }

    @Test
    @SneakyThrows
    public void shouldNotProcessMessageWhenGameIsNotPlayed() {
        when(session.getStatus()).thenReturn(SessionStatus.WAITING_FOR_OPPONENTS);
        serverEventHandler.processMessage(player, EventFactory.move(222));
        verify(player, never()).send(anyString());
    }

    @Test
    @SneakyThrows
    public void shouldNotProcessMessageWhenOpponentIsNotConnected() {
        when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
        when(session.getOpponent(eq(player))).thenReturn(null);
        serverEventHandler.processMessage(player, EventFactory.move(222));
        verify(player, never()).send(anyString());
    }

    @Test
    @SneakyThrows
    public void shouldSendMoveEventToOpponent() {
        WebSocket opponent = mock(WebSocket.class);
        when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
        when(session.getOpponent(eq(player))).thenReturn(opponent);

        Event expectedEvent = EventFactory.move(222);
        serverEventHandler.processMessage(player, expectedEvent);
        verify(opponent, times(1)).send(expectedEvent.toStringValue());
    }

    @Test
    @SneakyThrows
    public void shouldHandleWinningNumber() {
        WebSocket opponent = mock(WebSocket.class);
        when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
        when(session.getOpponent(eq(player))).thenReturn(opponent);

        serverEventHandler.processMessage(player, EventFactory.move(1));
        verify(opponent, times(1)).send(EventFactory.gameEnded(GameResultStatus.LOSE).toStringValue());
        verify(player, times(1)).send(EventFactory.gameEnded(GameResultStatus.WIN).toStringValue());
    }

}
