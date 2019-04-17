package com.horizon.syncservice.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * An internal class used in sending payloads for various Sync Service APIs
 */
@JsonInclude(Include.NON_NULL)
class ObjectPayload {
    
    @JsonProperty("meta")
    private SyncServiceMetaData meta;

    ObjectPayload(SyncServiceMetaData meta) {
        this.meta = meta;
    }

    protected SyncServiceMetaData getMeta() {
        return meta;
    }
    
    protected void setMeta(SyncServiceMetaData meta) {
        this.meta = meta;
    }
}