package com.horizon.syncservice.client;

/**
 * An exception thrown when a Sync Service API call to the Sync Server returns an error
 * that occurred on the server.
 */
public class SyncServiceException extends Exception {
    static final long serialVersionUID = 1;

    /**
     * Constructor
     * @param message Message to set in the exception.
     */
    SyncServiceException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param message Message to set in the exception.
     * @param cause   Throwable that caused this exception to be thrown.
     */
    SyncServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
