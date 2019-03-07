package com.horizon.syncservice.client;

/**
 * An internal class used in sending payloads for various Sync Service APIs
 */
class ObjectPayload {
    
    private SyncServiceMetaData meta;

    ObjectPayload(SyncServiceMetaData meta) {
        this.meta = meta;
    }

    public SyncServiceMetaData getMeta() {
        return meta;
    }
    public void setMeta(SyncServiceMetaData meta) {
        this.meta = meta;
    }
}