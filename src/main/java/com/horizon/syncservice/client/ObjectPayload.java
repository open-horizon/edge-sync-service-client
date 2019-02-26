package com.horizon.syncservice.client;

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