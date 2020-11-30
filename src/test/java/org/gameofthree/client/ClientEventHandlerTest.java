package org.gameofthree.client;

import org.gameofthree.common.event.EventFactory;
import org.gameofthree.common.game.GameResultStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class ClientEventHandlerTest
{
    private ClientEventHandler clientEventHandler;
    private Client client;
    private Random random;
    private ArgumentCaptor<String> eventStringCaptor;

    @Before
    public void beforeEach() {
        client = mock(Client.class);
        random = mock(Random.class);
        eventStringCaptor = ArgumentCaptor.forClass(String.class);
        clientEventHandler = new ClientEventHandler(random);
    }

    @Test
    public void shouldHandleGameStartedEvent() {
        int initialNumber = 100;
        String expectedEvent = EventFactory.move(initialNumber).toStringValue();
        when(random.nextInt(anyInt())).thenReturn(initialNumber - 1);

        clientEventHandler.handle(client, EventFactory.gameStarted());

        verify(client, times(1)).send(eventStringCaptor.capture());
        assertThat(eventStringCaptor.getValue()).isEqualTo(expectedEvent);
    }

    @Test
    public void shouldHandleMoveEventCurrentNumberDivisibleByThree() {
        int currentNumber = 6;
        int expectedNumber = 2;
        String expectedEvent = EventFactory.move(expectedNumber).toStringValue();

        clientEventHandler.handle(client, EventFactory.move(currentNumber));

        verify(client, times(1)).send(eventStringCaptor.capture());
        assertThat(eventStringCaptor.getValue()).isEqualTo(expectedEvent);
    }

    @Test
    public void shouldHandleMoveEventCurrentNumberPlusOneDivisibleByThree() {
        int currentNumber = 5;
        int expectedNumber = 2;
        String expectedEvent = EventFactory.move(expectedNumber).toStringValue();

        clientEventHandler.handle(client, EventFactory.move(currentNumber));

        verify(client, times(1)).send(eventStringCaptor.capture());
        assertThat(eventStringCaptor.getValue()).isEqualTo(expectedEvent);
    }

    @Test
    public void shouldHandleMoveEventCurrentNumberMinusOneDivisibleByThree() {
        int currentNumber = 7;
        int expectedNumber = 2;
        String expectedEvent = EventFactory.move(expectedNumber).toStringValue();

        clientEventHandler.handle(client, EventFactory.move(currentNumber));

        verify(client, times(1)).send(eventStringCaptor.capture());
        assertThat(eventStringCaptor.getValue()).isEqualTo(expectedEvent);
    }

    @Test
    public void shouldHandleGameEndedEvent() {
        clientEventHandler.handle(client, EventFactory.gameEnded(GameResultStatus.LOSE));
        verify(client, times(1)).close();
    }
}
