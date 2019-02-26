package com.horizon.syncservice.client;

public class SyncServiceException extends Exception {
    static final long serialVersionUID = 1;

    public SyncServiceException(String message) {
        super(message);
    }

    public SyncServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}