package com.horizon.syncservice.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * An internal class used in sending payloads for various Sync Service APIs
 */
@JsonInclude(Include.NON_NULL)
class ACLBulkPayload {
    @JsonProperty("action")
    private final String action;

    @JsonProperty("usernames")
    private final String[] usernames;
    
    ACLBulkPayload(String action, String[] usernames) {
        this.action = action;
        this.usernames = usernames;
    }

    protected String getAction() {
        return action;
    }

    protected String[] getUsernames() {
        return usernames;
    }
}