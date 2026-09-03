package com.asrevo.cvhome.uaa.dto;

/**
 * What the sign-in page can say about why it is being shown: the client that started the authorization, and the
 * brokered login waiting to be confirmed, if any.
 */
public record LoginContext(String clientId, String clientName, PendingLinkView pendingLink) {

    public record PendingLinkView(String providerAlias, String providerName, String email) {
    }

}
