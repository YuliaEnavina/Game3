package org.gameofthree.common.session;

public class SessionInProgressException extends RuntimeException {
    public SessionInProgressException(String message) {
        super(message);
    }
}
