package com.asrevo.cvhome.sso.dto;

import com.asrevo.cvhome.sso.client.ClientType;

/** The client list's filters; every part is optional and they combine with AND. */
public record ClientSearch(String q, Boolean enabled, ClientType type) {

    public static ClientSearch none() {
        return new ClientSearch(null, null, null);
    }

}
