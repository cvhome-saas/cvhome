package com.asrevo.cvhome.commons.domain;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Pod(PodId id, String name, PodEndpoint endpoint, ManagerOrgId orgId, String domain) {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String shortenPodId() {
        if (Objects.isNull(id)) {
            return null;
        }
        return id.shorten();
    }
}
