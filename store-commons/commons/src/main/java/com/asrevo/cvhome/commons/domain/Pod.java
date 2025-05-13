package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public record Pod(
        PodId id,
        String name,
        PodEndpoint endpoint,
        @JsonDeserialize(using = ManagerOrgIdDeserializer.class) ManagerOrgId orgId) {}
